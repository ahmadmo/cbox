package org.telegram.bot.cbox;

import org.telegram.bot.AbstractTelegramBot;
import org.telegram.bot.logging.Log4j;
import org.telegram.bot.messaging.LoopShiftingStrategy;
import org.telegram.bot.messaging.Message;
import org.telegram.bot.messaging.MessageQueueLoopStrategy;
import org.telegram.bot.util.concurrent.TimeProperty;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.telegram.bot.messaging.Message.toMessage;

/**
 * @author ahmad
 */
public final class CBoxBot extends AbstractTelegramBot {

    public static final TimeProperty DEFAULT_UPDATE_INTERVAL = TimeProperty.millis(500L);

    private final String token;
    private final long updateInterval;
    private final ChatLoopHandler handler;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private int lastOffset = 0;

    public CBoxBot(String token) throws SQLException {
        this(token, DEFAULT_UPDATE_INTERVAL, new LoopShiftingStrategy());
    }

    public CBoxBot(String token, TimeProperty updateInterval, MessageQueueLoopStrategy strategy) throws SQLException {
        this.token = token;
        this.updateInterval = updateInterval.to(TimeUnit.MILLISECONDS);
        handler = new ChatLoopHandler(this, strategy);
    }

    public CBoxBot(String token, TimeProperty updateInterval, int parallelism, MessageQueueLoopStrategy strategy) throws SQLException {
        this.token = token;
        this.updateInterval = updateInterval.to(TimeUnit.MILLISECONDS);
        handler = new ChatLoopHandler(this, parallelism, strategy);
    }

    @Override
    public void run() {
        if (running.compareAndSet(false, true)) {
            try {
                while (!Thread.currentThread().isInterrupted() && isRunning()) {
                    List updates = select();
                    if (updates != null) {
                        handle(updates);
                        setLastOffset(updates);
                    }
                }
            } catch (Throwable e) {
                Log4j.BOT.error(e.getMessage(), e);
            } finally {
                running.set(false);
                handler.close();
            }
        } else {
            Log4j.BOT.warn("Bot is currently running...");
        }
    }

    @Override
    public String token() {
        return token;
    }

    private List select() {
        while (isRunning()) {
            Message response = getUpdates(String.valueOf(lastOffset + 1));
            if (ok(response)) {
                List updates = response.getList("result");
                if (updates != null && !updates.isEmpty()) {
                    return updates;
                }
            }
            sleep(updateInterval);
        }
        return null;
    }

    private void handle(List updates) {
        for (Object o : updates) {
            if (isRunning()) {
                Message message = getMessage(o);
                if (message != null && !message.isEmpty()) {
                    handler.handle(message);
                }
            } else {
                break;
            }
        }
    }

    private void setLastOffset(List messages) {
        Message last = toMessage(messages.get(messages.size() - 1));
        if (last != null) {
            Integer offset = last.getInt("update_id");
            if (offset != null) {
                lastOffset = offset;
            }
        }
    }

    public boolean isRunning() {
        return running.get() && handler.isLooping();
    }

    @Override
    public void stop() {
        running.set(false);
    }

    private static Message getMessage(Object o) {
        Message update = toMessage(o);
        return update != null && !update.isEmpty() ? toMessage(update.get("message")) : null;
    }

    private static boolean ok(Message response) {
        if (response != null) {
            Boolean ok = response.getBoolean("ok");
            if (ok != null && ok) {
                return true;
            }
            Log4j.BOT.error(response);
        }
        return false;
    }

    private static void sleep(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException ignored) {
        }
    }

}

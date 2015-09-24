package org.telegram.bot.messaging;

import org.telegram.bot.util.concurrent.FieldUpdater;
import org.telegram.bot.util.concurrent.Function;
import org.telegram.bot.util.concurrent.TimeProperty;

/**
 * @author ahmad
 */
public final class LoopShiftingStrategy implements MessageQueueLoopStrategy {

    public static final TimeProperty DEFAULT_SHIFTING_TIMEOUT = TimeProperty.millis(500L);

    private final TimeProperty timeout;
    private final FieldUpdater<Boolean> poke;

    public LoopShiftingStrategy() {
        this(DEFAULT_SHIFTING_TIMEOUT);
    }

    public LoopShiftingStrategy(TimeProperty timeout) {
        this.timeout = timeout;
        poke = new FieldUpdater<>(false, new Function<Boolean, Boolean>() {
            @Override
            public Boolean apply(Boolean b) {
                return false;
            }
        }, timeout);
    }

    @Override
    public Message next(MessageQueue queue) {
        Message message = queue.poll(timeout);
        if (message == null && poke.compareAndSet(true, false)) {
            message = queue.poll(timeout);
        }
        return message;
    }

    @Override
    public void poke() {
        poke.set(true);
    }

    @Override
    public boolean stopIfEmpty() {
        return !poke.getAndSet(false);
    }

}

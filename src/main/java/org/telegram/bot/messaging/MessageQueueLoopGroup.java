package org.telegram.bot.messaging;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.telegram.bot.util.concurrent.CacheManager;
import org.telegram.bot.util.concurrent.Consumer;
import org.telegram.bot.util.concurrent.TimeProperty;

import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ahmad
 */
public final class MessageQueueLoopGroup {

    public static final int DEFAULT_PARALLELISM = 1024;

    private final ForkJoinPool pool;
    private final MessageQueueLoopHandler handler;
    private final MessageQueueLoopStrategy strategy;
    private final CacheManager<KeyPair, MessageQueueLoop> loops;
    private final AtomicBoolean looping = new AtomicBoolean(true);

    public MessageQueueLoopGroup(MessageQueueLoopHandler handler, MessageQueueLoopStrategy strategy) {
        this(DEFAULT_PARALLELISM, handler, strategy);
    }

    public MessageQueueLoopGroup(int parallelism, MessageQueueLoopHandler handler, MessageQueueLoopStrategy strategy) {
        Objects.requireNonNull(handler);
        pool = new ForkJoinPool(parallelism);
        this.handler = handler;
        this.strategy = strategy;
        loops = new CacheManager<>(TimeProperty.minutes(10), new RemovalListener<KeyPair, MessageQueueLoop>() {
            @Override
            public void onRemoval(RemovalNotification<KeyPair, MessageQueueLoop> notification) {
                MessageQueueLoop loop = notification.getValue();
                if (loop != null) {
                    loop.stop();
                }
            }
        });
    }

    public void queueMessage(KeyPair key, Message message) {
        final MessageQueueLoop loop = getLoop(key);
        loop.getMessageQueue().add(message);
        loop.getStrategy().poke();
        if (!loop.isLooping()) {
            pool.execute(loop);
        }
    }

    private MessageQueueLoop getLoop(KeyPair key) {
        MessageQueueLoop loop = loops.retrieve(key);
        if (loop == null) {
            final MessageQueueLoop l = loops.cacheIfAbsent(key, loop = new MessageQueueLoop(key, new ConcurrentMessageQueue(), handler, strategy));
            if (l != null) {
                loop = l;
            }
        }
        return loop;
    }

    public boolean isLooping() {
        return looping.get();
    }

    public boolean shutdownGracefully() {
        if (looping.compareAndSet(true, false)) {
            pool.shutdown();
            loops.forEachValue(new Consumer<MessageQueueLoop>() {
                @Override
                public void accept(MessageQueueLoop loop) {
                    loop.stop();
                }
            });
            pool.shutdownNow();
            try {
                return pool.awaitTermination(15L, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
        }
        return false;
    }

}

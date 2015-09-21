package org.telegram.bot.messaging;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ahmad
 */
public final class MessageQueueLoop implements Runnable {

    private static final Message STOP_MESSAGE = new Message();

    private final KeyPair key;
    private final MessageQueue messageQueue;
    private final MessageQueueLoopHandler handler;
    private final MessageQueueLoopStrategy strategy;
    private final AtomicBoolean looping = new AtomicBoolean(false);

    public MessageQueueLoop(KeyPair key, MessageQueue messageQueue, MessageQueueLoopHandler handler, MessageQueueLoopStrategy strategy) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(handler);
        Objects.requireNonNull(strategy);
        this.key = key;
        this.messageQueue = messageQueue;
        this.handler = handler;
        this.strategy = strategy;
    }

    @Override
    public void run() {
        if (looping.compareAndSet(false, true)) {
            Message message = null;
            try {
                while (!Thread.currentThread().isInterrupted() && looping.get()) {
                    message = strategy.pollMessage(messageQueue);
                    if (message != null) {
                        if (message == STOP_MESSAGE) {
                            if (looping.get()) {
                                messageQueue.stop(STOP_MESSAGE);
                            } else {
                                break;
                            }
                        } else {
                            handler.onMessage(this, message);
                        }
                    } else if (strategy.stopIfEmpty()) {
                        break;
                    }
                }
            } catch (Throwable cause) {
                handler.exceptionCaught(this, message, cause);
            } finally {
                looping.set(false);
            }
        }
    }

    public KeyPair getKey() {
        return key;
    }

    public MessageQueue getMessageQueue() {
        return messageQueue;
    }

    public MessageQueueLoopHandler getHandler() {
        return handler;
    }

    public MessageQueueLoopStrategy getStrategy() {
        return strategy;
    }

    public boolean isLooping() {
        return looping.get();
    }

    public void stop() {
        looping.set(false);
        messageQueue.stop(STOP_MESSAGE);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null && obj instanceof MessageQueueLoop && key.equals(((MessageQueueLoop) obj).key);
    }

}

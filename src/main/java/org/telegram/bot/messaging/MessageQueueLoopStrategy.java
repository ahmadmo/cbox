package org.telegram.bot.messaging;

/**
 * @author ahmad
 */
public interface MessageQueueLoopStrategy {

    Message pollMessage(MessageQueue queue);

    void poke();

    boolean stopIfEmpty();

}

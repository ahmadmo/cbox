package org.telegram.bot.messaging;

import org.telegram.bot.util.concurrent.TimeProperty;

import java.util.Iterator;
import java.util.List;

/**
 * @author ahmad
 */
public interface MessageQueue {

    boolean add(Message message);

    boolean addAll(List<Message> messages);

    Message poll();

    Message poll(TimeProperty timeout);

    Message take();

    Iterator<Message> iterator();

    List<Message> removeAll();

    void stop(Message stopMessage);

}

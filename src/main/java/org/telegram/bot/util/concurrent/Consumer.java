package org.telegram.bot.util.concurrent;

public interface Consumer<T> {

    void accept(T t);

}

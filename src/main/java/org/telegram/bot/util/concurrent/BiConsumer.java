package org.telegram.bot.util.concurrent;

public interface BiConsumer<T, U> {

    void accept(T t, U u);

}

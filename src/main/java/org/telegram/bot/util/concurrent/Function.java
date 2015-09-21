package org.telegram.bot.util.concurrent;

public interface Function<T, R> {

    R apply(T t);

}

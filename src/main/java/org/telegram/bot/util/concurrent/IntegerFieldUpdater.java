package org.telegram.bot.util.concurrent;

/**
 * @author ahmad
 */
public final class IntegerFieldUpdater extends FieldUpdater<Integer> {

    public IntegerFieldUpdater(Integer value, Function<Integer, Integer> updater, TimeProperty updateInterval) {
        super(value, updater, updateInterval);
    }

    public void add(int delta) {
        set(get() + delta);
    }

}

package org.telegram.bot.util.concurrent;

import java.util.ConcurrentModificationException;
import java.util.Map;

/**
 * @author ahmad
 */
public final class MapUtil {

    private MapUtil() {
    }

    public static <K, V> void forEach(Map<K, V> map, BiConsumer<? super K, ? super V> action) {
        for (Map.Entry<K, V> e : map.entrySet()) {
            K k;
            V v;
            try {
                k = e.getKey();
                v = e.getValue();
            } catch (IllegalStateException ex) {
                throw new ConcurrentModificationException(ex);
            }
            action.accept(k, v);
        }
    }

    public static <K, V> void forEachValue(Map<K, V> map, Consumer<? super V> action) {
        for (V v : map.values()) {
            action.accept(v);
        }
    }

}

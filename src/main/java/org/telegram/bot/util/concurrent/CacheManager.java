package org.telegram.bot.util.concurrent;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;

import java.util.concurrent.ConcurrentMap;

/**
 * @author ahmad
 */
public final class CacheManager<K, V> {

    private final ConcurrentMap<K, V> cache;

    public CacheManager(TimeProperty duration) {
        this(duration, false);
    }

    public CacheManager(TimeProperty duration, RemovalListener<K, V> listener) {
        this(duration, listener, false);
    }

    public CacheManager(TimeProperty duration, boolean expireAfterWrite) {
        if (expireAfterWrite) {
            cache = CacheBuilder.newBuilder()
                    .expireAfterWrite(duration.getTime(), duration.getUnit())
                    .concurrencyLevel(16)
                    .<K, V>build().asMap();
        } else {
            cache = CacheBuilder.newBuilder()
                    .expireAfterAccess(duration.getTime(), duration.getUnit())
                    .concurrencyLevel(16)
                    .<K, V>build().asMap();
        }
    }

    public CacheManager(TimeProperty duration, RemovalListener<K, V> listener, boolean expireAfterWrite) {
        if (expireAfterWrite) {
            cache = CacheBuilder.newBuilder()
                    .expireAfterWrite(duration.getTime(), duration.getUnit())
                    .concurrencyLevel(16)
                    .removalListener(listener)
                    .<K, V>build().asMap();
        } else {
            cache = CacheBuilder.newBuilder()
                    .expireAfterAccess(duration.getTime(), duration.getUnit())
                    .concurrencyLevel(16)
                    .removalListener(listener)
                    .<K, V>build().asMap();
        }
    }

    public V cache(K k, V v) {
        return cache.put(k, v);
    }

    public V cacheIfAbsent(K key, V value) {
        return cache.putIfAbsent(key, value);
    }

    public boolean containsKey(K k) {
        return cache.containsKey(k);
    }

    public boolean containsValue(V v) {
        return cache.containsValue(v);
    }

    public V retrieve(K k) {
        return cache.get(k);
    }

    public V expire(K k) {
        return cache.remove(k);
    }

    public void forEach(BiConsumer<? super K, ? super V> action) {
        MapUtil.forEach(cache, action);
    }

    public void forEachValue(Consumer<? super V> action) {
        MapUtil.forEachValue(cache, action);
    }

    public void clear() {
        cache.clear();
    }

    @Override
    public String toString() {
        return cache.toString();
    }

}

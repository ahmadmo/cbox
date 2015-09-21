package org.telegram.bot.messaging;

import org.telegram.bot.util.concurrent.CacheManager;
import org.telegram.bot.util.concurrent.TimeProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author ahmad
 */
public final class KeyPair implements Serializable {

    private static final long serialVersionUID = 2634687758160469917L;

    private static final CacheManager<Integer, KeyPair> CACHE = new CacheManager<>(TimeProperty.minutes(5));

    private final String firstKey;
    private final String secondKey;

    private KeyPair(final String firstKey, final String secondKey) {
        this.firstKey = firstKey;
        this.secondKey = secondKey;
    }

    public String getFirstKey() {
        return firstKey;
    }

    public String getSecondKey() {
        return secondKey;
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstKey, secondKey);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof KeyPair)) {
            return false;
        }
        KeyPair other = (KeyPair) obj;
        return firstKey.equals(other.firstKey) && secondKey.equals(other.secondKey);
    }

    @Override
    public String toString() {
        return "{" +
                "firstKey='" + firstKey + '\'' +
                ", secondKey='" + secondKey + '\'' +
                '}';
    }

    public static KeyPair get(final String firstKey, final String secondKey) {
        int hash = Objects.hash(firstKey, secondKey);
        KeyPair pair = CACHE.retrieve(hash);
        if (pair == null) {
            final KeyPair p = CACHE.cacheIfAbsent(hash, pair = new KeyPair(firstKey, secondKey));
            if (p != null) {
                pair = p;
            }
        }
        return pair;
    }

}

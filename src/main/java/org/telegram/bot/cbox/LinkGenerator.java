package org.telegram.bot.cbox;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.telegram.bot.util.concurrent.CacheManager;
import org.telegram.bot.util.concurrent.TimeProperty;

import java.util.concurrent.ConcurrentHashMap;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

/**
 * @author ahmad
 */
public final class LinkGenerator {

    private static final int UID_LENGTH = 6;
    private static final String LINK_FORMAT = "https://telegram.me/CBoxBot?start=%s";
    private static final ConcurrentHashMap<String, String> FILES = new ConcurrentHashMap<>();
    private static final CacheManager<String, String> LINKS = new CacheManager<>(TimeProperty.days(3), new RemovalListener<String, String>() {
        @Override
        public void onRemoval(RemovalNotification<String, String> notification) {
            String fileId = notification.getValue();
            if (fileId != null) {
                FILES.remove(fileId);
            }
        }
    }, true);

    private LinkGenerator() {
    }

    public static String generate(String fileId) {
        String uid, s;
        do {
            s = LINKS.cacheIfAbsent(uid = randomAlphanumeric(UID_LENGTH), fileId);
        } while (s != null);
        s = FILES.get(fileId);
        if (s != null) {
            LINKS.expire(s);
        }
        FILES.put(fileId, uid);
        return String.format(LINK_FORMAT, uid);
    }

    public static String getFileId(String uid) {
        return LINKS.retrieve(uid);
    }

}

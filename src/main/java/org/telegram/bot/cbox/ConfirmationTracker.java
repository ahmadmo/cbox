package org.telegram.bot.cbox;

import org.telegram.bot.messaging.KeyPair;
import org.telegram.bot.util.concurrent.CacheManager;
import org.telegram.bot.util.concurrent.TimeProperty;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.telegram.bot.cbox.ChatState.CONFIRM_DELETE;

/**
 * @author ahmad
 */
public final class ConfirmationTracker {

    private static final CacheManager<KeyPair, Confirmation> CONFIRMATIONS = new CacheManager<>(TimeProperty.minutes(5));

    public static Confirmation submit(KeyPair confirmationKey, KeyPair owner, ChatSession session, int confirmCount, CompletionListener listener) {
        Confirmation confirmation = new Confirmation(confirmationKey, owner, session, confirmCount, listener);
        CONFIRMATIONS.cache(confirmationKey, confirmation);
        return confirmation;
    }

    public static Confirmation getConfirmation(KeyPair key) {
        return CONFIRMATIONS.retrieve(key);
    }

    public interface CompletionListener {

        void onComplete();

    }

    public static final class Confirmation {

        private final KeyPair key;
        private final KeyPair owner;
        private final ChatSession session;
        private final int confirmCount;
        private final CompletionListener listener;
        private final Set<KeyPair> confirmers = Collections.newSetFromMap(new ConcurrentHashMap<KeyPair, Boolean>());
        private final AtomicBoolean completed = new AtomicBoolean(false);

        private Confirmation(KeyPair key, KeyPair owner, ChatSession session, int confirmCount, CompletionListener listener) {
            this.key = key;
            this.owner = owner;
            this.session = session;
            this.confirmCount = confirmCount;
            this.listener = listener;
        }

        public int confirm(KeyPair confirmer) {
            if (isValid()) {
                confirmers.add(confirmer);
                int i = Math.max(0, confirmCount - confirmers.size());
                if (i == 0 && completed.compareAndSet(false, true)) {
                    CONFIRMATIONS.expire(key);
                    listener.onComplete();
                }
                return i;
            } else {
                CONFIRMATIONS.expire(key);
            }
            return -1;
        }

        public KeyPair getKey() {
            return key;
        }

        public KeyPair getOwner() {
            return owner;
        }

        public boolean isValid() {
            return session.getCurrentChatState() == CONFIRM_DELETE && confirmers.size() < confirmCount;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj != null && obj instanceof Confirmation && key.equals(((Confirmation) obj).key);
        }

    }

}

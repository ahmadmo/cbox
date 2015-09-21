package org.telegram.bot.cbox;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.telegram.bot.cbox.model.FileItem;
import org.telegram.bot.messaging.KeyPair;
import org.telegram.bot.util.concurrent.CacheManager;
import org.telegram.bot.util.concurrent.TimeProperty;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ahmad
 */
public final class ChatSession {

    private static final CacheManager<KeyPair, ChatSession> SESSIONS
            = new CacheManager<>(TimeProperty.minutes(30), new RemovalListener<KeyPair, ChatSession>() {
        @Override
        public void onRemoval(RemovalNotification<KeyPair, ChatSession> notification) {
            ChatSession session = notification.getValue();
            if (session != null) {
                session.versionHolder.clear();
            }
        }
    });

    private final KeyPair key;
    private final FileVersionHolder versionHolder = new FileVersionHolder();
    private final FileFilter filter = new FileFilter();
    private final Mood mood = new Mood();
    private final AtomicReference<ChatState> currentChatState = new AtomicReference<>(ChatState.NONE);
    private final AtomicReference<ChatContext> currentChatContext = new AtomicReference<>(ChatContext.NONE);
    private final AtomicReference<String> currentSearchQuery = new AtomicReference<>();
    private final AtomicReference<FileItem> currentFile = new AtomicReference<>();

    public ChatSession(KeyPair key) {
        this.key = key;
    }

    public FileVersionHolder getVersionHolder() {
        return versionHolder;
    }

    public FileFilter getFilter() {
        return filter;
    }

    public Mood getMood() {
        return mood;
    }

    public ChatState getCurrentChatState() {
        return currentChatState.get();
    }

    public void setCurrentChatState(ChatState state) {
        currentChatState.set(state);
    }

    public ChatContext getCurrentChatContext() {
        return currentChatContext.get();
    }

    public void setCurrentChatContext(ChatContext context) {
        currentChatContext.set(context);
    }

    public String getCurrentSearchQuery() {
        return currentSearchQuery.get();
    }

    public void setCurrentSearchQuery(String query) {
        currentSearchQuery.set(query);
    }

    public FileItem getCurrentFile() {
        return currentFile.get();
    }

    public void setCurrentFile(FileItem fileItem) {
        currentFile.set(fileItem);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null && obj instanceof ChatSession && key.equals(((ChatSession) obj).key);
    }

    public static ChatSession get(KeyPair key) {
        ChatSession session = SESSIONS.retrieve(key);
        if (session == null) {
            final ChatSession s = SESSIONS.cacheIfAbsent(key, session = new ChatSession(key));
            if (s != null) {
                session = s;
            }
        }
        return session;
    }

}

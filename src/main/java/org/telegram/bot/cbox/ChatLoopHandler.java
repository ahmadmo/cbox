package org.telegram.bot.cbox;

import org.telegram.bot.TelegramBot;
import org.telegram.bot.cbox.bl.ChatRepository;
import org.telegram.bot.cbox.bl.FileItemRepository;
import org.telegram.bot.cbox.model.Chat;
import org.telegram.bot.cbox.model.FileItem;
import org.telegram.bot.cbox.model.SendType;
import org.telegram.bot.logging.Log4j;
import org.telegram.bot.messaging.*;
import org.telegram.bot.util.JDBCUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.telegram.bot.cbox.ChatState.*;
import static org.telegram.bot.cbox.Constants.*;
import static org.telegram.bot.cbox.SearchQueryHelper.*;
import static org.telegram.bot.messaging.Message.toMessage;

/**
 * @author ahmad
 */
public final class ChatLoopHandler implements MessageQueueLoopHandler, AutoCloseable {

    private final TelegramBot bot;
    private final Connection connection;
    private final FileItemRepository fileItemRepository;
    private final ChatRepository chatRepository;
    private final MessageQueueLoopGroup loopGroup;

    public ChatLoopHandler(TelegramBot bot, MessageQueueLoopStrategy strategy) throws SQLException {
        this(bot, MessageQueueLoopGroup.DEFAULT_PARALLELISM, strategy);
    }

    public ChatLoopHandler(TelegramBot bot, int parallelism, MessageQueueLoopStrategy strategy) throws SQLException {
        this.bot = bot;
        connection = JDBCUtil.getConnection();
        fileItemRepository = new FileItemRepository(connection);
        chatRepository = new ChatRepository(connection, fileItemRepository);
        loopGroup = new MessageQueueLoopGroup(parallelism, this, strategy);
    }

    public void handle(Message message) {
        loopGroup.queueMessage(getChatKey(message), message);
    }

    @Override
    public void onMessage(MessageQueueLoop loop, Message message) {
        KeyPair key = loop.getKey();
        String userId = key.getFirstKey();
        String chatId = key.getSecondKey();
        boolean group = !chatId.equals(userId);
        String replyTo = group ? String.valueOf(message.getInt("message_id")) : null;
        String text = message.getString("text");
        ChatSession session = ChatSession.get(key);
        if (session.getCurrentChatContext() == ChatContext.NONE) {
            session.setCurrentChatContext(group ? ChatContext.GROUP : ChatContext.PRIVATE);
        }
        if (text != null) {
            text = text.trim();
            CommandQuery cq = Command.match(text);
            if (cq != null) {
                checkState(chatId, replyTo, session, processCommand(key, group, replyTo, session, cq));
                session.getMood().incrementCommands();
            } else {
                processMessage(message, key, replyTo, text, session);
            }
        } else {
            try {
                FileItem item = FileParser.parse(chatId, message);
                if (item != null) {
                    fileItemRepository.save(item);
                }
            } catch (Exception e) {
                Log4j.BOT.error(e.getMessage(), e);
            }
        }
    }

    private ChatState processCommand(KeyPair key, boolean group, String replyTo, ChatSession session, CommandQuery cq) {
        String userId = key.getFirstKey();
        String chatId = key.getSecondKey();
        switch (cq.getCommand()) {
            case START:
                return sendStart(chatId, replyTo, cq.getQuery()) == NONE
                        ? session.getCurrentChatState()
                        : null;
            case LIST:
                if (group) {
                    return sendChooseContext(chatId, replyTo);
                } else {
                    return sendList(key, userId, null, session);
                }
            case SEARCH:
                if (group) {
                    session.setCurrentSearchQuery(cq.getQuery());
                    return sendChooseSearchContext(chatId, replyTo);
                } else {
                    return sendSearch(key, userId, null, session, cq.getQuery());
                }
            case SET_FILTERS:
                session.getMood().incrementFilters();
                return sendSetFilters(key, replyTo, session, cq.getQuery());
            case NO_FILTERS:
                session.getMood().incrementFilters();
                session.getFilter().noFilters();
                return sendNoFilters(key, replyTo);
            case HELP:
                return sendHelp(chatId, replyTo);
            case CANCEL:
                final ChatState currentChatState = session.getCurrentChatState();
                session.setCurrentChatState(NONE);
                session.setCurrentChatContext(ChatContext.NONE);
                session.setCurrentSearchQuery(null);
                session.setCurrentFile(null);
                session.getMood().incrementCancels();
                return sendCancel(chatId, replyTo, currentChatState, session.getMood().cancelMood());
        }
        return null;
    }

    private void processMessage(Message message, KeyPair key, String replyTo, String text, ChatSession session) {
        String userId = key.getFirstKey();
        String chatId = key.getSecondKey();
        ChatState chatState = null, currentChatState = session.getCurrentChatState();
        ConfirmationTracker.Confirmation groupConfirmation = null;
        Message m = toMessage(message.get("reply_to_message"));
        if (m != null) {
            groupConfirmation = ConfirmationTracker.getConfirmation(KeyPair.get(chatId, String.valueOf(m.getInt("message_id"))));
        }
        boolean checkState = false, groupConfirmationNotNull = groupConfirmation != null,
                checkGroupConfirmation = groupConfirmationNotNull && (currentChatState == NONE || currentChatState == CONFIRM_DELETE);
        if (groupConfirmationNotNull && !checkGroupConfirmation && !groupConfirmation.getOwner().equals(key)) {
            bot.sendMessage(chatId, CONFIRMATION_REJECTED, replyTo, null);
        }
        String answer;
        switch (currentChatState) {
            case CHOOSE_CONTEXT:
                answer = CHOOSE_CONTEXT.matchAnswer(text);
                if (answer == null) {
                    if (ok(bot.sendMessage(chatId, String.format(INVALID_CONTEXT_MESSAGE, session.getMood().invalidAnswerMood()),
                            replyTo, null))) {
                        chatState = currentChatState;
                    }
                    checkState = true;
                    session.getMood().incrementInvalidAnswers();
                } else {
                    switch (answer) {
                        case MY_FILES:
                            session.setCurrentChatContext(ChatContext.PRIVATE);
                            chatState = sendList(key, userId, replyTo, session);
                            checkState = true;
                            break;
                        case GROUP_FILES:
                            session.setCurrentChatContext(ChatContext.GROUP);
                            chatState = sendList(key, chatId, replyTo, session);
                            checkState = true;
                            break;
                    }
                }
                if (checkState) {
                    checkState(chatId, replyTo, session, chatState);
                }
                break;
            case CHOOSE_SEARCH_CONTEXT:
                answer = CHOOSE_SEARCH_CONTEXT.matchAnswer(text);
                if (answer == null) {
                    if (ok(bot.sendMessage(chatId, String.format(INVALID_CONTEXT_MESSAGE, session.getMood().invalidAnswerMood()),
                            replyTo, null))) {
                        chatState = currentChatState;
                    }
                    checkState = true;
                    session.getMood().incrementInvalidAnswers();
                } else {
                    final String query = session.getCurrentSearchQuery();
                    switch (answer) {
                        case MY_FILES:
                            session.setCurrentChatContext(ChatContext.PRIVATE);
                            session.setCurrentSearchQuery(null);
                            chatState = sendSearch(key, userId, replyTo, session, query);
                            checkState = true;
                            break;
                        case GROUP_FILES:
                            session.setCurrentChatContext(ChatContext.GROUP);
                            session.setCurrentSearchQuery(null);
                            chatState = sendSearch(key, chatId, replyTo, session, query);
                            checkState = true;
                            break;
                    }
                }
                if (checkState) {
                    checkState(chatId, replyTo, session, chatState);
                }
                break;
            case SELECT_FILE:
                if (text.equalsIgnoreCase(DELETE_ALL)) {
                    chatState = sendConfirmDeleteAll(key, replyTo, session);
                } else {
                    String fileId = session.getVersionHolder().getFileId(text);
                    if (fileId == null) {
                        if (ok(bot.sendMessage(chatId, String.format(INVALID_FILE_NAME_MESSAGE, session.getMood().invalidAnswerMood()),
                                replyTo, null))) {
                            chatState = currentChatState;
                        }
                        session.getMood().incrementInvalidAnswers();
                    } else {
                        FileItem item = fileItemRepository.get(fileId);
                        if (item == null) {
                            if (ok(bot.sendMessage(chatId, FILE_NOT_FOUND_MESSAGE, replyTo, null))) {
                                chatState = currentChatState;
                            }
                        } else {
                            session.setCurrentFile(item);
                            chatState = sendFileOptions(chatId, replyTo);
                        }
                    }
                }
                checkState(chatId, replyTo, session, chatState);
                break;
            case FILE_OPTIONS:
                answer = FILE_OPTIONS.matchAnswer(text);
                if (answer == null) {
                    if (ok(bot.sendMessage(chatId, String.format(INVALID_OPTION_MESSAGE, session.getMood().invalidAnswerMood()),
                            replyTo, null))) {
                        chatState = currentChatState;
                    }
                    checkState = true;
                    session.getMood().incrementInvalidAnswers();
                } else {
                    switch (answer) {
                        case GET_FILE:
                            chatState = sendFile(chatId, replyTo, session.getCurrentFile());
                            checkState = true;
                            break;
                        case FILE_METADATA:
                            chatState = sendFileMetaData(chatId, replyTo, session.getCurrentFile());
                            checkState = true;
                            break;
                        case SHARE_FILE:
                            chatState = sendShareFile(chatId, replyTo, session.getCurrentFile());
                            checkState = true;
                            break;
                        case DELETE_FILE:
                            chatState = sendConfirmDelete(key, replyTo, session);
                            checkState = true;
                            break;
                    }
                }
                if (checkState) {
                    checkState(chatId, replyTo, session, chatState);
                }
                break;
            case CONFIRM_DELETE:
                ChatContext currentChatContext = session.getCurrentChatContext();
                switch (currentChatContext) {
                    case PRIVATE:
                        ConfirmationTracker.Confirmation confirmation = ConfirmationTracker.getConfirmation(key);
                        if (confirmation != null && confirmation.isValid()) {
                            if (text.equalsIgnoreCase(DELETE_CONFIRMATION)) {
                                confirmation.confirm(key);
                            } else {
                                if (ok(bot.sendMessage(chatId,
                                        String.format(INVALID_DELETE_CONFIRMATION_MESSAGE, session.getMood().invalidAnswerMood()),
                                        replyTo, null))) {
                                    chatState = currentChatState;
                                }
                                checkState = true;
                                session.getMood().incrementInvalidAnswers();
                            }
                        } else {
                            if (ok(bot.sendMessage(chatId, CONFIRMATION_ENDED_MESSAGE, replyTo, null))) {
                                chatState = NONE;
                            }
                            checkState = true;
                        }
                        break;
                    case GROUP:
                        if (groupConfirmationNotNull) {
                            checkGroupConfirmation = false;
                            if (groupConfirmation.isValid()) {
                                if (text.equalsIgnoreCase(DELETE_CONFIRMATION_GROUP)) {
                                    int i = groupConfirmation.confirm(key);
                                    if (i > 0) {
                                        if (i == 1) {
                                            if (ok(bot.sendMessage(chatId, ONE_MORE_CONFIRMATION, replyTo, null))) {
                                                chatState = currentChatState;
                                            }
                                        } else if (ok(bot.sendMessage(chatId, String.format(N_MORE_TO_GO, i), replyTo, null))) {
                                            chatState = currentChatState;
                                        }
                                        checkState = true;
                                    }
                                } else {
                                    if (ok(bot.sendMessage(chatId,
                                            String.format(INVALID_DELETE_CONFIRMATION_GROUP_MESSAGE, session.getMood().invalidAnswerMood()),
                                            replyTo, null))) {
                                        chatState = currentChatState;
                                    }
                                    checkState = true;
                                    session.getMood().incrementInvalidAnswers();
                                }
                            } else {
                                if (ok(bot.sendMessage(chatId, CONFIRMATION_ENDED_MESSAGE, replyTo, null))) {
                                    chatState = NONE;
                                }
                                checkState = true;
                            }
                        }
                }
                if (checkState) {
                    checkState(chatId, replyTo, session, chatState);
                }
        }
        if (checkGroupConfirmation) {
            if (groupConfirmation.isValid()) {
                if (text.equalsIgnoreCase(DELETE_CONFIRMATION_GROUP)) {
                    int i = groupConfirmation.confirm(key);
                    if (i > 0) {
                        if (i == 1) {
                            if (ok(bot.sendMessage(chatId, ONE_MORE_CONFIRMATION, replyTo, null))) {
                                chatState = currentChatState;
                            }
                        } else if (ok(bot.sendMessage(chatId, String.format(N_MORE_TO_GO, i), replyTo, null))) {
                            chatState = currentChatState;
                        }
                        checkState = true;
                    }
                } else {
                    if (ok(bot.sendMessage(chatId,
                            String.format(INVALID_DELETE_CONFIRMATION_GROUP_MESSAGE, session.getMood().invalidAnswerMood()),
                            replyTo, null))) {
                        chatState = currentChatState;
                    }
                    checkState = true;
                    session.getMood().incrementInvalidAnswers();
                }
            } else {
                if (ok(bot.sendMessage(chatId, CONFIRMATION_ENDED_MESSAGE, replyTo, null))) {
                    chatState = currentChatState;
                }
                checkState = true;
            }
            if (checkState) {
                checkState(chatId, replyTo, session, chatState);
            }
        }
        if (currentChatState != NONE || checkGroupConfirmation) {
            session.getMood().incrementCommands();
        }
    }

    @Override
    public void exceptionCaught(MessageQueueLoop loop, Message message, Throwable cause) {
//        if (message != null) {
//            KeyPair key = loop.getKey();
//            String userId = key.getFirstKey();
//            String chatId = key.getSecondKey();
//            sendError(chatId, !chatId.equals(userId) ? String.valueOf(message.getInt("message_id")) : null, ChatSession.get(key));
//        }
        Log4j.BOT.error(cause.getMessage(), cause);
    }

    private ChatState sendStart(String chatId, String replyTo, String uid) {
        if (uid == null) {
            return NONE;
        }
        String fileId = LinkGenerator.getFileId(uid);
        if (fileId == null) {
            return ok(bot.sendMessage(chatId, INVALID_LINK_MESSAGE, replyTo, null))
                    ? NONE
                    : null;
        }
        FileItem item = fileItemRepository.get(fileId);
        if (item == null) {
            return ok(bot.sendMessage(chatId, FILE_NOT_FOUND_MESSAGE, replyTo, null))
                    ? NONE
                    : null;
        }
        Message response = null;
        switch (item.getSendType()) {
            case PHOTO:
                response = bot.sendPhoto(chatId, fileId, replyTo, null);
                break;
            case VIDEO:
                response = bot.sendVideo(chatId, fileId, replyTo, null);
                break;
            case AUDIO:
            case DOCUMENT:
                response = bot.sendDocument(chatId, fileId, replyTo, null);
                break;
        }
        return ok(response) ? NONE : null;
    }

    private ChatState sendChooseContext(String chatId, String replyTo) {
        return ok(bot.sendMessage(chatId, CHOOSE_CONTEXT_MESSAGE, replyTo, CHOOSE_CONTEXT_KEYBOARD))
                ? CHOOSE_CONTEXT
                : null;
    }

    private ChatState sendChooseSearchContext(String chatId, String replyTo) {
        return ok(bot.sendMessage(chatId, CHOOSE_CONTEXT_MESSAGE, replyTo, CHOOSE_CONTEXT_KEYBOARD))
                ? CHOOSE_SEARCH_CONTEXT
                : null;
    }

    private ChatState sendList(KeyPair key, String chatOrUserId, String replyTo, ChatSession session) {
        Chat chat = chatRepository.get(chatOrUserId);
        return sendList(key, replyTo, session, chat == null ? null : chat.getFileItems());
    }

    private ChatState sendSearch(KeyPair key, String chatOrUserId, String replyTo, ChatSession session, String query) {
        final String chatId = key.getSecondKey();
        if (query == null) {
            String fileName = chatRepository.randomFileName(chatOrUserId);
            String text = String.format(SEARCH_HELP_MESSAGE, fileName == null ? "fileName" : removeExtension(fileName), searchDateExample());
            return ok(bot.sendMessage(chatId, text, replyTo, HIDE_KEYBOARD))
                    ? NONE
                    : null;
        }
        List<FileItem> items;
        Date[] dates = parseDates(query);
        if (dates.length > 0) {
            Date from = dates[0], to = dates[dates.length - 1];
            Calendar cal = Calendar.getInstance();
            cal.setTime(from);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            from = cal.getTime();
            cal.setTime(to);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            to = cal.getTime();
            items = chatRepository.searchPeriod(chatOrUserId, from, to);
        } else {
            items = chatRepository.searchByName(chatOrUserId, query);
        }
        if (items.isEmpty()) {
            ChatState state = ok(bot.sendMessage(chatId,
                    String.format(SEARCH_DID_NOT_MATCH_MESSAGE, query, session.getMood().searchFailMood()),
                    replyTo, HIDE_KEYBOARD)) ? NONE : null;
            session.getMood().incrementSearchFails();
            return state;
        }
        return sendList(key, replyTo, session, items);
    }

    private ChatState sendList(KeyPair key, String replyTo, ChatSession session, List<FileItem> items) {
        final String chatId = key.getSecondKey();
        if (items == null || items.isEmpty()) {
            return ok(bot.sendMessage(chatId, EMPTY_LIST_MESSAGE, replyTo, HIDE_KEYBOARD))
                    ? NONE
                    : null;
        }
        FileVersionHolder versionHolder = session.getVersionHolder();
        versionHolder.clear();
        FileItemList list = FileItemListBuilder.build(versionHolder, session.getFilter(), items);
        if (list != null && list.getListSize() > 0) {
            String text = list.getText();
            String replyMarkup = list.getKeyboard();
            return text != null && !text.isEmpty() && replyMarkup != null && ok(bot.sendMessage(chatId, text, replyTo, replyMarkup))
                    ? SELECT_FILE
                    : null;
        }
        return ok(bot.sendMessage(chatId, EMPTY_LIST_MESSAGE, replyTo, HIDE_KEYBOARD))
                ? NONE
                : null;
    }

    private ChatState sendSetFilters(KeyPair key, String replyTo, ChatSession session, String query) {
        final String chatId = key.getSecondKey();
        final FileFilter filter = session.getFilter();
        if (query == null) {
            return ok(bot.sendMessage(chatId, String.format(SET_FILTERS_HELP_MESSAGE, Arrays.toString(filter.getFilters())),
                    replyTo, HIDE_KEYBOARD))
                    ? NONE
                    : null;
        }
        ChatState state = null;
        final SendType[] types = FileFilter.parse(query);
        final Mood mood = session.getMood();
        if (types == null) {
            if (ok(bot.sendMessage(chatId, String.format(INVALID_FILTERS_MESSAGE, mood.invalidAnswerMood()),
                    replyTo, HIDE_KEYBOARD))) {
                state = NONE;
            }
            mood.incrementInvalidAnswers();
        } else {
            filter.setFilters(types);
            if (ok(bot.sendMessage(chatId,
                    String.format(FILTERS_UPDATED_MESSAGE, mood.filtersMood(), Arrays.toString(filter.getFilters())),
                    replyTo, HIDE_KEYBOARD))) {
                state = NONE;
            }
        }
        return state;
    }

    private ChatState sendNoFilters(KeyPair key, String replyTo) {
        return ok(bot.sendMessage(key.getSecondKey(), NO_FILTERS_MESSAGE, replyTo, HIDE_KEYBOARD))
                ? NONE
                : null;
    }

    private ChatState sendHelp(String chatId, String replyTo) {
        return ok(bot.sendMessage(chatId, HELP_MESSAGE, replyTo, HIDE_KEYBOARD))
                ? NONE
                : null;
    }

    private ChatState sendCancel(String chatId, String replyTo, ChatState currentChatState, String mood) {
        String text;
        switch (currentChatState) {
            case CHOOSE_CONTEXT:
                text = String.format(CANCEL_COMMAND_MESSAGE, Command.LIST.getCommand(), mood);
                break;
            case CHOOSE_SEARCH_CONTEXT:
                text = String.format(CANCEL_COMMAND_MESSAGE, Command.SEARCH.getCommand(), mood);
                break;
            case SELECT_FILE:
                text = String.format(CANCEL_COMMAND_MESSAGE, Command.LIST.getCommand(), mood);
                break;
            case FILE_OPTIONS:
                text = String.format(CANCEL_COMMAND_MESSAGE, "fileOptions", mood);
                break;
            case CONFIRM_DELETE:
                text = String.format(CANCEL_COMMAND_MESSAGE, "deleteFile", mood);
                break;
            default:
                text = NO_ACTIVE_COMMAND_MESSAGE;
        }
        return ok(bot.sendMessage(chatId, text, replyTo, HIDE_KEYBOARD))
                ? NONE
                : null;
    }

    private ChatState sendConfirmDeleteAll(final KeyPair key, final String replyTo, final ChatSession session) {
        final String chatId = key.getSecondKey();
        final ChatContext currentChatContext = session.getCurrentChatContext();
        final FileFilter filter = session.getFilter();
        switch (currentChatContext) {
            case PRIVATE:
                if (ok(bot.sendMessage(chatId, CONFIRM_DELETE_ALL_MESSAGE, replyTo, HIDE_KEYBOARD))) {
                    ConfirmationTracker.submit(key, key, session, 1, new ConfirmationTracker.CompletionListener() {
                        @Override
                        public void onComplete() {
                            ChatState state = chatRepository.delete(new Chat(chatId), filter)
                                    ? ok(bot.sendMessage(chatId, DELETE_ALL_COMPLETED_MESSAGE, replyTo, HIDE_KEYBOARD)) ? NONE : null
                                    : null;
                            checkState(chatId, replyTo, session, state);
                        }
                    });
                    return CONFIRM_DELETE;
                }
                break;
            case GROUP:
                Message response = bot.sendMessage(chatId, CONFIRM_DELETE_ALL_GROUP_MESSAGE, replyTo, HIDE_KEYBOARD);
                if (ok(response)) {
                    KeyPair confirmationKey = KeyPair.get(chatId,
                            String.valueOf(toMessage(response.get("result")).getInt("message_id")));
                    ConfirmationTracker.submit(confirmationKey, key, session, 3, new ConfirmationTracker.CompletionListener() {
                        @Override
                        public void onComplete() {
                            ChatState state = chatRepository.delete(new Chat(chatId), filter)
                                    ? ok(bot.sendMessage(chatId, DELETE_ALL_COMPLETED_MESSAGE, replyTo, HIDE_KEYBOARD)) ? NONE : null
                                    : null;
                            checkState(chatId, replyTo, session, state);
                        }
                    });
                    return CONFIRM_DELETE;
                }
        }
        return null;
    }

    private ChatState sendConfirmDelete(final KeyPair key, final String replyTo, final ChatSession session) {
        final String chatId = key.getSecondKey();
        final FileItem fileToBeDeleted = session.getCurrentFile();
        if (fileToBeDeleted == null) {
            return ok(bot.sendMessage(chatId, FILE_NOT_FOUND_MESSAGE, replyTo, HIDE_KEYBOARD))
                    ? NONE
                    : null;
        }
        final ChatContext currentChatContext = session.getCurrentChatContext();
        switch (currentChatContext) {
            case PRIVATE:
                if (ok(bot.sendMessage(chatId, String.format(CONFIRM_DELETE_FILE_MESSAGE, fileToBeDeleted.getFileName()),
                        replyTo, HIDE_KEYBOARD))) {
                    ConfirmationTracker.submit(key, key, session, 1, new ConfirmationTracker.CompletionListener() {
                        @Override
                        public void onComplete() {
                            ChatState state = fileItemRepository.delete(fileToBeDeleted)
                                    ? ok(bot.sendMessage(chatId, DELETE_COMPLETED_MESSAGE, replyTo, HIDE_KEYBOARD)) ? NONE : null
                                    : null;
                            checkState(chatId, replyTo, session, state);
                        }
                    });
                    return CONFIRM_DELETE;
                }
                break;
            case GROUP:
                Message response = bot.sendMessage(chatId,
                        String.format(CONFIRM_DELETE_FILE_GROUP_MESSAGE, fileToBeDeleted.getFileName()),
                        replyTo, HIDE_KEYBOARD);
                if (ok(response)) {
                    KeyPair confirmationKey = KeyPair.get(chatId,
                            String.valueOf(toMessage(response.get("result")).getInt("message_id")));
                    ConfirmationTracker.submit(confirmationKey, key, session, 3, new ConfirmationTracker.CompletionListener() {
                        @Override
                        public void onComplete() {
                            ChatState state = fileItemRepository.delete(fileToBeDeleted)
                                    ? ok(bot.sendMessage(chatId, DELETE_COMPLETED_MESSAGE, replyTo, HIDE_KEYBOARD)) ? NONE : null
                                    : null;
                            checkState(chatId, replyTo, session, state);
                        }
                    });
                    return CONFIRM_DELETE;
                }
        }
        return null;
    }

    private ChatState sendFileOptions(String chatId, String replyTo) {
        return ok(bot.sendMessage(chatId, FILE_OPTIONS_MESSAGE, replyTo, FILE_OPTIONS_KEYBOARD))
                ? FILE_OPTIONS
                : null;
    }

    private ChatState sendFile(String chatId, String replyTo, FileItem item) {
        if (item == null) {
            return ok(bot.sendMessage(chatId, FILE_NOT_FOUND_MESSAGE, replyTo, HIDE_KEYBOARD))
                    ? NONE
                    : null;
        }
        String fileId = item.getFileId();
        Message response = null;
        switch (item.getSendType()) {
            case PHOTO:
                response = bot.sendPhoto(chatId, fileId, replyTo, FILE_OPTIONS_KEYBOARD);
                break;
            case VIDEO:
                response = bot.sendVideo(chatId, fileId, replyTo, FILE_OPTIONS_KEYBOARD);
                break;
            case AUDIO:
//                response = bot.sendAudio(chatId, fileId, replyTo, FILE_OPTIONS_KEYBOARD);
//                break;
            case DOCUMENT:
                response = bot.sendDocument(chatId, fileId, replyTo, FILE_OPTIONS_KEYBOARD);
                break;
        }
        return ok(response) ? FILE_OPTIONS : null;
    }

    private ChatState sendFileMetaData(String chatId, String replyTo, FileItem item) {
        return item == null ? ok(bot.sendMessage(chatId, FILE_NOT_FOUND_MESSAGE, replyTo, HIDE_KEYBOARD))
                ? NONE
                : null : ok(bot.sendMessage(chatId, FileMetadata.getMetadata(item), replyTo, FILE_OPTIONS_KEYBOARD))
                ? FILE_OPTIONS
                : null;
    }

    private ChatState sendShareFile(String chatId, String replyTo, FileItem item) {
        return item == null ? ok(bot.sendMessage(chatId, FILE_NOT_FOUND_MESSAGE, replyTo, HIDE_KEYBOARD))
                ? NONE
                : null : ok(bot.sendMessage(chatId, String.format(LINK_MESSAGE, LinkGenerator.generate(item.getFileId())),
                replyTo, FILE_OPTIONS_KEYBOARD))
                ? FILE_OPTIONS
                : null;
    }

    private void checkState(String chatId, String replyTo, ChatSession session, ChatState chatState) {
        if (chatState != null) {
            session.setCurrentChatState(chatState);
        } else {
//            sendError(chatId, replyTo, session);
            Log4j.BOT.error(String.format("Exception in chat id = %s", chatId));
        }
    }

//    private void sendError(String chatId, String replyTo, ChatSession session) {
//        bot.sendMessage(chatId, String.format(ERROR_MESSAGE, session.getMood().errorMood()), replyTo, HIDE_KEYBOARD);
//        session.setCurrentChatState(NONE);
//        session.getMood().incrementErrors();
//    }

    public boolean isLooping() {
        return loopGroup.isLooping();
    }

    @Override
    public void close() {
        loopGroup.shutdownGracefully();
        JDBCUtil.close(connection);
    }

    private static KeyPair getChatKey(Message message) {
        Message from = toMessage(message.get("from"));
        Message chat = toMessage(message.get("chat"));
        String userId = String.valueOf(from.getInt("id"));
        String chatId = String.valueOf(chat.getInt("id"));
        return KeyPair.get(userId, chatId);
    }

    private static boolean ok(Message response) {
        if (response != null) {
            Boolean ok = response.getBoolean("ok");
            if (ok != null && ok) {
                return true;
            }
            Log4j.BOT.error(response);
        }
        return false;
    }

}

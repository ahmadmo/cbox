package org.telegram.bot.cbox.bl;

import org.telegram.bot.cbox.FileFilter;
import org.telegram.bot.cbox.model.Chat;
import org.telegram.bot.cbox.model.FileItem;
import org.telegram.bot.cbox.model.SendType;
import org.telegram.bot.logging.Log4j;
import org.telegram.bot.util.JDBCTemplate;

import java.sql.*;
import java.util.*;

import static org.telegram.bot.cbox.bl.PreparedQueries.*;

/**
 * @author ahmad
 */
public final class ChatRepository extends JDBCTemplate implements IChatRepository {

    private final IFileItemRepository fileItemRepository;

    public ChatRepository(Connection connection, IFileItemRepository fileItemRepository) {
        super(connection);
        this.fileItemRepository = fileItemRepository;
    }

    @Override
    public boolean save(Chat chat) {
        return fileItemRepository.save(chat.getFileItems());
    }

    @Override
    public Chat get(String id) {
        try (PreparedStatement statement = prepareStatement(FIND_BY_CHAT_ID)) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Chat chat = new Chat(id);
                    do {
                        chat.getFileItems().add(fileItemRepository.mapRow(rs));
                    } while (rs.next());
                    return chat;
                }
            }
        } catch (SQLException e) {
            Log4j.BOT.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean delete(Chat chat) {
        try (PreparedStatement statement = prepareStatement(DELETE_BY_CHAT_ID)) {
            statement.setString(1, chat.getChatId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            Log4j.BOT.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean delete(Chat chat, FileFilter filter) {
        List<SendType> types = new ArrayList<>();
        for (SendType type : SendType.values()) {
            if (filter.doFilter(type)) {
                types.add(type);
            }
        }
        boolean modified = false;
        try (PreparedStatement statement = prepareStatement(DELETE_BY_CHAT_ID_FILTER)) {
            statement.setString(1, chat.getChatId());
            for (SendType type : types) {
                statement.setInt(2, type.ordinal());
                modified |= statement.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            Log4j.BOT.error(e.getMessage(), e);
        }
        return modified;
    }

    @Override
    public List<Chat> loadAll() {
        List<FileItem> fileItems = fileItemRepository.loadAll();
        Map<String, Chat> chats = new HashMap<>();
        for (FileItem fileItem : fileItems) {
            String chatId = fileItem.getChatId();
            Chat chat = chats.get(chatId);
            if (chat == null) {
                chats.put(chatId, chat = new Chat(chatId));
            }
            chat.getFileItems().add(fileItem);
        }
        return new ArrayList<>(chats.values());
    }

    @Override
    public List<FileItem> searchByName(String chatId, String fileName) {
        List<FileItem> items = new ArrayList<>();
        try (PreparedStatement statement = prepareStatement(SEARCH_BY_NAME)) {
            statement.setString(1, chatId);
            statement.setString(2, "%" + fileName + "%");
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    items.add(fileItemRepository.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            Log4j.BOT.error(e.getMessage(), e);
        }
        return items;
    }

    @Override
    public List<FileItem> searchPeriod(String chatId, java.util.Date from, java.util.Date to) {
        if (from.after(to)) {
            return Collections.emptyList();
        }
        List<FileItem> items = new ArrayList<>();
        try (PreparedStatement statement = prepareStatement(SEARCH_PERIOD)) {
            statement.setString(1, chatId);
            statement.setTimestamp(2, new Timestamp(from.getTime()));
            statement.setTimestamp(3, new Timestamp(to.getTime()));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    items.add(fileItemRepository.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            Log4j.BOT.error(e.getMessage(), e);
        }
        return items;
    }

    @Override
    public String randomFileName(String chatId) {
        try (PreparedStatement statement = prepareStatement(SELECT_RANDOM_FILE_NAME)) {
            statement.setString(1, chatId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("file_name");
                }
            }
        } catch (SQLException e) {
            Log4j.BOT.error(e.getMessage(), e);
        }
        return null;
    }

}

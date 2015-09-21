package org.telegram.bot.cbox.bl;

import org.telegram.bot.cbox.model.FileItem;
import org.telegram.bot.cbox.model.SendType;
import org.telegram.bot.logging.Log4j;
import org.telegram.bot.util.JDBCTemplate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.telegram.bot.cbox.bl.PreparedQueries.*;

/**
 * @author ahmad
 */
public final class FileItemRepository extends JDBCTemplate implements IFileItemRepository {

    public FileItemRepository(Connection connection) {
        super(connection);
    }

    @Override
    public boolean save(FileItem fileItem) {
        try (PreparedStatement statement = prepareStatement(INSERT_FILE_ITEM)) {
            return insert(fileItem, statement);
        } catch (SQLException e) {
            if (e.getSQLState().startsWith("23") && e.getMessage().contains("Duplicate entry")) {
                return true;
            }
            Log4j.BOT.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public FileItem get(String id) {
        try (PreparedStatement statement = prepareStatement(FIND_BY_FILE_ID)) {
            statement.setString(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            Log4j.BOT.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<FileItem> loadAll() {
        List<FileItem> items = new ArrayList<>();
        try (Statement statement = createStatement();
             ResultSet rs = statement.executeQuery(SELECT_ALL)) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        } catch (SQLException e) {
            Log4j.BOT.error(e.getMessage(), e);
        }
        return items;
    }

    @Override
    public boolean delete(FileItem fileItem) {
        try (PreparedStatement statement = prepareStatement(DELETE_BY_FILE_ID)) {
            statement.setString(1, fileItem.getFileId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            Log4j.BOT.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean save(List<FileItem> fileItems) {
        boolean modified = false;
        try (PreparedStatement statement = prepareStatement(INSERT_FILE_ITEM)) {
            for (FileItem fileItem : fileItems) {
                modified |= insert(fileItem, statement);
            }
        } catch (SQLException e) {
            Log4j.BOT.error(e.getMessage(), e);
        }
        return modified;
    }

    @Override
    public FileItem mapRow(ResultSet rs) throws SQLException {
        return new FileItem(rs.getString("file_id"), rs.getString("chat_id"), rs.getString("file_name"),
                rs.getString("mime_type"), rs.getInt("file_size"),
                new java.util.Date(rs.getTimestamp("date").getTime()),
                SendType.values()[rs.getInt("send_type")],
                rs.getInt("height"), rs.getInt("width"), rs.getInt("duration"));
    }

    private boolean insert(FileItem fileItem, PreparedStatement statement) throws SQLException {
        statement.setString(1, fileItem.getFileId());
        statement.setString(2, fileItem.getChatId());
        statement.setString(3, fileItem.getFileName());
        statement.setString(4, fileItem.getMimeType());
        statement.setInt(5, fileItem.getFileSize());
        statement.setTimestamp(6, new Timestamp(fileItem.getDate().getTime()));
        statement.setInt(7, fileItem.getSendType().ordinal());
        statement.setInt(8, fileItem.getHeight());
        statement.setInt(9, fileItem.getWidth());
        statement.setInt(10, fileItem.getDuration());
        return statement.executeUpdate() == 1;
    }

}

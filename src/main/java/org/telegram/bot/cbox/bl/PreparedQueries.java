package org.telegram.bot.cbox.bl;

/**
 * @author ahmad
 */
public final class PreparedQueries {

    private PreparedQueries() {
    }

    public static final String INSERT_FILE_ITEM = "INSERT INTO chat_files (file_id, chat_id, file_name, mime_type, file_size, date, send_type, height, width, duration) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String FIND_BY_FILE_ID = "SELECT * FROM chat_files WHERE file_id = ?";

    public static final String FIND_BY_CHAT_ID = "SELECT * FROM chat_files WHERE chat_id = ?";

    public static final String SEARCH_BY_NAME = "SELECT * FROM chat_files WHERE chat_id = ? AND file_name LIKE ?";

    public static final String SEARCH_PERIOD = "SELECT * FROM chat_files WHERE chat_id = ? AND date BETWEEN ? AND ?";

    public static final String SELECT_ALL = "SELECT * FROM chat_files";

    public static final String SELECT_RANDOM_FILE_NAME = "SELECT file_name FROM chat_files WHERE chat_id = ? ORDER BY RAND() LIMIT 1";

    public static final String DELETE_BY_FILE_ID = "DELETE FROM chat_files WHERE file_id = ?";

    public static final String DELETE_BY_CHAT_ID = "DELETE FROM chat_files WHERE chat_id = ?";

    public static final String DELETE_BY_CHAT_ID_FILTER = "DELETE FROM chat_files WHERE chat_id = ? AND send_type = ?";

}

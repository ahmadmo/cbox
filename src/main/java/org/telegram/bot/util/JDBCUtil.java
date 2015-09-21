package org.telegram.bot.util;

import org.telegram.bot.logging.Log4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author ahmad
 */
public final class JDBCUtil {

    private static final String db_host = System.getenv("OPENSHIFT_MYSQL_DB_HOST");
    private static final String db_port = System.getenv("OPENSHIFT_MYSQL_DB_PORT");
    private static final String driverClassName = ApplicationProps.getProperty("jdbc.driverClassName");
    private static final String url = String.format(ApplicationProps.getProperty("jdbc.url"), db_host, db_port);
    private static final String username = ApplicationProps.getProperty("jdbc.username");
    private static final String password = ApplicationProps.getProperty("jdbc.password");

    private JDBCUtil() {
    }

    static {
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            Log4j.BOT.error(e.getMessage(), e);
            System.exit(0);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static void close(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            Log4j.BOT.error(e.getMessage(), e);
        }
    }

}

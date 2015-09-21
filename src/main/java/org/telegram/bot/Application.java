package org.telegram.bot;

import org.telegram.bot.cbox.CBoxBot;
import org.telegram.bot.util.ApplicationProps;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.SQLException;

/**
 * @author ahmad
 */
public final class Application implements ServletContextListener {

    private final TelegramBot bot;

    public Application() throws SQLException {
        String token = ApplicationProps.getProperty("bot.token");
        this.bot = new CBoxBot(token);
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Thread t = new Thread(bot);
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        bot.stop();
    }

}

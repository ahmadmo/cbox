package org.telegram.bot.util;

import org.telegram.bot.logging.Log4j;

import java.io.IOException;
import java.util.Properties;

/**
 * @author ahmad
 */
public final class ApplicationProps {

    private static final Properties PROPS = new Properties();

    private ApplicationProps() {
    }

    static {
        try {
            PROPS.load(ApplicationProps.class.getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            Log4j.BOT.error(e.getMessage(), e);
            System.exit(0);
        }
    }

    public static String getProperty(String key) {
        return PROPS.getProperty(key);
    }

    public static String getProperty(String key, String def) {
        return PROPS.getProperty(key, def);
    }

}

package org.telegram.bot.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.telegram.bot.util.ApplicationProps;

/**
 * @author ahmad
 */
public final class Log4j {

    public static final Logger BOT;

    private static final String DEFAULT_LOG_PATH = System.getenv("OPENSHIFT_REPO_DIR") + "/.cBoxBot/log";

    static {
        System.setProperty("log4j.path", ApplicationProps.getProperty("log4j.path", DEFAULT_LOG_PATH));
        BOT = LogManager.getLogger("BOT");
    }

}

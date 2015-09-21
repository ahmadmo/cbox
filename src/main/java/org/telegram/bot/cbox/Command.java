package org.telegram.bot.cbox;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ahmad
 */
public enum Command {

    START("start"),
    LIST("list"),
    SEARCH("search"),
    SET_FILTERS("setFilters"),
    NO_FILTERS("noFilters"),
    HELP("help"),
    CANCEL("cancel");

    private static final Pattern COMMAND_PATTERN = Pattern.compile("/\\s*(\\w+)", Pattern.CASE_INSENSITIVE);

    private final String command;

    Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return command;
    }

    public static CommandQuery match(String text) {
        Matcher matcher = COMMAND_PATTERN.matcher(text);
        if (matcher.find() && matcher.start() == 0) {
            String command = matcher.group(1);
            for (Command c : Command.values()) {
                if (c.command.equalsIgnoreCase(command)) {
                    String q = text.substring(matcher.end()).trim();
                    if (q.isEmpty()) {
                        q = null;
                    }
                    return new CommandQuery(c, q);
                }
            }
        }
        return null;
    }

}

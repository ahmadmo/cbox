package org.telegram.bot.cbox;

/**
 * @author ahmad
 */
public final class CommandQuery {

    private final Command command;
    private final String query;

    public CommandQuery(Command command, String query) {
        this.command = command;
        this.query = query;
    }

    public Command getCommand() {
        return command;
    }

    public String getQuery() {
        return query;
    }

}

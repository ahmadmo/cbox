package org.telegram.bot.cbox;

import static org.telegram.bot.cbox.Constants.*;

/**
 * @author ahmad
 */
public enum ChatState {

    CHOOSE_CONTEXT(MY_FILES, GROUP_FILES),
    CHOOSE_SEARCH_CONTEXT(MY_FILES, GROUP_FILES),
    SELECT_FILE(),
    FILE_OPTIONS(GET_FILE, FILE_METADATA, SHARE_FILE, DELETE_FILE),
    CONFIRM_DELETE(),
    NONE();

    private final String[] expectedAnswers;

    ChatState(String... expectedAnswers) {
        this.expectedAnswers = expectedAnswers;
    }

    public String[] getExpectedAnswers() {
        return expectedAnswers;
    }

    public String matchAnswer(String answer) {
        for (String s : expectedAnswers) {
            if (s.equalsIgnoreCase(answer)) {
                return s;
            }
        }
        return null;
    }

}

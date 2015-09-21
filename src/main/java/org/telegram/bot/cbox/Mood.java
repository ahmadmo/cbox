package org.telegram.bot.cbox;

import org.telegram.bot.messaging.Emoticons;
import org.telegram.bot.util.concurrent.Function;
import org.telegram.bot.util.concurrent.IntegerFieldUpdater;
import org.telegram.bot.util.concurrent.TimeProperty;

import static org.telegram.bot.messaging.Emoticons.*;

/**
 * @author ahmad
 */
public final class Mood {

    private static final String[] SEARCH_FAIL_MOODS = {
            TEETH, TONGUE, LAUGH_1 + LAUGH_1, LAUGH_2, LAUGH_3, LAUGH_4 + LAUGH_4,
            NOT_SURE_1 + TIRED, NOT_SURE_1, NOT_SURE_2 + NOT_SURE_2, NOT_SURE_1 + HIDDEN_FACE, SURPRISED + SURPRISED,
            CRY_1, CRY_2 + CRY_2, NOT_SURE_1 + SAD_1, SAD_1 + SAD_2, SAD_2 + CRY_1, SAD_3 + SAD_3,
            PAIN_1 + CRY_2, PAIN_2 + PAIN_2 + PAIN_2, SHOCKED + SHOCKED, SHOCKED + SCARED, SHOCKED + SHOCKED + SHOCKED + SCARED
    };

    private static final String[] FILTERS_MOODS = Emoticons.VALUES;

    private static final String[] ERROR_MOODS = {
            TEETH + TEETH, LAUGH_4 + LAUGH_4, NOT_SURE_1, NOT_SURE_1 + NOT_SURE_2, HIDDEN_FACE + NOT_SURE_2, SURPRISED + SURPRISED + NOT_SURE_2,
            CRY_1, CRY_2 + CRY_2 + CRY_2, NOT_SURE_1 + SAD_1 + SAD_2, SAD_1 + CRY_2, SAD_2, SAD_3,
            PAIN_1 + SAD_3, PAIN_2 + PAIN_2, SHOCKED + SHOCKED, SHOCKED + SCARED + SCARED, SHOCKED + SHOCKED + SHOCKED + SCARED + SCARED
    };

    private static final String[] CANCEL_MOODS = Emoticons.VALUES;

    private static final String[] INVALID_ANSWER_MOODS = {
            LAUGH_1, LAUGH_2, LAUGH_3 + LAUGH_3,
            LAUGH_1 + TIRED, NOT_SURE_1 + NOT_SURE_1, NOT_SURE_1 + NOT_SURE_2 + NOT_SURE_2, SURPRISED + HIDDEN_FACE,
            SURPRISED + SURPRISED + SURPRISED,
            CRY_2 + CRY_2 + CRY_2, PAIN_1 + CRY_2, PAIN_2 + PAIN_2 + PAIN_2, SURPRISED + SHOCKED, ANGRY_1 + ANGRY_1, ANGRY_3,
            SHOCKED + ANGRY_2 + ANGRY_2, ANGRY_3 + ANGRY_2 + ANGRY_2 + ANGRY_2,
            ANGRY_2 + ANGRY_2 + ANGRY_2 + ANGRY_2, ANGRY_2 + ANGRY_2 + ANGRY_2 + ANGRY_2 + ANGRY_3 + ANGRY_3
    };

    private final Function<Integer, Integer> updater = new Function<Integer, Integer>() {
        @Override
        public Integer apply(Integer i) {
            return Math.max(0, i - 5);
        }
    };
    private final IntegerFieldUpdater commands = new IntegerFieldUpdater(0, updater, TimeProperty.minutes(5));
    private final IntegerFieldUpdater searchFails = new IntegerFieldUpdater(0, updater, TimeProperty.minutes(5));
    private final IntegerFieldUpdater filters = new IntegerFieldUpdater(0, updater, TimeProperty.minutes(5));
    private final IntegerFieldUpdater errors = new IntegerFieldUpdater(0, updater, TimeProperty.minutes(5));
    private final IntegerFieldUpdater cancels = new IntegerFieldUpdater(0, updater, TimeProperty.minutes(5));
    private final IntegerFieldUpdater invalidAnswers = new IntegerFieldUpdater(0, updater, TimeProperty.minutes(10));

    public void incrementCommands() {
        commands.add(1);
    }

    public void incrementSearchFails() {
        searchFails.add(1);
    }

    public void incrementFilters() {
        filters.add(1);
    }

    public void incrementErrors() {
        errors.add(1);
    }

    public void incrementCancels() {
        cancels.add(1);
    }

    public void incrementInvalidAnswers() {
        invalidAnswers.add(1);
    }

    public String searchFailMood() {
        int idx = (int) Math.floor((commands.get() + invalidAnswers.get() + searchFails.get() - errors.get()) / 10.0);
        return SEARCH_FAIL_MOODS[Math.min(Math.abs(idx), SEARCH_FAIL_MOODS.length - 1)];
    }

    public String filtersMood() {
        int idx = (int) Math.floor((commands.get() + invalidAnswers.get() + filters.get() - errors.get()) / 10.0);
        return FILTERS_MOODS[Math.min(Math.abs(idx), FILTERS_MOODS.length - 1)];
    }

    public String errorMood() {
        int idx = (int) Math.floor(errors.get() / 10.0);
        return ERROR_MOODS[Math.min(idx, ERROR_MOODS.length - 1)];
    }

    public String cancelMood() {
        int idx = (int) Math.floor((commands.get() + invalidAnswers.get() + cancels.get() - errors.get()) / 10.0);
        return CANCEL_MOODS[Math.min(Math.abs(idx), CANCEL_MOODS.length - 1)];
    }

    public String invalidAnswerMood() {
        int idx = (int) Math.floor((commands.get() + invalidAnswers.get() - errors.get()) / 10.0);
        return INVALID_ANSWER_MOODS[Math.min(Math.abs(idx), INVALID_ANSWER_MOODS.length - 1)];
    }

    public void reset() {
        commands.set(0);
        searchFails.set(0);
        filters.set(0);
        errors.set(0);
        cancels.set(0);
        invalidAnswers.set(0);
    }

}

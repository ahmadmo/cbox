package org.telegram.bot.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.telegram.bot.logging.Log4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ahmad
 */
public final class KeyboardBuilder {

    private final List<String[]> rows = new ArrayList<>();
    private boolean resize = false;
    private boolean oneTime = false;
    private boolean selective = false;

    public KeyboardBuilder addRow(String... rowData) {
        rows.add(rowData);
        return this;
    }

    public KeyboardBuilder resize(boolean b) {
        resize = b;
        return this;
    }

    public KeyboardBuilder oneTime(boolean b) {
        oneTime = b;
        return this;
    }

    public KeyboardBuilder selective(boolean b) {
        selective = b;
        return this;
    }

    public String build() {
        Message m = new Message();
        m.put("keyboard", rows.toArray());
        if (resize) {
            m.put("resize_keyboard", true);
        }
        if (oneTime) {
            m.put("one_time_keyboard", true);
        }
        if (selective) {
            m.put("selective", true);
        }
        try {
            return Message.encode(m);
        } catch (JsonProcessingException e) {
            Log4j.BOT.error(e.getMessage(), e);
        }
        return null;
    }

}

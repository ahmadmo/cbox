package org.telegram.bot.cbox;

/**
 * @author ahmad
 */
public final class FileItemList {

    private final String text;
    private final String keyboard;
    private final int listSize;

    public FileItemList(String text, String keyboard, int listSize) {
        this.text = text;
        this.keyboard = keyboard;
        this.listSize = listSize;
    }

    public String getText() {
        return text;
    }

    public String getKeyboard() {
        return keyboard;
    }

    public int getListSize() {
        return listSize;
    }

}

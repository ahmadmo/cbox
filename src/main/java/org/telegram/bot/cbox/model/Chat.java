package org.telegram.bot.cbox.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ahmad
 */
public final class Chat {

    private String chatId;
    private List<FileItem> fileItems;

    public Chat(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public List<FileItem> getFileItems() {
        if (fileItems == null) {
            fileItems = new ArrayList<>();
        }
        return fileItems;
    }

    public void setFileItems(List<FileItem> fileItems) {
        this.fileItems = fileItems;
    }

    @Override
    public String toString() {
        return "{" +
                "chatId='" + chatId + '\'' +
                ", fileItems=" + fileItems +
                '}';
    }

}

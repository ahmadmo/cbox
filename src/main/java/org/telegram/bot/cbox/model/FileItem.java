package org.telegram.bot.cbox.model;

import java.util.Date;

/**
 * @author ahmad
 */
public final class FileItem implements Comparable<FileItem> {

    private String fileId;
    private String chatId;
    private String fileName;
    private String mimeType;
    private int fileSize;
    private Date date;
    private SendType sendType;
    private int height;
    private int width;
    private int duration;

    public FileItem(String fileId, String chatId, String fileName,
                    String mimeType, int fileSize, Date date, SendType sendType,
                    int height, int width, int duration) {
        this.fileId = fileId;
        this.chatId = chatId;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.date = date;
        this.sendType = sendType;
        this.height = height;
        this.width = width;
        this.duration = duration;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public SendType getSendType() {
        return sendType;
    }

    public void setSendType(SendType sendType) {
        this.sendType = sendType;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "{" +
                "fileId='" + fileId + '\'' +
                ", chatId='" + chatId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", fileSize=" + fileSize +
                ", date=" + date +
                ", sendType=" + sendType +
                ", height=" + height +
                ", width=" + width +
                ", duration=" + duration +
                '}';
    }

    @Override
    public int compareTo(FileItem o) {
        return date.compareTo(o.date);
    }

    public int compareToDesc(FileItem o) {
        return o.date.compareTo(date);
    }

}

package org.telegram.bot.cbox;

import org.telegram.bot.cbox.model.FileItem;
import org.telegram.bot.messaging.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.telegram.bot.cbox.model.SendType.*;
import static org.telegram.bot.messaging.Message.toMessage;

/**
 * @author ahmad
 */
public final class FileParser {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String VIDEO_MP4 = "video/mp4";
    public static final String AUDIO_AAC = "audio/aac";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    private FileParser() {
    }

    public static FileItem parse(String chatId, Message message) throws Exception {
        Date date = message.getDate("date");
        List photoList = message.getList("photo");
        if (photoList != null) {
            Message photo = toMessage(photoList.get(photoList.size() - 1));
            if (photo != null) {
                return photo(chatId, date, photo);
            }
        }
        Message video = toMessage(message.get("video"));
        if (video != null) {
            return video(chatId, date, video);
        }
        Message audio = toMessage(message.get("audio"));
        if (audio != null) {
            return audio(chatId, date, audio);
        }
        Message document = toMessage(message.get("document"));
        if (document != null) {
            return document(chatId, date, document);
        }
        return null;
    }

    private static FileItem photo(String chatId, Date date, Message photo) {
        String fileId = photo.getString("file_id");
        String mimeType = getOrDefault(photo.getString("mime_type"), IMAGE_JPEG);
        String fileName = getOrDefault(photo.getString("file_name"), generateFileName(mimeType, date));
        Integer fileSize = getOrDefault(photo.getInt("file_size"), 0);
        Integer height = getOrDefault(photo.getInt("height"), 0);
        Integer width = getOrDefault(photo.getInt("width"), 0);
        return new FileItem(fileId, chatId, fileName, mimeType, fileSize, date, PHOTO, height, width, 0);
    }

    private static FileItem video(String chatId, Date date, Message video) {
        String fileId = video.getString("file_id");
        String mimeType = getOrDefault(video.getString("mime_type"), VIDEO_MP4);
        String fileName = getOrDefault(video.getString("file_name"), generateFileName(mimeType, date));
        Integer fileSize = getOrDefault(video.getInt("file_size"), 0);
        Integer height = getOrDefault(video.getInt("height"), 0);
        Integer width = getOrDefault(video.getInt("width"), 0);
        Integer duration = getOrDefault(video.getInt("duration"), 0);
        return new FileItem(fileId, chatId, fileName, mimeType, fileSize, date, VIDEO, height, width, duration);
    }

    private static FileItem audio(String chatId, Date date, Message audio) {
        String fileId = audio.getString("file_id");
        String mimeType = getOrDefault(audio.getString("mime_type"), AUDIO_AAC);
        String fileName = getOrDefault(audio.getString("file_name"), generateFileName(mimeType, date));
        Integer fileSize = getOrDefault(audio.getInt("file_size"), 0);
        Integer duration = getOrDefault(audio.getInt("duration"), 0);
        return new FileItem(fileId, chatId, fileName, mimeType, fileSize, date, AUDIO, 0, 0, duration);
    }

    private static FileItem document(String chatId, Date date, Message document) {
        String fileId = document.getString("file_id");
        String mimeType = getOrDefault(document.getString("mime_type"), APPLICATION_OCTET_STREAM);
        String fileName = getOrDefault(document.getString("file_name"), generateFileName(mimeType, date));
        Integer fileSize = getOrDefault(document.getInt("file_size"), 0);
        Integer height = getOrDefault(document.getInt("height"), 0);
        Integer width = getOrDefault(document.getInt("width"), 0);
        Integer duration = getOrDefault(document.getInt("duration"), 0);
        return new FileItem(fileId, chatId, fileName, mimeType, fileSize, date, DOCUMENT, height, width, duration);
    }

    private static String generateFileName(String mimeType, Date date) {
        StringBuilder sb = new StringBuilder();
        if (mimeType.startsWith("image")) {
            sb.append("IMG");
        } else if (mimeType.startsWith("audio")) {
            if (mimeType.endsWith("aac")) {
                sb.append("AUD");
            } else {
                sb.append("MP3");
            }
        } else if (mimeType.startsWith("video")) {
            sb.append("VID");
        } else if (mimeType.startsWith("text")) {
            sb.append("TXT");
        } else {
            sb.append("DOC");
        }
        return sb.append("_").append(DATE_FORMAT.format(date)).toString();
    }

    private static <T> T getOrDefault(T t, T def) {
        return t == null ? def : t;
    }

}

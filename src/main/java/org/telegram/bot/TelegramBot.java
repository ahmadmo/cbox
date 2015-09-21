package org.telegram.bot;

import org.telegram.bot.messaging.Message;

/**
 * @author ahmad
 */
public interface TelegramBot extends Runnable {

    String token();

    Message sendMessage(String chatId, String text);

    Message sendMessage(String chatId, String text, String replyTo, String replyMarkup);

    Message sendPhoto(String chatId, String fileId);

    Message sendPhoto(String chatId, String fileId, String replyTo, String replyMarkup);

    Message sendAudio(String chatId, String fileId);

    Message sendAudio(String chatId, String fileId, String replyTo, String replyMarkup);

    Message sendDocument(String chatId, String fileId);

    Message sendDocument(String chatId, String fileId, String replyTo, String replyMarkup);

    Message sendVideo(String chatId, String fileId);

    Message sendVideo(String chatId, String fileId, String replyTo, String replyMarkup);

    Message sendChatAction(String chatId, String action);

    Message getUpdates();

    Message getUpdates(String offset);

    Message getUpdates(String offset, String limit, String timeout);

    void stop();

}

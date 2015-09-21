package org.telegram.bot;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.telegram.bot.logging.Log4j;
import org.telegram.bot.messaging.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import static org.telegram.bot.messaging.Methods.*;

/**
 * @author ahmad
 */
public abstract class AbstractTelegramBot implements TelegramBot {

    @Override
    public Message sendMessage(String chatId, String text) {
        return sendMessage(chatId, text, null, null);
    }

    @Override
    public Message sendMessage(String chatId, String text, String replyTo, String replyMarkup) {
        Objects.requireNonNull(chatId);
        Objects.requireNonNull(text);
        HttpPost httpPost = new TelegramRequestBuilder(token(), SEND_MESSAGE)
                .chatId(chatId)
                .text(text)
                .replyTo(replyTo)
                .replyMarkup(replyMarkup)
                .build();
        return post(httpPost);
    }

    @Override
    public Message sendPhoto(String chatId, String fileId) {
        return sendPhoto(chatId, fileId, null, null);
    }

    @Override
    public Message sendPhoto(String chatId, String fileId, String replyTo, String replyMarkup) {
        Objects.requireNonNull(chatId);
        Objects.requireNonNull(fileId);
        HttpPost httpPost = new TelegramRequestBuilder(token(), SEND_PHOTO)
                .chatId(chatId)
                .photo(fileId)
                .replyTo(replyTo)
                .replyMarkup(replyMarkup)
                .build();
        return post(httpPost);
    }

    @Override
    public Message sendAudio(String chatId, String fileId) {
        return sendAudio(chatId, fileId, null, null);
    }

    @Override
    public Message sendAudio(String chatId, String fileId, String replyTo, String replyMarkup) {
        Objects.requireNonNull(chatId);
        Objects.requireNonNull(fileId);
        HttpPost httpPost = new TelegramRequestBuilder(token(), SEND_AUDIO)
                .chatId(chatId)
                .audio(fileId)
                .replyTo(replyTo)
                .replyMarkup(replyMarkup)
                .build();
        return post(httpPost);
    }

    @Override
    public Message sendDocument(String chatId, String fileId) {
        return sendDocument(chatId, fileId, null, null);
    }

    @Override
    public Message sendDocument(String chatId, String fileId, String replyTo, String replyMarkup) {
        Objects.requireNonNull(chatId);
        Objects.requireNonNull(fileId);
        HttpPost httpPost = new TelegramRequestBuilder(token(), SEND_DOCUMENT)
                .chatId(chatId)
                .document(fileId)
                .replyTo(replyTo)
                .replyMarkup(replyMarkup)
                .build();
        return post(httpPost);
    }

    @Override
    public Message sendVideo(String chatId, String fileId) {
        return sendVideo(chatId, fileId, null, null);
    }

    @Override
    public Message sendVideo(String chatId, String fileId, String replyTo, String replyMarkup) {
        Objects.requireNonNull(chatId);
        Objects.requireNonNull(fileId);
        HttpPost httpPost = new TelegramRequestBuilder(token(), SEND_VIDEO)
                .chatId(chatId)
                .video(fileId)
                .replyTo(replyTo)
                .replyMarkup(replyMarkup)
                .build();
        return post(httpPost);
    }

    @Override
    public Message sendChatAction(String chatId, String action) {
        Objects.requireNonNull(chatId);
        Objects.requireNonNull(action);
        HttpPost httpPost = new TelegramRequestBuilder(token(), SEND_CHAT_ACTION)
                .chatId(chatId)
                .action(action)
                .build();
        return post(httpPost);
    }

    @Override
    public Message getUpdates() {
        return getUpdates(null, null, null);
    }

    @Override
    public Message getUpdates(String offset) {
        return getUpdates(offset, null, null);
    }

    @Override
    public Message getUpdates(String offset, String limit, String timeout) {
        HttpPost httpPost = new TelegramRequestBuilder(token(), GET_UPDATES)
                .offset(offset)
                .limit(limit)
                .timeout(timeout)
                .build();
        return post(httpPost);
    }

    private static Message post(HttpPost httpPost) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {
            HttpEntity responseEntity = response.getEntity();
            StringBuilder json = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
            }
            consume(responseEntity);
            return Message.decode(json.toString());
        } catch (IOException e) {
            Log4j.BOT.error(e.getMessage(), e);
        } finally {
            consume(httpPost.getEntity());
        }
        return null;
    }

    private static void consume(HttpEntity entity) {
        try {
            EntityUtils.consume(entity);
        } catch (IOException e) {
            Log4j.BOT.error(e.getMessage(), e);
        }
    }

}

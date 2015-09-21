package org.telegram.bot;

import org.apache.http.Consts;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.telegram.bot.util.concurrent.BiConsumer;
import org.telegram.bot.util.concurrent.MapUtil;

import java.util.HashMap;
import java.util.Map;

import static org.telegram.bot.messaging.Params.*;

/**
 * @author ahmad
 */
public final class TelegramRequestBuilder {

    private static final String URI_FORMAT = "https://api.telegram.org/bot%s/%s";
    private static final ContentType TEXT_PLAIN_UTF8 = ContentType.create("text/plain", Consts.UTF_8);

    private final HttpPost httpPost;
    private final Map<String, StringBody> params = new HashMap<>();

    public TelegramRequestBuilder(String token, String method) {
        httpPost = new HttpPost(String.format(URI_FORMAT, token, method));
    }

    public TelegramRequestBuilder chatId(String chatId) {
        addParam(CHAT_ID, chatId);
        return this;
    }

    public TelegramRequestBuilder text(String text) {
        addParam(TEXT, text);
        return this;
    }

    public TelegramRequestBuilder photo(String photo) {
        addParam(PHOTO, photo);
        return this;
    }

    public TelegramRequestBuilder audio(String audio) {
        addParam(AUDIO, audio);
        return this;
    }

    public TelegramRequestBuilder document(String document) {
        addParam(DOCUMENT, document);
        return this;
    }

    public TelegramRequestBuilder video(String video) {
        addParam(VIDEO, video);
        return this;
    }

    public TelegramRequestBuilder action(String action) {
        addParam(ACTION, action);
        return this;
    }

    public TelegramRequestBuilder replyTo(String replyTo) {
        addParam(REPLY_TO_MESSAGE_ID, replyTo);
        return this;
    }

    public TelegramRequestBuilder replyMarkup(String replyMarkup) {
        addParam(REPLY_MARKUP, replyMarkup);
        return this;
    }

    public TelegramRequestBuilder offset(String offset) {
        addParam(OFFSET, offset);
        return this;
    }

    public TelegramRequestBuilder limit(String limit) {
        addParam(LIMIT, limit);
        return this;
    }

    public TelegramRequestBuilder timeout(String timeout) {
        addParam(TIMEOUT, timeout);
        return this;
    }

    private void addParam(String name, String value) {
        if (value != null) {
            StringBody body = new StringBody(value, TEXT_PLAIN_UTF8);
            params.put(name, body);
        }
    }

    public HttpPost build() {
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        MapUtil.forEach(params, new BiConsumer<String, StringBody>() {
            @Override
            public void accept(String name, StringBody body) {
                builder.addPart(name, body);
            }
        });
        httpPost.setEntity(builder.build());
        return httpPost;
    }

}

package org.telegram.bot.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ahmad
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Message extends HashMap<String, Object> {

    private static final long serialVersionUID = -1033680170557854942L;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectWriter WRITER = MAPPER.writerFor(Message.class);
    private static final ObjectReader READER = MAPPER.reader(Message.class);

    public Message() {
        super();
    }

    public Message(Map<String, Object> map) {
        super(map);
    }

    public String getString(String key) {
        Object o = get(key);
        return o == null || !(o instanceof String) ? null : (String) o;
    }

    public Integer getInt(String key) {
        Object o = get(key);
        return o == null || !(o instanceof Integer) ? null : (Integer) o;
    }

    public Long getLong(String key) {
        Object o = get(key);
        return o == null || !(o instanceof Long) ? null : (Long) o;
    }

    public Float getFloat(String key) {
        Object o = get(key);
        return o == null || !(o instanceof Float) ? null : (Float) o;
    }

    public Double getDouble(String key) {
        Object o = get(key);
        return o == null || !(o instanceof Double) ? null : (Double) o;
    }

    public Boolean getBoolean(String key) {
        Object o = get(key);
        return o == null || !(o instanceof Boolean) ? null : (Boolean) o;
    }

    public Date getDate(String key) {
        Object o = get(key);
        return o == null ? null
                : o instanceof Date ? (Date) o
                : o instanceof Long ? new Date(Long.parseLong(String.valueOf(o)))
                : o instanceof Integer ? new Date(Integer.parseInt(String.valueOf(o)) * 1000L) //unix-time
                : null;
    }

    public List getList(String key) {
        Object o = get(key);
        return o == null || !(o instanceof List) ? null : (List) o;
    }

    public Map getMap(String key) {
        Object o = get(key);
        return o == null || !(o instanceof Map) ? null : (Map) o;
    }

    @SuppressWarnings("unchecked")
    public static Message toMessage(Object o) {
        if (o != null && o instanceof Map) {
            return new Message((Map<String, Object>) o);
        }
        return null;
    }

    public static String encode(Message message) throws JsonProcessingException {
        return WRITER.writeValueAsString(message);
    }

    public static Message decode(String jsonString) throws IOException {
        return READER.readValue(jsonString);
    }

}

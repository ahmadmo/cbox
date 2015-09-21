package org.telegram.bot.cbox;

import org.telegram.bot.cbox.model.SendType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author ahmad
 */
public final class FileFilter {

    private static final Pattern FILTER_PATTERN = Pattern.compile("[1-4]+");
    private static final Pattern REPLACE_SPACE_PATTERN = Pattern.compile("\\s+");

    private final boolean[] filters = new boolean[SendType.values().length];

    public FileFilter() {
        noFilters();
    }

    public void setFilters(SendType... types) {
        Arrays.fill(filters, false);
        for (SendType type : types) {
            filters[type.ordinal()] = true;
        }
    }

    public SendType[] getFilters() {
        List<SendType> types = new ArrayList<>();
        for (SendType type : SendType.values()) {
            if (filters[type.ordinal()]) {
                types.add(type);
            }
        }
        return types.toArray(new SendType[types.size()]);
    }

    public boolean doFilter(SendType type) {
        return filters[type.ordinal()];
    }

    public void noFilters() {
        Arrays.fill(filters, true);
    }

    public static SendType[] parse(String query) {
        query = REPLACE_SPACE_PATTERN.matcher(query).replaceAll("");
        if (FILTER_PATTERN.matcher(query).matches()) {
            List<SendType> types = new ArrayList<>();
            for (char c : query.toCharArray()) {
                types.add(SendType.values()[Character.getNumericValue(c) - 1]);
            }
            return types.toArray(new SendType[types.size()]);
        } else if (query.equalsIgnoreCase("noFilters") || query.equalsIgnoreCase("noFilter")) {
            return SendType.values();
        }
        return null;
    }

}

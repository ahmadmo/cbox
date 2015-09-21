package org.telegram.bot.cbox;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author ahmad
 */
public final class SearchQueryHelper {

    private static final String SEARCH_DATE_FORMAT = "%s - %s";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
    private static final Pattern SPLIT_PATTERN = Pattern.compile("-");

    private SearchQueryHelper() {
    }

    public static String searchDateExample() {
        Calendar cal = Calendar.getInstance();
        String to = DATE_FORMAT.format(cal.getTime());
        cal.add(Calendar.DATE, -1);
        String from = DATE_FORMAT.format(cal.getTime());
        return String.format(SEARCH_DATE_FORMAT, from, to);
    }

    public static String removeExtension(String fileName) {
        int dotIdx = fileName.lastIndexOf('.');
        if (dotIdx != -1) {
            fileName = fileName.substring(0, dotIdx);
        }
        return fileName;
    }

    public static Date[] parseDates(String query) {
        String[] segments = SPLIT_PATTERN.split(query);
        List<Date> dates = new ArrayList<>();
        for (String s : segments) {
            Date date = parse(s.trim());
            if (date != null) {
                dates.add(date);
            }
        }
        return dates.toArray(new Date[dates.size()]);
    }

    private static Date parse(String source) {
        if (source.equalsIgnoreCase("today")) {
            return new Date();
        }
        if (source.equalsIgnoreCase("yesterday")) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            return cal.getTime();
        }
        try {
            return DATE_FORMAT.parse(source);
        } catch (ParseException e) {
            return null;
        }
    }

}

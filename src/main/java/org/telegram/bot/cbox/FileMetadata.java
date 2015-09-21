package org.telegram.bot.cbox;

import org.telegram.bot.cbox.model.FileItem;

import java.text.SimpleDateFormat;

/**
 * @author ahmad
 */
public final class FileMetadata {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private static final int MINUTES_IN_AN_HOUR = 60;
    private static final int SECONDS_IN_A_MINUTE = 60;

    private FileMetadata() {
    }

    public static String getMetadata(FileItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append("name = ").append(item.getFileName()).append("\n")
                .append("date posted = ").append(DATE_FORMAT.format(item.getDate())).append("\n")
                .append("mime type = ").append(item.getMimeType()).append("\n")
                .append("size = ").append(standardFileSize(item.getFileSize()));
        int width = item.getWidth(), height = item.getHeight();
        if (width > 0 && height > 0) {
            sb.append("\n").append("dimension = ").append(width).append(" * ").append(height);
        }
        int duration = item.getDuration();
        if (duration > 0) {
            sb.append("\n").append("duration = ").append(formatDuration(duration));
        }
        return sb.toString();
    }

    public static String standardFileSize(int bytes) {
        StringBuilder sb = new StringBuilder();
        if (bytes > 1024) {
            double kiloBytes = (bytes / 1024.0);
            if (kiloBytes > 1024) {
                double megaBytes = (kiloBytes / 1024);
                if (megaBytes > 1024) {
                    double gigaBytes = (megaBytes / 1024);
                    sb.append(twoTenthDigit(gigaBytes)).append(" GB");
                } else {
                    sb.append(twoTenthDigit(megaBytes)).append(" MB");
                }
            } else {
                sb.append(twoTenthDigit(kiloBytes)).append(" KB");
            }
        } else {
            sb.append(twoTenthDigit(bytes)).append(" Bytes");
        }
        return String.valueOf(sb);
    }

    private static float twoTenthDigit(double a) {
        return (float) (Math.round(a * 100.0) / 100.0);
    }

    private static String formatDuration(int seconds) {
        int seconds1 = seconds % SECONDS_IN_A_MINUTE;
        int totalMinutes = seconds / SECONDS_IN_A_MINUTE;
        int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        int hours = totalMinutes / MINUTES_IN_AN_HOUR;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds1);
        }
        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds1);
        }
        return String.format("%d sec", seconds1);
    }

}

package org.telegram.bot.cbox;

import org.telegram.bot.cbox.model.FileItem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ahmad
 */
public final class FileVersionHolder {

    private static final String FILE_NAME_FORMAT = "%s %s";
    private static final String FILE_VERSION_FORMAT = "%s (%d)";

    private final ConcurrentHashMap<FileVersion, String> filesIds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> versions = new ConcurrentHashMap<>();

    public String getFileId(String fileNameVersion) {
        return filesIds.get(detectVersion(fileNameVersion));
    }

    public String getFileNameVersion(FileItem fileItem, String icon) {
        String fileName = String.format(FILE_NAME_FORMAT, icon, fileItem.getFileName().trim());
        int version = nextVersion(fileName);
        filesIds.put(new FileVersion(fileName, version), fileItem.getFileId());
        return version == 0
                ? fileName
                : String.format(FILE_VERSION_FORMAT, fileName.trim(), version);
    }

    public void clear() {
        filesIds.clear();
        versions.clear();
    }

    private int nextVersion(String fileName) {
        AtomicInteger version = versions.get(fileName);
        if (version == null) {
            final AtomicInteger v = versions.putIfAbsent(fileName, version = new AtomicInteger(0));
            if (v != null) {
                version = v;
            }
        }
        return version.getAndIncrement();
    }

    private static FileVersion detectVersion(String fileName) {
        fileName = fileName.trim();
        int rp = fileName.lastIndexOf(')');
        if (rp == fileName.length() - 1) {
            StringBuilder version = new StringBuilder();
            int lp = 0;
            for (int i = rp - 1; i > 0; i--) {
                if (fileName.charAt(i) == '(') {
                    lp = i;
                    break;
                }
                version.insert(0, fileName.charAt(i));
            }
            try {
                return new FileVersion(fileName.substring(0, lp).trim(), Integer.parseInt(version.toString()));
            } catch (Exception ignored) {
            }
        }
        return new FileVersion(fileName, 0);
    }

}

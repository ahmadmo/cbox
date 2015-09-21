package org.telegram.bot.cbox;

import org.telegram.bot.cbox.model.FileItem;
import org.telegram.bot.messaging.KeyboardBuilder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.telegram.bot.cbox.Constants.*;

/**
 * @author ahmad
 */
public final class FileItemListBuilder {

    private static final String DETAILS_FORMAT = "%d %s %s %n";

    public static FileItemList build(FileVersionHolder versionHolder, FileFilter filter, List<FileItem> items) {
        int total = 0, photos = 0, videos = 0, audios = 0, musics = 0, files = 0;
        KeyboardBuilder kb = new KeyboardBuilder().resize(true).selective(true);
        Collections.sort(items, new Comparator<FileItem>() {
            @Override
            public int compare(FileItem o1, FileItem o2) {
                return o1.compareToDesc(o2);
            }
        });
        for (FileItem item : items) {
            if (filter.doFilter(item.getSendType())) {
                String icon = matchIcon(item.getMimeType().toLowerCase());
                switch (icon) {
                    case PHOTO_ICON:
                        photos++;
                        break;
                    case VIDEO_ICON:
                        videos++;
                        break;
                    case AUDIO_ICON:
                        audios++;
                        break;
                    case MUSIC_ICON:
                        musics++;
                        break;
                    case TEXT_ICON:
                    case DOCUMENT_ICON:
                        files++;
                }
                kb.addRow(versionHolder.getFileNameVersion(item, icon));
                total++;
            }
        }
        if (total == 0) {
            return new FileItemList(null, null, 0);
        }
        kb.addRow("delete all");
        StringBuilder sb = new StringBuilder();
        if (photos > 0) {
            sb.append(String.format(DETAILS_FORMAT, photos, photos > 1 ? "photos" : "photo", PHOTO_ICON));
        }
        if (videos > 0) {
            sb.append(String.format(DETAILS_FORMAT, videos, videos > 1 ? "video files" : "video file", VIDEO_ICON));
        }
        if (musics > 0) {
            sb.append(String.format(DETAILS_FORMAT, musics, musics > 1 ? "musics" : "music", MUSIC_ICON));
        }
        if (files > 0) {
            sb.append(String.format(DETAILS_FORMAT, files, files > 1 ? "files" : "file", DOCUMENT_ICON));
        }
        if (audios > 0) {
            sb.append(String.format(DETAILS_FORMAT, audios, audios > 1 ? "voice messages" : "voice message", AUDIO_ICON));
        }
        return new FileItemList(sb.toString().trim(), kb.build(), total);
    }

    private static String matchIcon(String mimeType) {
        return mimeType.startsWith("image") ? PHOTO_ICON
                : mimeType.startsWith("audio") ? mimeType.endsWith("aac") ? AUDIO_ICON : MUSIC_ICON
                : mimeType.startsWith("video") ? VIDEO_ICON
                : mimeType.startsWith("text") ? TEXT_ICON
                : DOCUMENT_ICON;
    }

}

package org.telegram.bot.cbox.bl;

import org.telegram.bot.cbox.FileFilter;
import org.telegram.bot.cbox.model.Chat;
import org.telegram.bot.cbox.model.FileItem;

import java.util.Date;
import java.util.List;

/**
 * @author ahmad
 */
public interface IChatRepository extends IRepository<Chat> {

    boolean delete(Chat chat, FileFilter filter);

    List<FileItem> searchByName(String chatId, String fileName);

    List<FileItem> searchPeriod(String chatId, Date from, Date to);

    String randomFileName(String chatId);

}

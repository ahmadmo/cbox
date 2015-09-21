package org.telegram.bot.cbox.bl;

import org.telegram.bot.cbox.model.FileItem;

import java.util.List;

/**
 * @author ahmad
 */
public interface IFileItemRepository extends IRepository<FileItem>, RowMapper<FileItem> {

    boolean save(List<FileItem> fileItems);

}

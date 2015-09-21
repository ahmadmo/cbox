package org.telegram.bot.cbox.bl;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author ahmad
 */
public interface RowMapper<E> {

    E mapRow(ResultSet rs) throws SQLException;

}

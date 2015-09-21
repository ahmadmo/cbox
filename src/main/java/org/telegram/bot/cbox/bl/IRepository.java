package org.telegram.bot.cbox.bl;

import java.util.List;

/**
 * @author ahmad
 */
public interface IRepository<E> {

    boolean save(E e);

    E get(String id);

    boolean delete(E e);

    List<E> loadAll();

}

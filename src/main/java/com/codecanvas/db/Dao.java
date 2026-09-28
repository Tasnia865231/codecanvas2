package com.codecanvas.db;

import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object interface satisfying academic OOP requirements.
 *
 * @param <T> The domain entity type
 */
public interface Dao<T> {
    Optional<T> findById(Object id);
    List<T> findAll();
    boolean insert(T entity);
    boolean update(T entity);
    boolean delete(Object id);
}

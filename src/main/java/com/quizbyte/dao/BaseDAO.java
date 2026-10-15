package com.quizbyte.dao;

import com.quizbyte.exception.DatabaseOperationException;

import java.util.List;
import java.util.Optional;

/**
 * Generic Data Access Object (DAO) contract interface.
 * Demonstrates Generics and Interface abstraction according to the university rubric.
 *
 * @param <T> Entity type managed by this DAO
 */
public interface BaseDAO<T> {

    /**
     * Persists an entity to the relational database.
     *
     * @param entity entity object to insert or save
     * @return true if operation was successful, false otherwise
     * @throws DatabaseOperationException if a database access error occurs
     */
    boolean save(T entity) throws DatabaseOperationException;

    /**
     * Locates an entity by its primary key identifier.
     *
     * @param id numeric unique ID
     * @return Optional wrapping the entity if present, or Optional.empty()
     * @throws DatabaseOperationException if a database access error occurs
     */
    Optional<T> findById(int id) throws DatabaseOperationException;

    /**
     * Retrieves all records of entity type T.
     *
     * @return List of all entities
     * @throws DatabaseOperationException if a database access error occurs
     */
    List<T> findAll() throws DatabaseOperationException;
}

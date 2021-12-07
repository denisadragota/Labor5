package com.company.Repository;

import com.company.Exceptions.NullException;

import java.sql.SQLException;
import java.util.List;

/**
 * CRUD operations repository interface
 *
 * @author Denisa Dragota
 * @version 29.11.2021
 */
public interface ICrudRepository<T> {
    /**
     * finds the entity with the give id from the repository
     *
     * @param id -the id of the entity to be returned id must not be null
     * @return the entity with the specified id or null - if there is no entity with the given id
     * @throws NullException if input parameter id is NULL
     * @throws SQLException  if connection to database could not succeed
     */
    T findOne(Long id) throws NullException, SQLException;

    /**
     * retrieves all entities from the repository
     *
     * @return all entities
     * @throws SQLException if connection to database could not succeed
     */
    List<T> findAll() throws SQLException;

    /**
     * adds an entity in the repository
     *
     * @param obj entity must be not null
     * @return null- if the given entity is saved otherwise returns the entity (id already exists)
     * @throws NullException if input parameter entity obj is NULL
     * @throws SQLException  if connection to database could not succeed
     */
    T save(T obj) throws NullException, SQLException;

    /**
     * updates an entity from the repository
     *
     * @param obj entity must not be null
     * @return null - if the entity is updated, otherwise returns the entity - (e.g id does not exist).
     * @throws NullException if input parameter entity obj is NULL
     * @throws SQLException  if connection to database could not succeed
     */
    T update(T obj) throws NullException, SQLException;

    /**
     * removes the entity with the specified id from the repository
     *
     * @param id id must be not null
     * @return the removed entity or null if there is no entity with the given id
     * @throws NullException if input parameter id is NULL
     * @throws SQLException  if connection to database could not succeed
     */
    T delete(Long id) throws NullException, SQLException;
}


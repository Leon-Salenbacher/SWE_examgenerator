package service.elements;

import models.DataObject;

import java.util.List;
import java.util.NoSuchElementException;


/**
 * Generic service API for CRUD operations on domain objects.
 *
 * @param <T> domain object type
 * @param <CMD> parameter object used for create and update operations
 */
public interface DataObjectService<
        T extends DataObject,
        CMD //command (parameter object for create and update)
        > {

    /**
     * Finds one object by id.
     *
     * @param id unique object id
     * @return matching object
     * @throws NoSuchElementException if no object exists for the id
     */
    T getById(int id) throws NoSuchElementException;

    /**
     * @return all objects known to the service
     */
    List<T> getAll();

    /**
     * Creates and persists an object from a command object.
     *
     * @param command input values for the new object
     * @return created object
     */
    T create(CMD command);

    /**
     * Updates an existing object from a command object.
     *
     * @param id id of the object to update
     * @param command replacement values
     * @return updated object
     */
    T update(int id, CMD command);

    /**
     * Deletes an object by id.
     *
     * @param id id of the object to delete
     * @throws NoSuchElementException if no object exists for the id
     */
    void delete(int id) throws NoSuchElementException;
}

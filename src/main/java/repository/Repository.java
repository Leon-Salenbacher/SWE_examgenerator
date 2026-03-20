package repository;

import objects.DataObject;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Generic persistence contract for {@link DataObject} instances.
 *
 * @param <T> entity type handled by the repository
 */
public interface Repository <T extends DataObject> {

    /**
     * The label of the attribute field id
     */
    String ID_ATTRIBUTE_NAME = "id";

    /**
     * The label of the attribute field title
     */
    String TITLE_ATTRIBUTE_NAME = "title";

    /**
     * Finds the target Entity by the id, and returns an {@link Optional}
     * Entity.
     *
     * @param id the id of the Entity
     * @return Returns the target Element {@see T}
      */
    Optional<T> findById(int id);

    /**
     * Finds all target Entities, that exist in the DB.
     * @return
     */
    List<T> findAll();

    /**
     * Persistent the given object.
     * @param object
     * @return
     */
    T save(T object);

    /**
     * Updates the given object values of the given Entity.
     * @param object the entity with new values.
     * @return Returns the updated entity
     * @throws NoSuchElementException the given Object doesn't exist as a persistent Entity
     */
    T update(T object) throws NoSuchElementException;

    /**
     * Deletes a persistent Entity by the given id.
     * @param id the id of the entity.
     */
    void deleteById(int id);
}

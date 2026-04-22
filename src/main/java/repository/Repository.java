package repository;

import models.DataObject;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Generic persistence contract for {@link DataObject} instances.
 *
 * <p>
 *     Implementations encapsulate how entities are loaded from and written to the
 *     underlying storage technology. The interface intentionally models only the
 *     minimal CRUD operations that are shared by all repositories in this package.
 *     More specialised repository contracts may extend this interface with
 *     aggregate-specific read or write operations.
 * </p>
 *
 * @param <T> entity type handled by the repository
 */
public interface Repository<T extends DataObject> {

    /**
     * Looks up a single entity by its identifier.
     *
     * @param id unique identifier of the entity to resolve
     * @return an {@link Optional} containing the matching entity or an empty
     * optional when no entity with the given id exists.
      */
    Optional<T> findById(int id);

    /**
     * Loads every entity currently persisted for the repository's element type.
     *
     * @return immutable or mutable list of all known entities, depending on the
     * concrete implementation
     */
    List<T> findAll();

    /**
     * Persists a new entity instance.
     *
     * <p>
     *     Implementations usually append the object to the configured storage and
     *     return the same instance for fluent usage.
     * </p>
     *
     * @param object entity to persist
     * @return the persisted entity instance.
     */
    T save(T object);

    /**
     * Replace the persisted state of an already existing entity.
     *
     * @param object entity containing the new values that should be stored.
     * @return the updated entity instance
     * @throws NoSuchElementException if the entity does not yet exist in the persistent storage.
     */
    T update(T object) throws NoSuchElementException;

    /**
     * Removes the entity with the given identifier from persistent storage.
     *
     * @param id unique identifier of the entity that should be deleted.
     * @throws NoSuchElementException if the entity with the identifier doesn't exist.
     */
    void deleteById(int id) throws NoSuchElementException;
}

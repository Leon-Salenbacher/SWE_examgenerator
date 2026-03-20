package repository;

import objects.ChildObject;
import objects.ParentObject;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Repository contract for aggreagte roots that expose nested child objects.
 * Child persistence remains repository-specific, while read access to nested children is kept here for convenience.
 */

/**
 * Repository contract for aggregate roots that own nested child objects.
 *
 * @param <T> Entity type handled by the repository.
 * @param <C> nested child type stored inside the aggregate
 */
public interface ParentRepository<
            T extends ParentObject<C>,
            C extends ChildObject>
        extends ChildRepository<T>{

    /**
     * Return all child objects that are nested under the given parent.
     *
     * @param id identifier of the parent aggregate
     * @return list of nested child objects; implementations may return an empty list
     *          if the parent has no children.
     * @throws NoSuchElementException if no parent with the given identifier exist.
     */
    List<C> findAllChildren(int id) throws NoSuchElementException;

}

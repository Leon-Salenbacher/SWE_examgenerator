package repository;

import objects.ChildObject;
import objects.ParentObject;

import java.util.List;

/**
 * Repository contract for aggreagte roots that expose nested child objects.
 * Child persistence remains repository-specific, while read access to nested children is kept here for convenience.
 */
public interface ParentRepository<
            T extends ParentObject<C>,
            C extends ChildObject>
        extends ChildRepository<T>{

    /**
     * Return the nested child objects currently stored under the given parent.
     *
     * @param id the ID of the {@link T} entity
     * @return a List of all {@link C} entities nested in the parent entity.
     */
    List<C> findAllChildren(int id);

}

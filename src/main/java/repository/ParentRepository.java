package repository;

import objects.ChildObject;
import objects.ParentObject;

import java.util.List;

/**
 * The Repository to manage the persistence of {@link ParentObject}s.
 * @param <T> the target Entity
 * @param <C> the {@link ChildObject} of the {@link ParentObject}
 */
public interface ParentRepository<
            T extends ParentObject<C>,
            C extends ChildObject>
        extends ChildRepository<T>{

    /**
     * Persistent a {@link C} to the {@link T} with the given id.
     *
     * @param id the id of the {@link T}
     * @param child the {@link C}, that should be added to the {@link T}.
     * @return the updated {@link T}.
     */
    T addChild(int id, C child);

    /**
     * Finds all {@link C}s of the {@link T} with the given id.
     *
     * @param id the ID of the {@link T} entity.
     * @return a List of all {@link C}
     */
    List<C> findAllChilds(int id);

    /**
     * Delete a {@link C} from the {@link T} entity with the given ID
     * @param id The ID of the {@link T}
     * @param childId The ID of the {@link C} that should be deleted.
     * @return the updated {@link T} entity.
     */
    T deleteChild(int id, int childId);

}

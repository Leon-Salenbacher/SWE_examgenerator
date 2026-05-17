package service.elements;

import models.ChildObject;

/**
 * Service API for leaf objects that can be edited independently.
 *
 * @param <T> child object type
 * @param <C> command type for create and update operations
 */
public interface ChildService<
            T extends ChildObject,
            C extends ChildService.ChildCommand
        > extends DataObjectService<T, C> {

    /**
     * Common command contract for child objects.
     */
    interface ChildCommand{
        /**
         * @return title value to store on the object
         */
        String title();

        /**
         * Optional parent id for assigning this child to its parent aggregate during creation.
         * For root objects this value can be {@code null}.
         *
         * @return parent id or {@code null}
         */
        Integer parentId();
    }
}

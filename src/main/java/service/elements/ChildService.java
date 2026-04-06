package service.elements;

import models.ChildObject;

public interface ChildService<
            T extends ChildObject,
            C extends ChildService.ChildCommand
        > extends DataObjectService<T, C> {

    interface ChildCommand{
        String title();

        /**
         * Optional parent id for assigning this child to its parent aggregate during creation.
         * For root objects this value can be {@code null}.
         */
        Integer parentId();
    }
}

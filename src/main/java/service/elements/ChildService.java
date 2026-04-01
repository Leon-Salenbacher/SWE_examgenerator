package service.elements;

import objects.ChildObject;

public interface ChildService<
            T extends ChildObject,
            C extends ChildService.ChildCommand
        > extends DataObjectService<T, C> {
    interface ChildCommand{
        String title();
    }
}

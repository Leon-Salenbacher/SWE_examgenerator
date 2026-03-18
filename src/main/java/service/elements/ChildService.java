package service.elements;

import objects.ChildObject;

public interface ChildService<T extends ChildObject> extends DataObjectService<T> {
    public T create();

    public T update();

    public void delete();

}

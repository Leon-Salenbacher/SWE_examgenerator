package repository;

import objects.ChildObject;
import objects.ParentObject;

import java.util.List;

public interface ParentRepository<
            T extends ParentObject<C>,
            C extends ChildObject>
        extends Repository<T>{

    public T addChild(C child);
    public List<C> getChilds(String id);
    public T removeChild(String id, String childId);

}

package repository;

import objects.ChildObject;
import objects.ParentObject;

import java.util.List;

public interface ParentRepository<
            T extends ParentObject<C>,
            C extends ChildObject>
        extends Repository<T>{



    public T addChild(int id, C child);
    public List<C> getChilds(int id);
    public T removeChild(int id, int childId);

}

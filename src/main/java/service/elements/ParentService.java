package service.elements;

import objects.ChildObject;
import objects.ParentObject;

public interface ParentService<
            T extends ParentObject<?>,
            C extends ChildObject
        > extends DataObjectService<T,ParentService.ParentCommand>{



    interface ParentCommand{
        String title();
    }
}

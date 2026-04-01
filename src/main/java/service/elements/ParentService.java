package service.elements;

import objects.ChildObject;
import objects.ParentObject;

import java.util.List;
import java.util.NoSuchElementException;

public interface ParentService<
            P extends ParentObject<?>,
            CH extends ChildObject,
            CMD extends ParentService.ParentCommand
        > extends DataObjectService<P, CMD>{

    int countChildren(int id) throws NoSuchElementException;

    List<CH> getChildren(int parentId) throws NoSuchElementException;

    interface ParentCommand extends ChildService.ChildCommand {
        String title();
    }
}

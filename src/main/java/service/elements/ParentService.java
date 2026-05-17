package service.elements;

import models.ChildObject;
import models.ParentObject;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service API for parent objects that own nested child elements.
 *
 * @param <P> parent object type
 * @param <CH> child object type
 * @param <CMD> command type for parent create and update operations
 */
public interface ParentService<
            P extends ParentObject<?>,
            CH extends ChildObject,
            CMD extends ParentService.ParentCommand
        > extends DataObjectService<P, CMD>{

    /**
     * Counts nested children for a parent id.
     *
     * @param id parent id
     * @return number of nested children
     * @throws NoSuchElementException if no parent exists for the id
     */
    int countChildren(int id) throws NoSuchElementException;

    /**
     * Loads nested children for a parent id.
     *
     * @param parentId parent id
     * @return nested child objects
     * @throws NoSuchElementException if no parent exists for the id
     */
    List<CH> getChildren(int parentId) throws NoSuchElementException;

    /**
     * Common command contract for objects with a title and optional parent id.
     */
    interface ParentCommand extends ChildService.ChildCommand {
        String title();
    }
}

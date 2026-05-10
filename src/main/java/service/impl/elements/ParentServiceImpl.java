package service.impl.elements;

import models.ChildObject;
import models.ParentObject;
import repository.ParentRepository;
import service.elements.ParentService;
import service.impl.DataObjectServiceImpl;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Base service implementation for parent aggregates with nested children.
 *
 * @param <P> parent object type
 * @param <CH> child object type
 * @param <CMD> command type
 */
public abstract class ParentServiceImpl<
            P extends ParentObject<CH>,
            CH extends ChildObject,
            CMD extends ParentService.ParentCommand
        > extends DataObjectServiceImpl<P, CMD>
        implements ParentService<P, CH, CMD>{

    private final ParentRepository<P, CH> repository;

    protected ParentServiceImpl(ParentRepository<P, CH> repository){
        super(repository);
        this.repository = repository;
    }

    /**
     * Counts the children of a parent aggregate.
     *
     * @param id parent id
     * @return child count
     * @throws NoSuchElementException if no parent exists for the id
     */
    @Override
    public int countChildren(int id) throws NoSuchElementException {
        //TODO implement
        return 0;
    }

    /**
     * Loads the children stored under a parent aggregate.
     *
     * @param parentId parent id
     * @return nested child objects
     */
    @Override
    public List<CH> getChildren(int parentId) {
        return repository.findAllChildren(parentId);
    }
}

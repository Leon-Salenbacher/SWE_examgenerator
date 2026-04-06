package service.impl.elements;

import models.ChildObject;
import models.ParentObject;
import repository.ParentRepository;
import service.elements.ParentService;
import service.impl.DataObjectServiceImpl;

import java.util.List;
import java.util.NoSuchElementException;

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

    @Override
    public int countChildren(int id) throws NoSuchElementException {
        //TODO implement
        return 0;
    }

    @Override
    public List<CH> getChildren(int parentId) {
        return repository.findAllChildren(parentId);
    }
}

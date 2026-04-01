package service.impl;

import objects.DataObject;
import repository.Repository;
import service.elements.DataObjectService;

import java.util.List;
import java.util.NoSuchElementException;

public abstract class DataObjectServiceImpl<
            T extends DataObject,
            CMD
        > implements DataObjectService<T, CMD> {

    private final Repository<T> repository;

    protected DataObjectServiceImpl(Repository<T> repository){
        this.repository = repository;
    }

    @Override
    public T getById(int id) throws NoSuchElementException{
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No element found for id " + id));
    }

    @Override
    public List<T> getAll(){
        return repository.findAll();
    }

    @Override
    public T create(CMD command){
        return repository.save(mapCreateCommand(command));
    }

    @Override
    public T update(int id, CMD command) {
        T current = getById(id);
        T updated = mapUpdateCommand(current, command);
        updated.setId(id);
        return repository.update(updated);
    }

    @Override
    public void delete(int id) throws NoSuchElementException {
        repository.deleteById(id);
    }

    protected abstract T mapCreateCommand(CMD command);

    protected abstract T mapUpdateCommand(T current, CMD command);

}

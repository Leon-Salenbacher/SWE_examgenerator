package service.impl;

import models.DataObject;
import repository.Repository;
import service.elements.DataObjectService;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Shared CRUD service implementation that delegates persistence to a repository.
 *
 * @param <T> domain object type
 * @param <CMD> command type used to build or update the domain object
 */
public abstract class DataObjectServiceImpl<
            T extends DataObject,
            CMD
        > implements DataObjectService<T, CMD> {

    private final Repository<T> repository;

    protected DataObjectServiceImpl(Repository<T> repository){
        this.repository = repository;
    }

    /**
     * Finds one object by id or throws a clear exception for controller callers.
     *
     * @param id unique object id
     * @return matching object
     * @throws NoSuchElementException if no object exists for the id
     */
    @Override
    public T getById(int id) throws NoSuchElementException{
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No element found for id " + id));
    }

    /**
     * @return all objects from the backing repository
     */
    @Override
    public List<T> getAll(){
        return repository.findAll();
    }

    /**
     * Maps a command into a new domain object and persists it.
     *
     * @param command create command
     * @return created object
     */
    @Override
    public T create(CMD command){
        return repository.save(mapCreateCommand(command));
    }

    /**
     * Loads, maps and persists an updated object.
     *
     * @param id object id
     * @param command update command
     * @return updated object
     */
    @Override
    public T update(int id, CMD command) {
        T current = getById(id);
        T updated = mapUpdateCommand(current, command);
        updated.setId(id);
        return repository.update(updated);
    }

    /**
     * Deletes one object through the backing repository.
     *
     * @param id object id
     * @throws NoSuchElementException if no object exists for the id
     */
    @Override
    public void delete(int id) throws NoSuchElementException {
        repository.deleteById(id);
    }

    /**
     * Maps create input to a new domain object.
     *
     * @param command create input
     * @return new domain object
     */
    protected abstract T mapCreateCommand(CMD command);

    /**
     * Applies update input to an existing domain object.
     *
     * @param current current persisted object
     * @param command update input
     * @return updated domain object
     */
    protected abstract T mapUpdateCommand(T current, CMD command);

}

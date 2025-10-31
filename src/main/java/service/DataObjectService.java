package service;

import objects.DataObject;

import java.util.List;
import java.util.Optional;


public interface DataObjectService<T extends DataObject> {
    public Optional<T> getById(int id);
    public List<T> getAll();
    public void delete();
}

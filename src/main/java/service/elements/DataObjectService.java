package service.elements;

import objects.DataObject;

import java.util.List;
import java.util.NoSuchElementException;


public interface DataObjectService<
        T extends DataObject,
        C //command (giving parameter for creation and update)
        > {

    T getById(int id) throws NoSuchElementException;

    List<T> getAll();

    T create(C command);

    T update(int id, C command);

    void delete(int id) throws NoSuchElementException;
}

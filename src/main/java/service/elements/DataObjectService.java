package service.elements;

import objects.DataObject;

import java.util.List;
import java.util.NoSuchElementException;


public interface DataObjectService<
        T extends DataObject,
        CMD //command (parameter object for create and update)
        > {

    T getById(int id) throws NoSuchElementException;

    List<T> getAll();

    T create(CMD command);

    T update(int id, CMD command);

    void delete(int id) throws NoSuchElementException;
}

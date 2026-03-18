package repository;

import objects.DataObject;

import java.util.List;
import java.util.Optional;

public interface Repository <T extends DataObject> {

    Optional<T> findById(int id);

    List<T> findAll();

    T save(T object);

    T update(T object);

    void deleteById(int id);
    
}

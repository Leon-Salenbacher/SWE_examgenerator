package repository;

import objects.DataObject;

import java.util.List;
import java.util.Optional;

public interface Repository <T extends DataObject> {

    public static final String ID_ATTRIBUTE_NAME = "id";
    public static final String TITLE_ATTRIBUTE_NAME = "title";

    Optional<T> findById(int id);

    List<T> findAll();

    T save(T object);

    T update(T object);

    void deleteById(int id);



}

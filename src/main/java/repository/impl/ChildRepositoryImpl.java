package repository.impl;

import objects.ChildObject;
import repository.ChildRepository;
import repository.XMLStorageConnector;

public abstract class ChildRepositoryImpl<T extends ChildObject>
        extends RepositoryImpl<T>
    implements ChildRepository<T>{

    protected ChildRepositoryImpl(XMLStorageConnector xmlStorageConnector) {
        super(xmlStorageConnector);
    }

}

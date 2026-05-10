package repository.impl;

import models.ChildObject;
import repository.ChildRepository;
import repository.XMLStorageConnector;

/**
 * Base repository implementation for simple XML-backed child entities.
 *
 * @param <T> child entity type handled by the repository
 */
public abstract class ChildRepositoryImpl<T extends ChildObject>
        extends RepositoryImpl<T>
    implements ChildRepository<T>{

    protected ChildRepositoryImpl(XMLStorageConnector xmlStorageConnector) {
        super(xmlStorageConnector);
    }

}

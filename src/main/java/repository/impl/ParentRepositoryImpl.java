package repository.impl;

import objects.ChildObject;
import objects.ParentObject;
import repository.ParentRepository;

public abstract class ParentRepositoryImpl<
        T extends ParentObject<C>,
        C extends ChildObject>
        extends RepositoryImpl<T> implements ParentRepository<T, C> {


}

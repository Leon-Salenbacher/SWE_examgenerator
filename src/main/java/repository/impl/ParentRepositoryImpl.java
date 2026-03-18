package repository.impl;

import objects.ChildObject;
import objects.ParentObject;
import org.w3c.dom.Element;
import repository.ParentRepository;
import repository.XMLStorageConnector;

import java.util.List;

public abstract class ParentRepositoryImpl<
        T extends ParentObject<C>,
        C extends ChildObject>
        extends RepositoryImpl<T> implements ParentRepository<T, C> {

    protected ParentRepositoryImpl(XMLStorageConnector xmlStorageConnector) {
        super(xmlStorageConnector);
    }

    protected abstract String getChildTagName();

    protected abstract C mapChild(Element childElement);

    @Override
    public T addChild(int id, C child){
        throw new UnsupportedOperationException("add Child is not implemneted yet for tag " + getElementTagName());
    }

    @Override
    public List<C> getChilds(int id){
        return findById(id)
                .map(ParentObject::getChildElements)
                .orElse(List.of());
    }

    @Override
    public T removeChild(int id, int childId){
        throw new UnsupportedOperationException(
                "remove Child is not implemented yet for parent tag " + getElementTagName()
                + " and child tag " + getChildTagName()
        );
    }
}

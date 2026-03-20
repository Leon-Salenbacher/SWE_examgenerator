package repository.impl;

import lombok.*;
import objects.ChildObject;
import org.w3c.dom.Element;
import repository.ChildRepository;
import repository.XMLStorageConnector;

public abstract class ChildRepositoryImpl<T extends ChildObject>
        extends RepositoryImpl<T>
    implements ChildRepository<T>{

    protected ChildRepositoryImpl(XMLStorageConnector xmlStorageConnector) {
        super(xmlStorageConnector);
    }

    @Override
    protected void mapElementFields(Element element, T target){
        super.mapElementFields(element, target);
        this.mapChildElementAttributes(element, target);
    }

    private void mapChildElementAttributes(Element element, T target){
        target.setTitle(getStringAttribute(element, TITLE_ATTRIBUTE_NAME));
    }


}

package repository.impl;

import lombok.*;
import objects.ChildObject;
import org.w3c.dom.Element;
import repository.ChildRepository;
import repository.XMLStorageConnector;

public abstract class ChildRepositoryImpl<T extends ChildObject>
        extends RepositoryImpl<T>
    implements ChildRepository<T>{

    public abstract T mapElement(Element element);

    public ChildRepositoryImpl(XMLStorageConnector xmlStorageConnector) {
        super(xmlStorageConnector);
    }

    private String mapTitle(Element element){
        return element.getAttribute(TITLE_ATTRIBUTE_NAME);
    }

    protected void mapChildElement(Element element, T target){
        this.mapElementData(element, target);
        target.setTitle(mapTitle(element));
    }
}

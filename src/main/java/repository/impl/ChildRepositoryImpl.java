package repository.impl;

import lombok.*;
import objects.ChildObject;
import org.w3c.dom.Element;
import repository.ChildRepository;
import repository.XMLStorageConnector;

public abstract class ChildRepositoryImpl<T extends ChildObject>
        extends RepositoryImpl<T>
    implements ChildRepository<T>{

    public ChildRepositoryImpl(XMLStorageConnector xmlStorageConnector) {
        super(xmlStorageConnector);
    }

    private String mapTitle(Element element){
        return element.getAttribute(TITLE_ATTRIBUTE_NAME);
    }

    @Override
    protected void mapElementFields(Element element, T target){
        super.mapElementFields(element, target);
        this.mapChildElementAttributes(element, target);
    }

    protected void mapChildElementAttributes(Element element, T target){
        this.mapElementData(element, target);
        target.setTitle(mapTitle(element));
    }


}

package repository.impl;

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

    private int mapId(Element element){
        return Integer.parseInt(element.getAttribute(ID_ATTRIBUTE_NAME));
    }

    private String mapTitle(Element element){
        return element.getAttribute(TITLE_ATTRIBUTE_NAME);
    }

    protected ChildObject mapChildObject(Element element){
        return new ChildObject(
            mapId(element),
            mapTitle(element)
        );
    }

    protected record ChildObject(
            int id,
            String title
    ){}
}

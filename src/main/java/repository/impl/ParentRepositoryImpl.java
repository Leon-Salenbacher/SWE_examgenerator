package repository.impl;

import exceptions.XmlStorageException;
import lombok.*;
import objects.ChildObject;
import objects.ParentObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import repository.ParentRepository;
import repository.XMLStorageConnector;

import java.util.ArrayList;
import java.util.List;


public abstract class ParentRepositoryImpl<
        T extends ParentObject<C>,
        C extends ChildObject>
        extends ChildRepositoryImpl<T> implements ParentRepository<T, C> {

    private final ChildRepositoryImpl<C> childRepository;

    protected ParentRepositoryImpl(XMLStorageConnector xmlStorageConnector, ChildRepositoryImpl<C> childRepository) {
        super(xmlStorageConnector);
        this.childRepository = childRepository;
    }

    protected abstract String getChildTagName();

    @Override
    protected void mapElementFields(Element element, T target){
        super.mapElementFields(element, target);
        this.mapParentElementAttributes(element, target);
    }

    private void mapParentElementAttributes(Element element, T target){
        target.setChildElements(mapChildren(element));
    }

    @Override
    protected Element createElement(T object){
        Element element = super.createElement(object);
        appendChildren(element, object.getChildElements());
        return element;
    }

    @Override
    public T update(T object){
        Element element = findElementById(object.getId())
                .orElseThrow(() -> new XmlStorageException(
                        String.format("No %s entry found for id %d.",
                                getElementTagName(), object.getId())
                ));
        writeElement(element, object);
        removeNestedChildren(element);
        appendChildren(element, object.getChildElements());
        getXMLStorageService().saveDocument();
        return object;
    }

    @Override
    public List<C> findAllChildren(int id){
        return findById(id)
                .map(ParentObject::getChildElements)
                .orElse(List.of());
    }

    private List<C> mapChildren(Element parentElement) {
        List<C> children = new ArrayList<>();
        NodeList childNodes = parentElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node instanceof Element childElement && getChildTagName().equals(childElement.getTagName())) {
                children.add(childRepository.mapElement(childElement));
            }
        }
        return children;
    }

    private List<Element> mapChildElements(Element parentElement) {
        List<Element> childElements = new ArrayList<>();
        NodeList childNodes = parentElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node instanceof Element childElement && getChildTagName().equals(childElement.getTagName())) {
                childElements.add(childElement);
            }
        }
        return childElements;
    }

    private void removeNestedChildren(Element parentElement) {
        for (Element childElement : mapChildElements(parentElement)) {
            parentElement.removeChild(childElement);
        }
    }

    private void appendChildren(Element parentElement, List<C> children) {
        if (children == null) {
            return;
        }

        for (C child : children) {
            Element childElement = childRepository.createElement(child);
            parentElement.appendChild(childElement);
        }
    }
}

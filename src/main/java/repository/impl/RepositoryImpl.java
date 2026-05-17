package repository.impl;

import exceptions.XmlStorageException;
import models.DataObject;
import models.DataObjectReflectionSupport;
import org.w3c.dom.*;
import repository.Repository;
import repository.XMLStorageConnector;

import java.util.*;

/**
 * Generic XML repository implementation for objects persisted as DOM elements.
 *
 * @param <T> domain object type handled by the repository
 */
public abstract class RepositoryImpl<T extends DataObject> implements Repository<T> {
    private final XMLStorageConnector xmlStorageConnector;


    protected RepositoryImpl(XMLStorageConnector xmlStorageConnector){
        this.xmlStorageConnector = xmlStorageConnector;
    }

    /**
     * Returns the XML tag name used for this repository's elements.
     *
     * @return XML element tag name
     */
    protected abstract String getElementTagName();

    /**
     * Maps a DOM element to a domain object.
     *
     * @param element XML element to map
     * @return mapped domain object
     */
    protected T mapElement(Element element){
        T target = createEmptyInstance();
        this.mapElementFields(element, target);
        return target;
    }

    /**
     * Maps XML element data into an existing target object.
     *
     * @param element XML element to read
     * @param target domain object to populate
     */
    protected void mapElementFields(Element element, T target){
        this.mapElementData(element, target);
    }

    /**
     * Creates an empty domain object before XML attributes are applied.
     *
     * @return new empty instance
     */
    protected abstract T createEmptyInstance();

    /**
     * Writes a domain object into an existing XML element.
     *
     * @param element XML element to update
     * @param object domain object to write
     */
    protected void writeElement(Element element, T object){
        clearAttributes(element, object.getAttributeNames());
        object.getAttributes().forEach(element::setAttribute);
    }

    /**
     * Returns the connector that owns the DOM document.
     *
     * @return XML storage connector
     */
    protected XMLStorageConnector getXMLStorageService(){
        return this.xmlStorageConnector;
    }

    /**
     * Returns the current DOM document.
     *
     * @return XML document
     */
    protected Document getDocument(){
        return getXMLStorageService().getDocument();
    }

    protected Element getRootElement() throws XmlStorageException{
        Document document = getDocument();
        Element root = document.getDocumentElement();
        if(root == null){
            throw new XmlStorageException("XML document has no root element.");
        }
        return root;
    }

    /**
     * Returns all elements that match this repository's element tag.
     *
     * @return matching DOM elements
     */
    private List<Element> getElementsByTagName(){
        Document document = getDocument();
        NodeList nodeList = document.getElementsByTagName(getElementTagName());
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element element) {
                elements.add(element);
            }
        }
        return elements;
    }


    /**
     * Creates a new DOM element for a domain object.
     *
     * @param object object to serialize
     * @return new DOM element
     */
    protected Element createElement(T object){
        Document document = getDocument();
        Element element = document.createElement(getElementTagName());
        writeElement(element, object);
        return element;
    }

    protected Optional<Element> findElementById(int id){
        return getElementsByTagName().stream()
                .filter(element -> Integer.toString(id)
                        .equals(element.getAttribute(DataObject.ID_ATTRIBUTE_LABEL)))
                .findFirst();
    }

    /**
     * Finds one object by its XML id attribute.
     *
     * @param id object id
     * @return matching object, or an empty optional when no element exists
     */
    @Override
    public Optional<T> findById(int id){
        return findElementById(id).map(this::mapElement);
    }

    /**
     * Loads all XML elements handled by this repository.
     *
     * @return mapped domain objects in document order
     */
    @Override
    public List<T> findAll(){
        return getElementsByTagName().stream()
                .map(this::mapElement)
                .toList();
    }

    /**
     * Appends a new object to the XML root element.
     *
     * @param object object to persist
     * @return persisted object
     */
    @Override
    public T save(T object) {
        Element root = getRootElement();
        root.appendChild(createElement(object));
        xmlStorageConnector.saveDocument();
        return object;
    }

    /**
     * Rewrites the XML element with the same id as the given object.
     *
     * @param object replacement object
     * @return updated object
     * @throws XmlStorageException if no matching element exists
     */
    @Override
    public T update(T object){
        Element element = findElementById(object.getId())
                .orElseThrow(() -> new XmlStorageException(
                        "No " + getElementTagName() + " entry found for id " + object.getId() + "."
                ));

        writeElement(element, object);
        xmlStorageConnector.saveDocument();
        return object;
    }

    /**
     * Removes the XML element with the given id.
     *
     * @param id object id
     * @throws XmlStorageException if no matching element exists or it cannot be removed
     */
    @Override
    public void deleteById(int id){
        Element element = findElementById(id)
                .orElseThrow(() -> new XmlStorageException(
                        "No " + getElementTagName() + " entry found for id " + id + '.'
                ));

        Node parent = element.getParentNode();
        if (parent == null) {
            throw new XmlStorageException("XML element has no parent and cannot be removed.");
        }

        parent.removeChild(element);
        xmlStorageConnector.saveDocument();
    }

    protected void mapElementData(Element element, T target){
        NamedNodeMap attributes = element.getAttributes();
        Map<String, String> mappedAttributes = new LinkedHashMap<>();
        for(int i = 0; i<attributes.getLength(); i++){
            Node attribute = attributes.item(i);
            mappedAttributes.put(attribute.getNodeName(), attribute.getNodeValue());
        }
        DataObjectReflectionSupport.applyAttributes(target, mappedAttributes);
    }

    private void clearAttributes(Element element, List<String> attributeNamesToKeep) {
        Set<String> keep = new HashSet<>(attributeNamesToKeep);
        NamedNodeMap existingAttributes = element.getAttributes();
        List<String> attributesToRemove = new ArrayList<>();
        for (int i = 0; i < existingAttributes.getLength(); i++) {
            Node attribute = existingAttributes.item(i);
            if (!keep.contains(attribute.getNodeName())) {
                attributesToRemove.add(attribute.getNodeName());
            }
        }
        attributesToRemove.forEach(element::removeAttribute);
    }
}

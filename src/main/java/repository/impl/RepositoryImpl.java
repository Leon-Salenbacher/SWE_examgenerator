package repository.impl;

import exceptions.XmlStorageException;
import lombok.*;
import objects.DataObject;
import objects.DataObjectReflectionSupport;
import org.w3c.dom.*;
import repository.Repository;
import repository.XMLStorageConnector;

import java.util.*;

public abstract class RepositoryImpl<T extends DataObject> implements Repository<T> {
    private final XMLStorageConnector xmlStorageConnector;


    protected RepositoryImpl(XMLStorageConnector xmlStorageConnector){
        this.xmlStorageConnector = xmlStorageConnector;
    }

    /**
     * Returns the xml tag for the element.
     * @return
     */
    protected abstract String getElementTagName();

    /**
     * Maps the given element to the full object
     * @param element
     * @return
     */
    protected T mapElement(Element element){
        T target = createEmptyInstance();
        this.mapElementFields(element, target);
        return target;
    }

    /**
     * Maps the ELement into the target Obeject structure.
     * @param element
     * @param target
     */
    protected void mapElementFields(Element element, T target){
        this.mapElementData(element, target);
    }

    protected abstract T createEmptyInstance();

    /**
     * Write object as element in xml
     * @param element
     * @param object
     */
    protected void writeElement(Element element, T object){
        clearAttributes(element, object.getAttributeNames());
        object.getAttributes().forEach(element::setAttribute);
    }

    /**
     * Returns the {@link XMLStorageConnector}
     * @return
     */
    protected XMLStorageConnector getXMLStorageService(){
        return this.xmlStorageConnector;
    }

    /**
     * Returns the {@link Document}
     * @return
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
     * Returns all {@link Element}s, that are stored with the tag on
     * @return
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

    @Override
    public Optional<T> findById(int id){
        return findElementById(id).map(this::mapElement);
    }

    @Override
    public List<T> findAll(){
        return getElementsByTagName().stream()
                .map(this::mapElement)
                .toList();
    }

    @Override
    public T save(T object) {
        Element root = getRootElement();
        root.appendChild(createElement(object));
        xmlStorageConnector.saveDocument();
        return object;
    }

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

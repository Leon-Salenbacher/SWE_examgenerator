package repository.impl;

import exceptions.XmlStorageException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import repository.XMLStorageConnector;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;


public class XMLStorageConnectorImpl implements XMLStorageConnector {

    private final Path xmlPath;
    private final DocumentBuilderFactory documentBuilderFactory;
    private final TransformerFactory transformerFactory;
    private Document document;

    public XMLStorageConnectorImpl(Path xmlPath, DocumentBuilderFactory documentBuilderFactory, TransformerFactory transformerFactory) {
        this.xmlPath = xmlPath;
        this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
        this.documentBuilderFactory.setNamespaceAware(false);
        this.transformerFactory = TransformerFactory.newInstance();
    }

    @Override
    public Document getDocument(){
        if(document == null){
            document = loadDocument();
        }
        return document;
    }

    @Override
    public void saveDocument() {
        Document currentDocument = getDocument();
        Element rootElement = currentDocument.getDocumentElement();
        if (rootElement == null) {
            throw new XmlStorageException("Cannot save XML document because no root element exists.");
        }

        try (OutputStream outputStream = Files.newOutputStream(xmlPath)) {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(currentDocument), new StreamResult(outputStream));
        } catch (IOException | TransformerException e) {
            throw new XmlStorageException("Failed to save XML document at " + xmlPath + '.', e);
        }
    }

    @Override
    public Path getXmlPath() {
        return xmlPath;
    }

    private Document loadDocument() {
        ensureFileExists();
        try (InputStream inputStream = Files.newInputStream(xmlPath)) {
            DocumentBuilder builder = newDocumentBuilder();
            Document loadedDocument = builder.parse(inputStream);
            loadedDocument.getDocumentElement().normalize();
            return loadedDocument;
        } catch (Exception e) {
            throw new XmlStorageException("Failed to open XML document at " + xmlPath + '.', e);
        }
    }

    private DocumentBuilder newDocumentBuilder() throws XmlStorageException {
        try {
            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new XmlStorageException("Failed to create XML document builder.", e);
        }
    }

    /**
     * Makes sure an XML file exist, if not it will create one.
     *
     * @throws XmlStorageException if failed to initialize a new XML document.
     */
    private void ensureFileExists() throws XmlStorageException{
        if (Files.exists(xmlPath)) {
            return;
        }

        try {
            Path parent = xmlPath.getParent();
            //TODO move create out or change method name!
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(xmlPath, "<ExamGenerator/>");
            //TODO magic string remove!

        } catch (IOException e) {
            throw new XmlStorageException("Failed to initialize XML document at " + xmlPath + '.', e);
        }
    }
}

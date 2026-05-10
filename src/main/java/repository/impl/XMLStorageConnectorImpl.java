package repository.impl;

import exceptions.XmlStorageException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import repository.XMLStorageConnector;

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
import java.util.Objects;

import org.xml.sax.SAXException;

/**
 * DOM-based XML storage connector used by all repositories.
 *
 * <p>The connector lazily loads the XML file, keeps the DOM in memory, and writes
 * it back after repository mutations, imports or exports.</p>
 */
public class XMLStorageConnectorImpl implements XMLStorageConnector {

    private static final String ROOT_ELEMENT_NAME = "ExamGenerator";

    private final Path xmlPath;
    private final DocumentBuilderFactory documentBuilderFactory;
    private final TransformerFactory transformerFactory;
    private Document document;

    /**
     * Creates a connector with injectable XML factories for production or tests.
     *
     * @param xmlPath project-local XML data path
     * @param documentBuilderFactory XML parser factory, or {@code null} for the default
     * @param transformerFactory XML writer factory, or {@code null} for the default
     */
    public XMLStorageConnectorImpl(Path xmlPath, DocumentBuilderFactory documentBuilderFactory, TransformerFactory transformerFactory) {
        this.xmlPath = Objects.requireNonNull(xmlPath, "xmlPath must not be null.");
        this.documentBuilderFactory = documentBuilderFactory == null
                ? DocumentBuilderFactory.newInstance()
                : documentBuilderFactory;
        this.documentBuilderFactory.setNamespaceAware(false);
        this.transformerFactory = transformerFactory == null
                ? TransformerFactory.newInstance()
                : transformerFactory;
    }

    @Override
    /**
     * @return lazily loaded DOM document for the current XML file
     */
    public Document getDocument() {
        if (document == null) {
            document = loadDocument();
        }
        return document;
    }

    @Override
    /**
     * Persists the currently loaded DOM document to the configured XML path.
     */
    public void saveDocument() {
        Document currentDocument = getDocument();
        Element rootElement = currentDocument.getDocumentElement();
        if (rootElement == null) {
            throw new XmlStorageException("Cannot save XML document because no root element exists.");
        }

        removeWhitespaceOnlyTextNodes(currentDocument);

        writeDocument(currentDocument, xmlPath, "save");
    }

    @Override
    /**
     * Replaces the current storage document with an imported XML file.
     *
     * @param sourcePath source XML file
     */
    public void importDocument(Path sourcePath) {
        if (sourcePath == null) {
            throw new XmlStorageException("No XML import file was selected.");
        }
        if (!Files.exists(sourcePath)) {
            throw new XmlStorageException("XML import file does not exist: " + sourcePath + '.');
        }

        Document importedDocument = parseDocument(sourcePath);
        validateRootElement(importedDocument, sourcePath);
        document = importedDocument;
        saveDocument();
    }

    @Override
    /**
     * Writes the current storage document to a chosen export location.
     *
     * @param targetPath target XML file
     */
    public void exportDocument(Path targetPath) {
        if (targetPath == null) {
            throw new XmlStorageException("No XML export file was selected.");
        }

        Document currentDocument = getDocument();
        Element rootElement = currentDocument.getDocumentElement();
        if (rootElement == null) {
            throw new XmlStorageException("Cannot export XML document because no root element exists.");
        }

        removeWhitespaceOnlyTextNodes(currentDocument);
        writeDocument(currentDocument, targetPath, "export");
    }

    @Override
    /**
     * @return configured project XML data path
     */
    public Path getXmlPath() {
        return xmlPath;
    }

    private Document loadDocument() {
        ensureFileExists();
        return parseDocument(xmlPath);
    }

    private Document parseDocument(Path sourcePath) {
        try (InputStream inputStream = Files.newInputStream(sourcePath)) {
            DocumentBuilder builder = newDocumentBuilder();
            Document parsedDocument = builder.parse(inputStream);
            removeWhitespaceOnlyTextNodes(parsedDocument);
            if (parsedDocument.getDocumentElement() != null) {
                parsedDocument.getDocumentElement().normalize();
            }
            return parsedDocument;
        } catch (IOException | SAXException e) {
            throw new XmlStorageException("Failed to open XML document at " + sourcePath + '.', e);
        }
    }

    private DocumentBuilder newDocumentBuilder() throws XmlStorageException {
        try {
            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new XmlStorageException("Failed to create XML document builder.", e);
        }
    }

    private void ensureFileExists() throws XmlStorageException {
        if (Files.exists(xmlPath)) {
            return;
        }

        try {
            Path parent = xmlPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(xmlPath, "<ExamGenerator/>");
        } catch (IOException e) {
            throw new XmlStorageException("Failed to initialize XML document at " + xmlPath + '.', e);
        }
    }

    private void validateRootElement(Document importedDocument, Path sourcePath) {
        Element rootElement = importedDocument.getDocumentElement();
        if (rootElement == null || !ROOT_ELEMENT_NAME.equals(rootElement.getTagName())) {
            throw new XmlStorageException(
                    "XML document at " + sourcePath + " must use <" + ROOT_ELEMENT_NAME + "> as root element."
            );
        }
    }

    private void writeDocument(Document documentToWrite, Path targetPath, String operation) {
        try {
            Path parent = targetPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new XmlStorageException("Failed to prepare XML " + operation + " path " + targetPath + '.', e);
        }

        try (OutputStream outputStream = Files.newOutputStream(targetPath)) {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(documentToWrite), new StreamResult(outputStream));
        } catch (IOException | TransformerException e) {
            throw new XmlStorageException("Failed to " + operation + " XML document at " + targetPath + '.', e);
        }
    }

    private void removeWhitespaceOnlyTextNodes(Node node) {
        NodeList childNodes = node.getChildNodes();
        // Iterate backwards because child removal changes the live DOM NodeList.
        for (int index = childNodes.getLength() - 1; index >= 0; index--) {
            Node child = childNodes.item(index);
            if (child.getNodeType() == Node.TEXT_NODE && child.getTextContent().trim().isEmpty()) {
                node.removeChild(child);
            } else if (child.hasChildNodes()) {
                removeWhitespaceOnlyTextNodes(child);
            }
        }
    }
}

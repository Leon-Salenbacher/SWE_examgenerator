package repository;

import exceptions.XmlStorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import repository.impl.XMLStorageConnectorImpl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TestXMLStorageConnector {

    @TempDir
    Path tempDir;

    @Test
    public void test_getDocument_goodcase01_createDefault(){
        //File doesn't exist => create new one
        Path xmlFile = tempDir.resolve("data").resolve("exam.xml");
        assertFalse(Files.exists(xmlFile));
        XMLStorageConnector connector = new XMLStorageConnectorImpl(
                xmlFile,
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );

        Document document = connector.getDocument();

        assertNotNull(document);
        assertTrue(Files.exists(xmlFile));
        assertEquals("ExamGenerator", document.getDocumentElement().getTagName());
    }

    @Test
    public void test_getDocument_goodcase02_loadExistingXml() throws Exception{
        Path xmlFile = tempDir.resolve("exam.xml");
        Files.writeString(xmlFile, "<ExamGenerator version=\"2\"><chapter id=\"1\"/></ExamGenerator>");
        XMLStorageConnector connector = new XMLStorageConnectorImpl(
                xmlFile,
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );

        Document document = connector.getDocument();

        assertNotNull(document);
        assertEquals("ExamGenerator", document.getDocumentElement().getTagName());
        assertEquals("2", document.getDocumentElement().getAttribute("version"));
        assertEquals(1, document.getDocumentElement().getElementsByTagName("chapter").getLength());
    }

    @Test
    public void test_getDocument_goodcase03_returnSameDocumentInstance(){
        Path xmlFile = tempDir.resolve("exam.xml");
        XMLStorageConnector connector = new XMLStorageConnectorImpl(
                xmlFile,
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );

        Document firstDocument = connector.getDocument();
        Document secondDocument = connector.getDocument();

        assertSame(firstDocument, secondDocument);
    }

    @Test
    public void test_getXmlPath_goodcase01_returnConfiguredPath(){
        Path xmlFile = tempDir.resolve("exam.xml");
        XMLStorageConnector connector = new XMLStorageConnectorImpl(
                xmlFile,
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );

        Path actualPath = connector.getXmlPath();

        assertEquals(xmlFile, actualPath);
    }

    @Test
    public void test_saveDocument_goodcase01_persistNewElement() throws Exception{
        Path xmlFile = tempDir.resolve("exam.xml");
        XMLStorageConnector connector = new XMLStorageConnectorImpl(
                xmlFile,
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );

        Document document = connector.getDocument();
        Element chapter = document.createElement("chapter");
        chapter.setAttribute("id", "7");
        document.getDocumentElement().appendChild(chapter);

        connector.saveDocument();

        String persistedXml = Files.readString(xmlFile);
        assertTrue(persistedXml.contains("<chapter id=\"7\""));
    }

    @Test
    public void test_saveDocument_badcase01_noRootElementThrows(){
        Path xmlFile = tempDir.resolve("exam.xml");
        XMLStorageConnector connector = new XMLStorageConnectorImpl(
                xmlFile,
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );

        Document document = connector.getDocument();
        document.removeChild(document.getDocumentElement());

        assertThrows(XmlStorageException.class, connector::saveDocument);
    }

}

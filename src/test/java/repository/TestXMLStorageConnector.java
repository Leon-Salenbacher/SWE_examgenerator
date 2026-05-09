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

    @Test
    public void test_importDocument_goodcase01_replaceDocumentAndPersistStorage() throws Exception{
        Path xmlFile = tempDir.resolve("exam.xml");
        Path importFile = tempDir.resolve("import.xml");
        Files.writeString(importFile, "<ExamGenerator><Chapter id=\"42\" title=\"Imported\"/></ExamGenerator>");
        XMLStorageConnector connector = new XMLStorageConnectorImpl(
                xmlFile,
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );

        connector.importDocument(importFile);

        Document document = connector.getDocument();
        assertEquals(1, document.getElementsByTagName("Chapter").getLength());
        assertTrue(Files.readString(xmlFile).contains("Imported"));
    }

    @Test
    public void test_importDocument_badcase01_rejectInvalidRoot() throws Exception{
        Path xmlFile = tempDir.resolve("exam.xml");
        Path importFile = tempDir.resolve("import.xml");
        Files.writeString(importFile, "<OtherRoot/>");
        XMLStorageConnector connector = new XMLStorageConnectorImpl(
                xmlFile,
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );

        assertThrows(XmlStorageException.class, () -> connector.importDocument(importFile));
    }

    @Test
    public void test_importDocument_badcase02_rejectMissingImportFile(){
        Path xmlFile = tempDir.resolve("exam.xml");
        Path importFile = tempDir.resolve("missing.xml");
        XMLStorageConnector connector = new XMLStorageConnectorImpl(
                xmlFile,
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );

        assertThrows(XmlStorageException.class, () -> connector.importDocument(importFile));
    }

    @Test
    public void test_importDocument_badcase03_rejectMalformedXml() throws Exception{
        Path xmlFile = tempDir.resolve("exam.xml");
        Path importFile = tempDir.resolve("malformed.xml");
        Files.writeString(importFile, "<ExamGenerator>");
        XMLStorageConnector connector = new XMLStorageConnectorImpl(
                xmlFile,
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );

        assertThrows(XmlStorageException.class, () -> connector.importDocument(importFile));
    }

    @Test
    public void test_exportDocument_goodcase01_writeCurrentDocumentToTarget() throws Exception{
        Path xmlFile = tempDir.resolve("exam.xml");
        Path exportFile = tempDir.resolve("exports").resolve("exam-export.xml");
        XMLStorageConnector connector = new XMLStorageConnectorImpl(
                xmlFile,
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
        Document document = connector.getDocument();
        Element chapter = document.createElement("Chapter");
        chapter.setAttribute("id", "7");
        chapter.setAttribute("title", "Exported");
        document.getDocumentElement().appendChild(chapter);

        connector.exportDocument(exportFile);

        String exportedXml = Files.readString(exportFile);
        assertTrue(exportedXml.contains("<Chapter"));
        assertTrue(exportedXml.contains("id=\"7\""));
        assertTrue(exportedXml.contains("title=\"Exported\""));
    }

    @Test
    public void test_exportDocument_goodcase02_writeToTargetWithoutXmlExtension() throws Exception{
        Path xmlFile = tempDir.resolve("exam.xml");
        Path exportFile = tempDir.resolve("exports").resolve("exam-export");
        XMLStorageConnector connector = new XMLStorageConnectorImpl(
                xmlFile,
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
        connector.getDocument();

        connector.exportDocument(exportFile);

        assertTrue(Files.exists(exportFile));
        assertTrue(Files.readString(exportFile).contains("<ExamGenerator"));
    }

    @Test
    public void test_exportDocument_badcase01_missingRootThrows(){
        Path xmlFile = tempDir.resolve("exam.xml");
        XMLStorageConnector connector = new XMLStorageConnectorImpl(
                xmlFile,
                DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance()
        );
        connector.getDocument().removeChild(connector.getDocument().getDocumentElement());

        assertThrows(XmlStorageException.class, () -> connector.exportDocument(tempDir.resolve("export.xml")));
    }

}

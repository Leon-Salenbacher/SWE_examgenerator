package repository;

import exceptions.XmlStorageException;
import org.w3c.dom.Document;

import java.nio.file.Path;


public interface XMLStorageConnector {
    Document getDocument() throws XmlStorageException;

    void saveDocument() throws  XmlStorageException;
    Path getXmlPath();
}

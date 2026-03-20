package repository;

import exceptions.XmlStorageException;
import org.w3c.dom.Document;

import java.nio.file.Path;

/**
 * Abstraction over the XML document used by repository implementations.
 * <p>
 *     The connector is responsible for lazily loading the backing XML document,
 *     exposing it to repositories and persisting in-memory DOM changes back to the
 *     configured file system location.
 * </p>
 */
public interface XMLStorageConnector {

    /**
     * Returns the managed XML document.
     *
     * @return lazily loaded DOM document representing the current repository state
     * @throws XmlStorageException if the document cannot be loaded.
     */
    Document getDocument() throws XmlStorageException;

    /**
     * Writes the current in-memory DOM state to the configured XML file.
     *
     * @throws XmlStorageException if persisting the document fails
     */
    void saveDocument() throws  XmlStorageException;

    /**
     * Returns the file system path of the XML file managed by this connector.
     *
     * @return path to the XML storage file
     */
    Path getXmlPath();
}

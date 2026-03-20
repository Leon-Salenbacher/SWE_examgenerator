package objects;

import java.util.List;
import java.util.Map;

/**
 * Base contract for all domain objects that can be persisted by a repository.
 * Implementations expose their XML attributes so generic repository code can
 * handle common save and update operations.
 */
public interface DataObject {
    int getId();
    void setId(int id);

    /**
     * Returns all XML attributes that should be written for this objects.
     * The map key is the XML attribute name, the value is the serialized value.
     * @return
     */
    Map<String, String> getAttributes();

    /**
     * Convenience method for code that only needs the configured attribute names.
     * @return
     */
    default List<String> getAttributeNames(){
        return List.copyOf(getAttributes().keySet());
    }
}

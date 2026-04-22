package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

/**
 * Base contract for all domain objects that can be persisted by a repository.
 * Implementations expose their XML attributes so generic repository code can
 * handle common save and update operations.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class DataObject {
    public static final String ID_ATTRIBUTE_LABEL = "id";

    @XmlField(ID_ATTRIBUTE_LABEL)
    protected int id;


    /**
     * Returns all XML attributes that should be written for this object.
     * The map key is the XML attribute name, the value is the serialized value.
     * @return
     */
    public Map<String, String> getAttributes(){
        return DataObjectReflectionSupport.getAttributes(this);
    }

    /**
     * Convenience method for code that only needs the configured attribute names.
     * @return
     */
    public List<String> getAttributeNames(){
        return DataObjectReflectionSupport.getAttributeNames(getClass());
    }
}

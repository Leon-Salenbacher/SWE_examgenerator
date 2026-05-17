package models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Base class for all editable domain objects that can appear in the UI tree.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class ChildObject extends DataObject{
    public static final String TITLE_ATTRIBUTE_LABEL ="title";

    @XmlField(TITLE_ATTRIBUTE_LABEL)
    protected String title;
}

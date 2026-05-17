package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for domain objects that contain nested child objects.
 *
 * @param <C> child element type contained by this parent
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class ParentObject<C extends ChildObject> extends ChildObject {
    public static final String CHILD_TAG_NAME = "child";

    @Builder.Default
    protected List<C> childElements = new ArrayList<>();

    /**
     * Replaces the current children with a defensive copy.
     *
     * @param childElements new child collection, or {@code null} to clear it
     */
    public void setChildElements(List<C> childElements){
        this.childElements = childElements == null ? new ArrayList<>() : new ArrayList<>(childElements);
    }

    /**
     * Adds one child element to the current child list.
     *
     * @param childElement child to append
     */
    public void addChildElement(C childElement){
        this.childElements.add(childElement);
    }
}

package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class ParentObject<C extends ChildObject> extends ChildObject {
    public static final String CHILD_TAG_NAME = "child";

    @Builder.Default
    protected List<C> childElements = new ArrayList<>();

    public void setChildElements(List<C> childElements){
        this.childElements = childElements == null ? new ArrayList<>() : new ArrayList<>(childElements);
    }
    public void addChildElement(C childElement){
        this.childElements.add(childElement);
    }
}

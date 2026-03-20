package objects;

import java.util.List;

public interface ParentObject<C extends ChildObject> extends ChildObject {
    void setChildElements(List<C> childObjects);
    void addChildElement(C childObject);
    List<C> getChildElements();
}

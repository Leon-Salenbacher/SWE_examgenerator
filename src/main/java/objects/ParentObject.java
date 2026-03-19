package objects;

import java.util.List;

public interface ParentObject<C extends ChildObject> extends ChildObject {
    public void setChildElements(List<C> childObjects);
    public void addChildElement(C childObject);
    public List<C> getChildElements();
}

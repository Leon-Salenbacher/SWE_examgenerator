package objects;

import java.util.List;

public interface ParentObject<T extends DataObject> extends ChildObject {
    public List<T> getChildElements();
}

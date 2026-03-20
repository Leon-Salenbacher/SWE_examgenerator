package objects;

public interface ChildObject extends DataObject{
    String TITLE_ATTRIBUTE_LABEL ="title";

    public String getTitle();
    public void setTitle(String title);
}

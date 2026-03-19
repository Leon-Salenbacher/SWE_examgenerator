package repository.detailed.impl;

import objects.Subtask;
import objects.Variant;
import org.w3c.dom.Element;
import repository.XMLStorageConnector;
import repository.detailed.SubtaskRepository;
import repository.impl.ParentRepositoryImpl;

public class SubtaskRepositoryImpl
        extends ParentRepositoryImpl<Subtask, Variant>
        implements SubtaskRepository {

    public SubtaskRepositoryImpl(XMLStorageConnector xmlStorageConnector){
        super(xmlStorageConnector);
    }

    @Override
    protected String getElementTagName(){
        return ELEMENT_TAG_NAME;
    }

    @Override
    protected String getChildTagName(){
        return CHILD_TAG_NAME;
    }

    @Override
    protected Subtask mapElement(Element element){
        int id = Integer.parseInt(element.getAttribute(ID_ATTRIBUTE_NAME));
        String title = element.getAttribute(TITLE_ATTRIBUTE_NAME)
    }





}

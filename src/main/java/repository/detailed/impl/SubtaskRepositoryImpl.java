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

    public SubtaskRepositoryImpl(XMLStorageConnector xmlStorageConnector, VariantRepositoryImpl variantRepository){
        super(xmlStorageConnector, variantRepository);
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
    protected void mapElementFields(Element element, Subtask target){
        super.mapElementFields(element, target);
    }

    private void mapSubtaskAttributes(Element element, Subtask target){

    }



}

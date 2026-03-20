package repository.detailed.impl;

import objects.Variant;
import org.w3c.dom.Element;
import repository.XMLStorageConnector;
import repository.detailed.VariantRepository;
import repository.impl.ChildRepositoryImpl;


public class VariantRepositoryImpl
        extends ChildRepositoryImpl<Variant>
        implements VariantRepository {

    public VariantRepositoryImpl(XMLStorageConnector xmlStorageConnector){
        super(xmlStorageConnector);
    }

    @Override
    protected String getElementTagName(){
        return ELEMENT_TAG_NAME;
    }

    @Override
    protected Variant createEmptyInstance(){
        return new Variant();
    }

    @Override
    protected void mapElementFields(Element element, Variant target){
        super.mapElementFields(element, target);
        mapVariantAttributes(element, target);
    }

    private void mapVariantAttributes(Element element, Variant target){
        target.setQuestion(getStringAttribute(element, QUESTION_ATTRIBUTE_LABEL));
        target.setSolution(getStringAttribute(element, SOLUTION_ATTRIBUTE_LABEL));
    }
}

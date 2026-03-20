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
        return Variant.ELEMENT_TAG_NAME;
    }

    @Override
    protected Variant createEmptyInstance(){
        return new Variant();
    }
}

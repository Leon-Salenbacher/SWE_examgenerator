package repository.detailed.impl;

import models.Variant;
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

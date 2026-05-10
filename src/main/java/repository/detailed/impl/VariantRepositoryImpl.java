package repository.detailed.impl;

import models.Variant;
import repository.XMLStorageConnector;
import repository.detailed.VariantRepository;
import repository.impl.ChildRepositoryImpl;


/**
 * XML repository for standalone variant elements.
 */
public class VariantRepositoryImpl
        extends ChildRepositoryImpl<Variant>
        implements VariantRepository {

    /**
     * Creates a variant repository backed by the shared XML connector.
     *
     * @param xmlStorageConnector shared XML connector
     */
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

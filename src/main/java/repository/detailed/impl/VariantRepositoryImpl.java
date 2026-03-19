package repository.detailed.impl;

import objects.Variant;
import repository.XMLStorageConnector;
import repository.detailed.VariantRepository;
import repository.impl.ChildRepositoryImpl;

public class VariantRepositoryImpl extends ChildRepositoryImpl<Variant> implements VariantRepository {
    public VariantRepositoryImpl(XMLStorageConnector xmlStorageConnector){
        super(xmlStorageConnector);

    }
}

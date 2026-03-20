package repository.detailed.impl;

import objects.Subtask;
import objects.Variant;
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
        return Subtask.ELEMENT_TAG_NAME;
    }

    @Override
    protected String getChildTagName(){
        return Subtask.CHILD_TAG_NAME;
    }

    @Override
    protected Subtask createEmptyInstance(){
        return new Subtask();
    }
}

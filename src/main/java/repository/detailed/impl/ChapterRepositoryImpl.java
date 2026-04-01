package repository.detailed.impl;

import objects.Chapter;
import objects.Subtask;
import repository.detailed.ChapterRepository;
import repository.XMLStorageConnector;
import repository.impl.ParentRepositoryImpl;

public class ChapterRepositoryImpl
    extends ParentRepositoryImpl<Chapter, Subtask>
    implements ChapterRepository {

    public ChapterRepositoryImpl(XMLStorageConnector xmlStorageConnector, SubtaskRepositoryImpl subtaskRepository){
        super(xmlStorageConnector, subtaskRepository);
    }

    @Override
    protected String getElementTagName(){
        return Chapter.ELEMENT_TAG_NAME;
    }

    @Override
    protected String getChildTagName(){
        return Chapter.CHILD_ELEMENT_TAG_NAME;
    }

    @Override
    protected Chapter createEmptyInstance(){
        return new Chapter();
    }

}

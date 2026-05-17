package repository.detailed.impl;

import models.Chapter;
import models.Subtask;
import repository.detailed.ChapterRepository;
import repository.XMLStorageConnector;
import repository.impl.ParentRepositoryImpl;

/**
 * XML repository for chapters and their nested subtasks.
 */
public class ChapterRepositoryImpl
    extends ParentRepositoryImpl<Chapter, Subtask>
    implements ChapterRepository {

    /**
     * Creates a chapter repository backed by the shared XML connector.
     *
     * @param xmlStorageConnector shared XML connector
     * @param subtaskRepository repository used to map nested subtasks
     */
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

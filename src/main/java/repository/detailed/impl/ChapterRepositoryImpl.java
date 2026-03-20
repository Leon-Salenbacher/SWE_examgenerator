package repository.detailed.impl;

import objects.Chapter;
import objects.Subtask;
import org.w3c.dom.Element;
import repository.detailed.ChapterRepository;
import repository.detailed.SubtaskRepository;
import repository.XMLStorageConnector;
import repository.impl.ParentRepositoryImpl;

import java.util.List;

public class ChapterRepositoryImpl
    extends ParentRepositoryImpl<Chapter, Subtask>
    implements ChapterRepository {

    public ChapterRepositoryImpl(XMLStorageConnector xmlStorageConnector, SubtaskRepositoryImpl subtaskRepository){
        super(xmlStorageConnector, subtaskRepository);
    }

    @Override
    protected String getElementTagName(){
        return ELEMENT_TAG_NAME;
    }

    @Override
    protected String getChildTagName(){
        return CHILD_TAG_NAME;
    }


    protected Chapter createEmptyInstance(){
        return new Chapter();
    }

    @Override
    protected void mapElementFields(Element element, Chapter target){
        super.mapElementFields(element, target);
    }
}

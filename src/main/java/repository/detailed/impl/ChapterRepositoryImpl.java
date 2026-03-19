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

    @Override
    protected void mapElementFields(Element element, Chapter target){
        super.mapElementFields(element, target);
        this.mapChapterAttributes(element, target);
    }

    private void mapChapterAttributes(Element element, Chapter target){

    }


    protected Chapter createEmptyInstance(){
        return new Chapter();
    }

    @Override
    protected void writeElement(Element element, Chapter object){
        element.setAttribute(ID_ATTRIBUTE_NAME, Integer.toString(object.getId()));
        element.setAttribute(TITLE_ATTRIBUTE_NAME, object.getTitle() == null ? "" : object.getTitle());
        //TODO add placeholder for child elements
    }


    @Override
    protected void writeChild(Element childElement, Subtask child) {
        //TODO can ChildRepo be used? problem: implementing there a create with given parent we need to implement it there to
        childElement.setAttribute(ID_ATTRIBUTE_NAME, Integer.toString(child.getId()));
        childElement.setAttribute(TITLE_ATTRIBUTE_NAME, child.getTitle() == null ? "" : child.getTitle());
        childElement.setAttribute(SubtaskRepository.POINT_ATTRIBUTE_LABEL, Integer.toString(child.getPoints()));
        childElement.setAttribute(SubtaskRepository.PARENT_ID_ATTRIBUTE_LABEL, Integer.toString(child.getChapterId()));
    }


}

package repository.detailed.impl;

import objects.Chapter;
import objects.ParentObject;
import objects.Subtask;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
    public Chapter mapElement(Element element){
        Chapter chapter = new Chapter();
        this.mapChapterElement(element, chapter);
        return chapter;
    }

    private void mapChapterElement(Element element, Chapter target){
        this.mapParentObject(element, target);
    }

    @Override
    protected void writeElement(Element element, Chapter object){
        element.setAttribute(ID_ATTRIBUTE_NAME, Integer.toString(object.getId()));
        element.setAttribute(TITLE_ATTRIBUTE_NAME, object.getTitle() == null ? "" : object.getTitle());
    }

    @Override
    protected Subtask mapChild(Element childElement){
        int id = Integer.parseInt(childElement.getAttribute(ID_ATTRIBUTE_NAME));
        String title = childElement.getAttribute(TITLE_ATTRIBUTE_NAME);
        int points = parseIntAttribute(childElement, SubtaskRepository.POINT_ATTRIBUTE_LABEL);
        int chapterId = parseIntAttribute(childElement, SubtaskRepository.PARENT_ID_ATTRIBUTE_LABEL);
        return new Subtask(id, title, points, chapterId);
    }

    @Override
    protected void writeChild(Element childElement, Subtask child) {
        childElement.setAttribute(ID_ATTRIBUTE_NAME, Integer.toString(child.getId()));
        childElement.setAttribute(TITLE_ATTRIBUTE_NAME, child.getTitle() == null ? "" : child.getTitle());
        childElement.setAttribute(SubtaskRepository.POINT_ATTRIBUTE_LABEL, Integer.toString(child.getPoints()));
        childElement.setAttribute(SubtaskRepository.PARENT_ID_ATTRIBUTE_LABEL, Integer.toString(child.getChapterId()));
    }

    private Subtask ensureChapterId(Subtask subtask, int chapterId) {
        if (subtask.getChapterId() == 0) {
            subtask.setChapterId(chapterId);
        }
        return subtask;
    }

    private int parseIntAttribute(Element element, String attributeName){
        String value = element.getAttribute(attributeName);
        if(value.isBlank()){
            return 0;
        }
        return Integer.parseInt(value);
    }
}

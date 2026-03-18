package repository.impl;

import objects.Chapter;
import objects.Subtask;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import repository.ChapterRepository;
import repository.ParentRepository;
import repository.SubtaskRepository;
import repository.XMLStorageConnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChapterRepositoryImpl
    extends ParentRepositoryImpl<Chapter, Subtask>
    implements ChapterRepository {

    public ChapterRepositoryImpl(XMLStorageConnector xmlStorageConnector){
        super(xmlStorageConnector);
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
    protected Chapter mapElement(Element element){
        int id = Integer.parseInt(element.getAttribute(ID_ATTRIBUTE_NAME));
        String title = element.getAttribute(TITLE_ATTRIBUTE_NAME);
        return new Chapter(id, title, mapSubtasks(element, id));
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

    private List<Subtask> mapSubtasks(Element chapterElement, int chapterId){
        List<Subtask> subtasks = new ArrayList<>();
        NodeList children = chapterElement.getChildNodes();
        for(int i = 0; i < children.getLength(); i++){
            Node node = children.item(i);
            if(node instanceof Element childElement && getChildTagName().equals(childElement.getTagName())){
                Subtask subtask = mapChild(childElement);
                if(subtask.getChapterId()  == 0){
                    subtask.setChapterId(chapterId);
                }
                subtasks.add(subtask);
            }
        }
        return subtasks;
    }


    private int parseIntAttribute(Element element, String attributeName){
        String value = element.getAttribute(attributeName);
        if(value.isBlank()){
            return 0;
        }
        return Integer.parseInt(value);
    }


}

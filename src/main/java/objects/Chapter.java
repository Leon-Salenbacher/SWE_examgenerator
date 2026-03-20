package objects;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Chapter implements ParentObject<Subtask>{
    public static final String ELEMENT_TAG_NAME = "Chapter";
    public static final String CHILD_ELEMENT_TAG_NAME = "Subtask";

    private int id;
    private String title;
    private List<Subtask> subtasks = new ArrayList<>();

    @Override
    public void setChildElements(List<Subtask> childElements){
        this.subtasks = childElements == null ? new ArrayList<>() : new ArrayList<>(childElements);
    }

    @Override
    public void addChildElement(Subtask childElement){
        this.subtasks.add(childElement);
    }

    @Override
    public List<Subtask> getChildElements() {
        return this.getSubtasks();
    }

    @Override
    public String getElementTagName() {
        return ELEMENT_TAG_NAME;
    }

    @Override
    public String getChildElementTagName() {
        return CHILD_ELEMENT_TAG_NAME;
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put(ID_ATTRIBUTE_LABEL, Integer.toString(id));
        attributes.put(TITLE_ATTRIBUTE_LABEL, title == null ? "" : title);
        return attributes;
    }

    @Override
    public void applyAttribute(String attributeName, String value) {
        switch (attributeName) {
            case ID_ATTRIBUTE_LABEL -> setId(parseInt(value));
            case TITLE_ATTRIBUTE_LABEL -> setTitle(value);
            default -> {
            }
        }
    }

    private int parseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Integer.parseInt(value);
    }

}

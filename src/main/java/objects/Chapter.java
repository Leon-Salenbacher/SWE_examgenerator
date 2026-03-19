package objects;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Chapter implements ParentObject<Subtask>{
    private int id;
    private String title;
    private List<Subtask> subtasks;

    @Override
    public void setChildElements(List<Subtask> childElements){
        this.subtasks = childElements;
    }

    @Override
    public void addChildElement(Subtask childElement){
        this.subtasks.add(childElement);
    }

    @Override
    public List<Subtask> getChildElements() {
        return this.getSubtasks();
    }
}

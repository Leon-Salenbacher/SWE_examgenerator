package objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@SuperBuilder
public class Variant implements ChildObject {
    private int id;
    private String title;
    private String question;
    private String solution;

    public Variant(int id, String question, String solution) {
        this.id = id;
        this.question = question;
        this.solution = solution;
    }


    @Override
    public String getTitle() {
        if (title != null && !title.isBlank()) {
            return title;
        }
        return question != null && !question.isBlank() ? question : String.valueOf(id);
    }

    @Override
    public Map<String, String> getAttributes() {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("id", Integer.toString(id));
        attributes.put("title", getTitle() == null ? "" : getTitle());
        attributes.put("question", question == null ? "" : question);
        attributes.put("solution", solution == null ? "" : solution);
        return attributes;
    }
}

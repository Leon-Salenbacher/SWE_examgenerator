package objects;

import lombok.Data;

import java.util.List;

@Data
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

    public void setLabels(List<String> labels) {
        this.labels = defaultLabels(labels);
    }

    @Override
    public String getTitle() {
        return this.question != null && !this.question.isBlank() ? this.question : String.valueOf(this.getId());
    }

}

package objects;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Subtask implements ParentObject<Variant> {
    private int id;
    private String title;
    private int points;
    private int chapterId;

    private List<String> labels;
    private List<Variant> variants;

    public Subtask(int id, String title, int points, int chapterId, List<String> labels, List<Variant> variants) {
        this.id = id;
        this.title = title;
        this.points = points;
        this.chapterId = chapterId;
        this.labels = defaultLabels(labels);
        this.variants = variants;
    }

    public Subtask(int id, String title, int points, int chapterId, List<String> labels) {
        this(id, title, points, chapterId, labels, null);
    }

    public Subtask(int id, String title, int points, int chapterId) {
        this(id, title, points, chapterId, Collections.emptyList(), null);
    }

    private List<String> defaultLabels(List<String> labels) {
        if (labels == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(labels);
    }

    @Override
    public void setChildElements(List<Variant> childElements){
        this.variants = childElements;
    }

    @Override
    public void addChildElement(Variant childElement){
        this.variants.add(childElement);
    }

    @Override
    public List<Variant> getChildElements() {
        return this.getVariants();
    }


}

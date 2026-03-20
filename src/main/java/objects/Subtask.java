package objects;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Subtask implements ParentObject<Variant> {
    private int id;
    private String title;
    private int points;
    private int chapterId;

    private List<String> labels = new ArrayList<>();
    private List<Variant> variants = new ArrayList<>();

    public Subtask(int id, String title, int points, int chapterId, List<String> labels, List<Variant> variants) {
        this.id = id;
        this.title = title;
        this.points = points;
        this.chapterId = chapterId;
        this.labels = defaultLabels(labels);
        this.variants = variants == null ? new ArrayList<>() : new ArrayList<>(variants);
    }

    public Subtask(int id, String title, int points, int chapterId, List<String> labels) {
        this(id, title, points, chapterId, labels, null);
    }

    public Subtask(int id, String title, int points, int chapterId) {
        this(id, title, points, chapterId, List.of(), null);
    }

    private List<String> defaultLabels(List<String> labels) {
        return labels == null ? new ArrayList<>() : new ArrayList<>(labels);
    }

    @Override
    public void setChildElements(List<Variant> childElements){
        this.variants = childElements == null ? new ArrayList<>() : new ArrayList<>(childElements);
    }

    @Override
    public void addChildElement(Variant childElement){
        this.variants.add(childElement);
    }

    @Override
    public List<Variant> getChildElements() {
        return this.getVariants();
    }

    @Override
    public Map<String, String> getAttributes(){
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("id", Integer.toString(id));
        attributes.put("title", title == null ? "" : title);
        attributes.put("points", Integer.toString(points));
        attributes.put("chapterId", Integer.toString(chapterId));
        attributes.put("labels", String.join(",", defaultLabels(labels)));
        return attributes;
    }

}

package objects;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Subtask extends ParentObject<Variant> {
    public static final String ELEMENT_TAG_NAME = "Subtask";
    public static final String CHILD_ELEMENT_TAG ="Variant";
    public static final String POINT_ATTRIBUTE_LABEL ="points";
    public static final String CHAPTER_ID_ATTRIBUTE_LABEL = "chapterId";
    public static final String LABELS_ATTRIBUTE_LABEL = "labels";

    @XmlField(POINT_ATTRIBUTE_LABEL)
    private int points;
    @XmlField(CHAPTER_ID_ATTRIBUTE_LABEL)
    private int chapterId;

    @XmlField(LABELS_ATTRIBUTE_LABEL)
    private List<String> labels = new ArrayList<>();

}

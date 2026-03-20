package objects;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Variant extends ChildObject {
    public static final String ELEMENT_TAG_NAME = "Variant";
    public static final String QUESTION_ATTRIBUTE_LABEL = "question";
    public static final String SOLUTION_ATTRIBUTE_LABEL = "solution";

    @XmlField(QUESTION_ATTRIBUTE_LABEL)
    private String question;

    @XmlField(SOLUTION_ATTRIBUTE_LABEL)
    private String solution;

    @Override
    public String getTitle() {
        String title = super.getTitle();
        if (title != null && !title.isBlank()) {
            return title;
        }
        return question != null && !question.isBlank() ? question : String.valueOf(id);
    }
}

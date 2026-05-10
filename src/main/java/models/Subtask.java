package models;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.*;

/**
 * Exam task with points, difficulty, usage labels and alternative variants.
 */
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
    public static final String DIFFICULTY_ATTRIBUTE_LABEL = "difficulty";

    @XmlField(POINT_ATTRIBUTE_LABEL)
    private double points;
    @XmlField(CHAPTER_ID_ATTRIBUTE_LABEL)
    private int chapterId;
    @Builder.Default
    @XmlField(DIFFICULTY_ATTRIBUTE_LABEL)
    private SubtaskDifficulty difficulty = SubtaskDifficulty.MEDIUM;

    @Builder.Default
    @XmlField(LABELS_ATTRIBUTE_LABEL)
    private List<String> labels = new ArrayList<>();

    /**
     * Derives the exam type from the persisted label list.
     *
     * @return practice only when the practice label is present without the exam label
     */
    public ExamType getExamType() {
        if (hasExamTypeLabel(ExamType.PRACTICE) && !hasExamTypeLabel(ExamType.EXAM)) {
            return ExamType.PRACTICE;
        }
        return ExamType.EXAM;
    }

    /**
     * Updates the label list so it contains exactly one exam type label.
     *
     * @param examType requested exam type, or the default when {@code null}
     */
    public void setExamType(ExamType examType) {
        labels = ExamType.replaceExamTypeLabel(labels, examType);
    }

    /**
     * Checks whether this task may be selected for the requested generated exam type.
     *
     * @param examType requested exam type
     * @return {@code true} if labels match the requested type
     */
    public boolean isEligibleForExamType(ExamType examType) {
        ExamType requestedType = examType == null ? ExamType.defaultType() : examType;
        boolean hasExamLabel = hasExamTypeLabel(ExamType.EXAM);
        boolean hasPracticeLabel = hasExamTypeLabel(ExamType.PRACTICE);

        // Ambiguous labels are excluded so a task cannot silently appear in both outputs.
        if (hasExamLabel && hasPracticeLabel) {
            return false;
        }
        if (!hasExamLabel && !hasPracticeLabel) {
            return requestedType == ExamType.EXAM;
        }
        return requestedType == ExamType.EXAM ? hasExamLabel : hasPracticeLabel;
    }

    private boolean hasExamTypeLabel(ExamType examType) {
        if (labels == null) {
            return false;
        }
        return labels.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .anyMatch(label -> examType.getLabel().equalsIgnoreCase(label));
    }

}

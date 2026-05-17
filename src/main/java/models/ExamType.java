package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Classification of generated output as a real exam or a practice exam.
 */
public enum ExamType {
    EXAM("Exam", "examType.exam"),
    PRACTICE("Practice", "examType.practice");

    private final String label;
    private final String localizationKey;

    ExamType(String label, String localizationKey) {
        this.label = label;
        this.localizationKey = localizationKey;
    }

    /**
     * @return persisted label used in subtask label lists
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return resource bundle key for displaying this type in the UI
     */
    public String getLocalizationKey() {
        return localizationKey;
    }

    /**
     * @return default type used when older data has no explicit exam type
     */
    public static ExamType defaultType() {
        return EXAM;
    }

    /**
     * Checks whether a label value is reserved for exam type classification.
     *
     * @param value raw label value
     * @return {@code true} when the value matches any exam type label
     */
    public static boolean isExamTypeLabel(String value) {
        if (value == null) {
            return false;
        }
        for (ExamType examType : values()) {
            if (examType.label.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replaces any existing exam type label with the requested type label.
     *
     * @param labels existing label list
     * @param examType requested exam type, or the default when {@code null}
     * @return new label list with the exam type label first
     */
    public static List<String> replaceExamTypeLabel(List<String> labels, ExamType examType) {
        List<String> updatedLabels = new ArrayList<>();
        if (labels != null) {
            labels.stream()
                    .filter(label -> !isExamTypeLabel(label))
                    .forEach(updatedLabels::add);
        }

        updatedLabels.add(0, (examType == null ? defaultType() : examType).getLabel());
        return updatedLabels;
    }
}

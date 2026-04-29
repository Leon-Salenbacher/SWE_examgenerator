package models;

import java.util.ArrayList;
import java.util.List;

public enum ExamType {
    EXAM("Exam", "examType.exam"),
    PRACTICE("Practice", "examType.practice");

    private final String label;
    private final String localizationKey;

    ExamType(String label, String localizationKey) {
        this.label = label;
        this.localizationKey = localizationKey;
    }

    public String getLabel() {
        return label;
    }

    public String getLocalizationKey() {
        return localizationKey;
    }

    public static ExamType defaultType() {
        return EXAM;
    }

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

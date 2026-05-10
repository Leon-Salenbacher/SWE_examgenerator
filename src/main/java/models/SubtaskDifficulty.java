package models;

/**
 * Difficulty level used for balancing generated exams.
 */
public enum SubtaskDifficulty {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    private final String xmlValue;

    SubtaskDifficulty(String xmlValue) {
        this.xmlValue = xmlValue;
    }

    /**
     * @return stable XML representation for this difficulty
     */
    public String getXmlValue() {
        return xmlValue;
    }

    /**
     * Parses persisted or legacy difficulty values.
     *
     * @param rawValue raw XML or enum value
     * @return parsed difficulty, defaulting to {@link #MEDIUM}
     */
    public static SubtaskDifficulty fromXmlValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return MEDIUM;
        }

        String normalizedValue = rawValue.trim();
        if ("difficult".equalsIgnoreCase(normalizedValue)) {
            return HARD;
        }

        for (SubtaskDifficulty difficulty : values()) {
            if (difficulty.xmlValue.equalsIgnoreCase(normalizedValue)
                    || difficulty.name().equalsIgnoreCase(normalizedValue)) {
                return difficulty;
            }
        }

        return MEDIUM;
    }
}

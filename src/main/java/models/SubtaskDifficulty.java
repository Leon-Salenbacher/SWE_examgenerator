package models;

public enum SubtaskDifficulty {
    EASY("easy"),
    MEDIUM("medium"),
    HARD("hard");

    private final String xmlValue;

    SubtaskDifficulty(String xmlValue) {
        this.xmlValue = xmlValue;
    }

    public String getXmlValue() {
        return xmlValue;
    }

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

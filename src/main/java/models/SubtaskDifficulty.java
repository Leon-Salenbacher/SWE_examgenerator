package models;

public enum SubtaskDifficulty {
    EASY("easy"),
    MEDIUM("medium"),
    DIFFICULT("difficult");

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

        for (SubtaskDifficulty difficulty : values()) {
            if (difficulty.xmlValue.equalsIgnoreCase(rawValue.trim())
                    || difficulty.name().equalsIgnoreCase(rawValue.trim())) {
                return difficulty;
            }
        }

        return MEDIUM;
    }
}

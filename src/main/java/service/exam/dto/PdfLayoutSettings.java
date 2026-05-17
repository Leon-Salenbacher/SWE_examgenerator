package service.exam.dto;

/**
 * User-configurable PDF layout options for generated exams.
 *
 * @param coverPageEnabled whether an additional cover page should be written
 * @param coverTitle cover page title
 * @param coverSubtitle cover page subtitle
 * @param headerText header text printed on body pages
 * @param footerText footer text printed on body pages
 * @param pageNumbersEnabled whether body pages should display page numbers
 * @param answerBoxHeightPerPoint answer field height per point in PDF units
 */
public record PdfLayoutSettings(
        boolean coverPageEnabled,
        String coverTitle,
        String coverSubtitle,
        String headerText,
        String footerText,
        boolean pageNumbersEnabled,
        int answerBoxHeightPerPoint
) {
    public static final int DEFAULT_ANSWER_BOX_HEIGHT_PER_POINT = 18;
    public static final int MIN_ANSWER_BOX_HEIGHT_PER_POINT = 0;
    public static final int MAX_ANSWER_BOX_HEIGHT_PER_POINT = 60;

    /**
     * Creates default layout settings for a new exam.
     *
     * @param examTitle title used as cover fallback
     * @return default layout settings
     */
    public static PdfLayoutSettings defaults(String examTitle) {
        return new PdfLayoutSettings(false, examTitle, "", "", "", true, DEFAULT_ANSWER_BOX_HEIGHT_PER_POINT);
    }

    /**
     * Normalizes blank text values and clamps numeric settings.
     *
     * @param fallbackExamTitle title used when the cover title is blank
     * @return sanitized layout settings
     */
    public PdfLayoutSettings sanitize(String fallbackExamTitle) {
        return new PdfLayoutSettings(
                coverPageEnabled,
                sanitizeText(coverTitle, fallbackExamTitle),
                sanitizeText(coverSubtitle, ""),
                sanitizeText(headerText, ""),
                sanitizeText(footerText, ""),
                pageNumbersEnabled,
                sanitizeAnswerBoxHeightPerPoint(answerBoxHeightPerPoint)
        );
    }

    /**
     * Clamps answer box height to the supported UI range.
     *
     * @param value requested answer box height per point
     * @return clamped value
     */
    public static int sanitizeAnswerBoxHeightPerPoint(int value) {
        if (value < MIN_ANSWER_BOX_HEIGHT_PER_POINT) {
            return MIN_ANSWER_BOX_HEIGHT_PER_POINT;
        }
        return Math.min(value, MAX_ANSWER_BOX_HEIGHT_PER_POINT);
    }

    /**
     * Builds a short German summary for the layout dialog.
     *
     * @return human-readable settings summary
     */
    public String summary() {
        StringBuilder builder = new StringBuilder();
        builder.append(coverPageEnabled ? "Deckblatt aktiv" : "Kein Deckblatt");
        builder.append(" | ");
        builder.append(headerText == null || headerText.isBlank() ? "ohne Header" : "Header aktiv");
        builder.append(" | ");
        builder.append(footerText == null || footerText.isBlank() ? "ohne Footer" : "Footer aktiv");
        builder.append(" | ");
        builder.append(pageNumbersEnabled ? "Seitenzahlen aktiv" : "ohne Seitenzahlen");
        builder.append(" | Antwortfelder: ");
        builder.append(sanitizeAnswerBoxHeightPerPoint(answerBoxHeightPerPoint));
        builder.append("/Punkt");
        return builder.toString();
    }

    private static String sanitizeText(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? fallback : trimmed;
    }
}

package service.exam.dto;

public record PdfLayoutSettings(
        boolean coverPageEnabled,
        String coverTitle,
        String coverSubtitle,
        String headerText,
        String footerText,
        boolean pageNumbersEnabled
) {

    public static PdfLayoutSettings defaults(String examTitle) {
        return new PdfLayoutSettings(false, examTitle, "", "", "", true);
    }

    public PdfLayoutSettings sanitize(String fallbackExamTitle) {
        return new PdfLayoutSettings(
                coverPageEnabled,
                sanitizeText(coverTitle, fallbackExamTitle),
                sanitizeText(coverSubtitle, ""),
                sanitizeText(headerText, ""),
                sanitizeText(footerText, ""),
                pageNumbersEnabled
        );
    }

    public String summary() {
        StringBuilder builder = new StringBuilder();
        builder.append(coverPageEnabled ? "Deckblatt aktiv" : "Kein Deckblatt");
        builder.append(" | ");
        builder.append(headerText == null || headerText.isBlank() ? "ohne Header" : "Header aktiv");
        builder.append(" | ");
        builder.append(footerText == null || footerText.isBlank() ? "ohne Footer" : "Footer aktiv");
        builder.append(" | ");
        builder.append(pageNumbersEnabled ? "Seitenzahlen aktiv" : "ohne Seitenzahlen");
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

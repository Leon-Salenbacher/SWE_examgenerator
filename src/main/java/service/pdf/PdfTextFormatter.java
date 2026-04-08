package service.pdf;

import java.util.ArrayList;
import java.util.List;

final class PdfTextFormatter {

    List<String> wrap(String text) {
        return wrap(text, PdfLayoutMetrics.MAX_CHARS_PER_LINE);
    }

    List<String> wrap(String text, int maxCharsPerLine) {
        List<String> lines = new ArrayList<>();
        String remaining = sanitize(text);

        while (remaining.length() > maxCharsPerLine) {
            int breakPosition = remaining.lastIndexOf(' ', maxCharsPerLine);
            if (breakPosition <= 0) {
                breakPosition = maxCharsPerLine;
            }
            lines.add(remaining.substring(0, breakPosition).trim());
            remaining = remaining.substring(breakPosition).trim();
        }

        if (!remaining.isBlank()) {
            lines.add(remaining);
        }

        if (lines.isEmpty()) {
            lines.add("");
        }
        return lines;
    }

    String escapePdfText(String text) {
        return sanitize(text)
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    String sanitize(String text) {
        String value = text == null ? "" : text.replace('\r', ' ').replace('\n', ' ');
        StringBuilder sanitized = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            sanitized.append(PdfLayoutMetrics.PDF_ENCODER.canEncode(current) ? current : '?');
        }
        return sanitized.toString();
    }

    String safeLabel(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}

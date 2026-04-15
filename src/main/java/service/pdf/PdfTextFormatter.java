package service.pdf;

import service.pdf.metrics.PdfLayoutMetrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Sanitizes, wraps, and escapes text before it is written into the PDF stream.
 */
final class PdfTextFormatter {

    /**
     * Wraps text using the default PDF body line length.
     *
     * @param text text to wrap
     * @return wrapped lines
     */
    List<String> wrap(String text) {
        return wrap(text, PdfLayoutMetrics.MAX_CHARS_PER_LINE);
    }

    /**
     * Wraps text at a custom maximum line length.
     *
     * @param text text to wrap
     * @param maxCharsPerLine maximum characters per line
     * @return wrapped lines
     */
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

    /**
     * Escapes text characters that have special meaning inside PDF string literals.
     *
     * @param text raw text
     * @return escaped PDF string content
     */
    String escapePdfText(String text) {
        return sanitize(text)
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    /**
     * Normalizes line breaks and replaces characters unsupported by the configured PDF charset.
     *
     * @param text raw text
     * @return sanitized text
     */
    String sanitize(String text) {
        String value = text == null ? "" : text.replace('\r', ' ').replace('\n', ' ');
        StringBuilder sanitized = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            sanitized.append(PdfLayoutMetrics.PDF_ENCODER.canEncode(current) ? current : '?');
        }
        return sanitized.toString();
    }

    /**
     * Returns a trimmed label or a fallback when the label is blank.
     *
     * @param value preferred label
     * @param fallback fallback label
     * @return safe label text
     */
    String safeLabel(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}

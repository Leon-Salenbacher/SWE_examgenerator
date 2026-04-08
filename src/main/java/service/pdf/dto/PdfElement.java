package service.pdf.dto;

import lombok.Builder;
import service.pdf.PdfElementMetrics;

import java.util.List;

@Builder
public record PdfElement(
        String text,
        List<String> boxLines,
        int height,
        boolean answerBox) {
    public static PdfElement text(String text) {
        return new PdfElement(text, List.of(), PdfElementMetrics.lineHeight(), false);
    }

    public static PdfElement answerBox(List<String> boxLines) {
        int height = Math.max(
                PdfElementMetrics.answerBoxMinHeight(),
                (boxLines.size() * PdfElementMetrics.lineHeight())
                        + (PdfElementMetrics.answerBoxPadding() * 2)
                        + PdfElementMetrics.lineHeight()
        );
        return new PdfElement("", boxLines, height, true);
    }
}

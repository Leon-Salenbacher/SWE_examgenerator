package service.pdf.dto;

import lombok.Builder;
import service.pdf.PdfExamWriter;

import java.util.List;

@Builder
public record PdfElement(
        String text,
        List<String> boxLines,
        int height,
        boolean answerBox) {
    public static PdfElement text(String text) {
        return new PdfElement(text, List.of(), PdfExamWriter.getLINE_HEIGHT(), false);
    }

    public static PdfElement answerBox(List<String> boxLines) {
        int height = Math.max(
                PdfExamWriter.getANSWER_BOX_MIN_HEIGHT(),
                (boxLines.size() * PdfExamWriter.getLINE_HEIGHT()) + (PdfExamWriter.getANSWER_BOX_PADDING() * 2) + PdfExamWriter.getLINE_HEIGHT()
        );
        return new PdfElement("", boxLines, height, true);
    }
}

package service.pdf.dto;

import service.pdf.PdfElementMetrics;

import java.util.List;

public record PdfElement(
        String text,
        List<String> boxLines,
        int height,
        boolean answerBox,
        PdfElementType type,
        boolean pageBreakBefore) {
    public static PdfElement text(String text) {
        return styledText(text, PdfElementType.TEXT, PdfElementMetrics.lineHeight(), false);
    }

    public static PdfElement chapterHeading(String text) {
        return styledText(text, PdfElementType.CHAPTER_HEADING, PdfElementMetrics.chapterHeadingHeight(), true);
    }

    public static PdfElement taskHeading(String text) {
        return styledText(text, PdfElementType.TASK_HEADING, PdfElementMetrics.taskHeadingHeight(), false);
    }

    public static PdfElement answerLabel(String text) {
        return styledText(text, PdfElementType.ANSWER_LABEL, PdfElementMetrics.lineHeight(), false);
    }

    public static PdfElement answerBox(List<String> boxLines) {
        int height = Math.max(
                PdfElementMetrics.answerBoxMinHeight(),
                (boxLines.size() * PdfElementMetrics.lineHeight())
                        + (PdfElementMetrics.answerBoxPadding() * 2)
                        + PdfElementMetrics.lineHeight()
        );
        return new PdfElement("", boxLines, height, true, PdfElementType.ANSWER_BOX, false);
    }

    private static PdfElement styledText(String text, PdfElementType type, int height, boolean pageBreakBefore) {
        return new PdfElement(text, List.of(), height, false, type, pageBreakBefore);
    }
}

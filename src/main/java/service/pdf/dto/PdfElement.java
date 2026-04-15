package service.pdf.dto;

import service.pdf.metrics.PdfElementMetrics;

import java.util.List;

/**
 * Renderable piece of PDF body content.
 *
 * @param text text content for non-box elements
 * @param boxLines text lines rendered inside an answer box
 * @param height reserved vertical height in PDF units
 * @param answerBox whether this element renders as an answer box
 * @param type semantic element type
 * @param pageBreakBefore whether pagination should start a new page before this element
 */
public record PdfElement(
        String text,
        List<String> boxLines,
        int height,
        boolean answerBox,
        PdfElementType type,
        boolean pageBreakBefore) {
    /**
     * Creates a default body text element.
     *
     * @param text body text
     * @return text element
     */
    public static PdfElement text(String text) {
        return styledText(text, PdfElementType.TEXT, PdfElementMetrics.lineHeight(), false);
    }

    /**
     * Creates a chapter heading that starts on a new page.
     *
     * @param text heading text
     * @return chapter heading element
     */
    public static PdfElement chapterHeading(String text) {
        return styledText(text, PdfElementType.CHAPTER_HEADING, PdfElementMetrics.chapterHeadingHeight(), true);
    }

    /**
     * Creates a task heading.
     *
     * @param text heading text
     * @return task heading element
     */
    public static PdfElement taskHeading(String text) {
        return styledText(text, PdfElementType.TASK_HEADING, PdfElementMetrics.taskHeadingHeight(), false);
    }

    /**
     * Creates an answer label.
     *
     * @param text label text
     * @return answer label element
     */
    public static PdfElement answerLabel(String text) {
        return styledText(text, PdfElementType.ANSWER_LABEL, PdfElementMetrics.lineHeight(), false);
    }

    /**
     * Creates an answer box sized to fit the given lines while respecting the minimum height.
     *
     * @param boxLines text lines to render inside the box
     * @return answer box element
     */
    public static PdfElement answerBox(List<String> boxLines) {
        int height = Math.max(
                PdfElementMetrics.answerBoxMinHeight(),
                (boxLines.size() * PdfElementMetrics.lineHeight())
                        + (PdfElementMetrics.answerBoxPadding() * 2)
                        + PdfElementMetrics.lineHeight()
        );
        return new PdfElement("", boxLines, height, true, PdfElementType.ANSWER_BOX, false);
    }

    /**
     * Creates spacing after an answer block.
     *
     * @return spacer element
     */
    public static PdfElement answerBlockBottomSpacing() {
        return new PdfElement("", List.of(), PdfElementMetrics.answerBlockBottomSpacing(), false, PdfElementType.SPACER, false);
    }

    private static PdfElement styledText(String text, PdfElementType type, int height, boolean pageBreakBefore) {
        return new PdfElement(text, List.of(), height, false, type, pageBreakBefore);
    }
}

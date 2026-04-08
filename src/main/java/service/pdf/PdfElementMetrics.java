package service.pdf;

public final class PdfElementMetrics {

    private PdfElementMetrics() {
    }

    public static int lineHeight() {
        return PdfLayoutMetrics.LINE_HEIGHT;
    }

    public static int chapterHeadingHeight() {
        return PdfLayoutMetrics.CHAPTER_HEADING_HEIGHT;
    }

    public static int taskHeadingHeight() {
        return PdfLayoutMetrics.TASK_HEADING_HEIGHT;
    }

    public static int answerBoxMinHeight() {
        return PdfLayoutMetrics.ANSWER_BOX_MIN_HEIGHT;
    }

    public static int answerBoxPadding() {
        return PdfLayoutMetrics.ANSWER_BOX_PADDING;
    }
}

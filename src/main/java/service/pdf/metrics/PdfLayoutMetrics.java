package service.pdf.metrics;

import java.nio.charset.CharsetEncoder;

/**
 * Central layout constants for generated PDF pages.
 */
public final class PdfLayoutMetrics {

    /** Charset used for the raw PDF stream. */
    public static final java.nio.charset.Charset PDF_CHARSET = PdfMetricsConfig.charsetValue("pdf.charset", "windows-1252");
    /** Encoder used to replace unsupported characters before writing PDF text. */
    public static final CharsetEncoder PDF_ENCODER = PDF_CHARSET.newEncoder();
    /** Default maximum line length used before text wrapping. */
    public static final int MAX_CHARS_PER_LINE = PdfMetricsConfig.intValue("layout.maxCharsPerLine", 90);
    /** Body start position for pages without a header. */
    public static final int DEFAULT_START_Y = PdfMetricsConfig.intValue("layout.defaultStartY", 790);
    /** Body start position for pages with a header. */
    public static final int HEADER_START_Y = PdfMetricsConfig.intValue("layout.headerStartY", 760);
    /** Lower body boundary for pages without footer or page numbers. */
    public static final int BODY_BOTTOM_Y = PdfMetricsConfig.intValue("layout.bodyBottomY", 45);
    /** Lower body boundary for pages with footer or page numbers. */
    public static final int BODY_BOTTOM_WITH_FOOTER_Y = PdfMetricsConfig.intValue("layout.bodyBottomWithFooterY", 70);
    /** Footer baseline position. */
    public static final int FOOTER_Y = PdfMetricsConfig.intValue("layout.footerY", 45);
    /** Page number baseline position. */
    public static final int PAGE_NUMBER_Y = PdfMetricsConfig.intValue("layout.pageNumberY", 28);
    /** Cover page title baseline position. */
    public static final int COVER_TITLE_Y = PdfMetricsConfig.intValue("layout.coverTitleY", 470);
    /** Cover page subtitle baseline position. */
    public static final int COVER_SUBTITLE_Y = PdfMetricsConfig.intValue("layout.coverSubtitleY", 430);
    /** Default body line height. */
    public static final int LINE_HEIGHT = PdfMetricsConfig.intValue("layout.lineHeight", 14);
    /** Reserved height for chapter headings. */
    public static final int CHAPTER_HEADING_HEIGHT = PdfMetricsConfig.intValue("layout.chapterHeadingHeight", 36);
    /** Reserved height for task headings. */
    public static final int TASK_HEADING_HEIGHT = PdfMetricsConfig.intValue("layout.taskHeadingHeight", 22);
    /** Left margin for body content. */
    public static final int BODY_X = PdfMetricsConfig.intValue("layout.bodyX", 50);
    /** Width of answer boxes. */
    public static final int ANSWER_BOX_WIDTH = PdfMetricsConfig.intValue("layout.answerBoxWidth", 495);
    /** Minimum answer box height. */
    public static final int ANSWER_BOX_MIN_HEIGHT = PdfMetricsConfig.intValue("layout.answerBoxMinHeight", 86);
    /** Answer box height reserved per achievable point. */
    public static final int ANSWER_BOX_HEIGHT_PER_POINT = PdfMetricsConfig.intValue("layout.answerBoxHeightPerPoint", 18);
    /** Inner padding for answer boxes. */
    public static final int ANSWER_BOX_PADDING = PdfMetricsConfig.intValue("layout.answerBoxPadding", 8);
    /** Spacing after answer boxes before the next heading. */
    public static final int ANSWER_BLOCK_BOTTOM_SPACING = PdfMetricsConfig.intValue("layout.answerBlockBottomSpacing", 18);
    /** Maximum line length for solution text inside answer boxes. */
    public static final int ANSWER_BOX_TEXT_MAX_CHARS = PdfMetricsConfig.intValue("layout.answerBoxTextMaxChars", 78);
    /** Table of contents title baseline position. */
    public static final int TOC_TITLE_Y = PdfMetricsConfig.intValue("layout.tocTitleY", 770);
    /** Top y-position of the table of contents grid. */
    public static final int TOC_TABLE_TOP_Y = PdfMetricsConfig.intValue("layout.tocTableTopY", 720);
    /** Table of contents row height. */
    public static final int TOC_ROW_HEIGHT = PdfMetricsConfig.intValue("layout.tocRowHeight", 24);

    private PdfLayoutMetrics() {
    }

    /**
     * Calculates the body start y-position depending on whether a header is rendered.
     *
     * @param headerText configured header text
     * @return y-position where body content should start
     */
    public static int calculateBodyStartY(String headerText) {
        return headerText == null || headerText.isBlank() ? DEFAULT_START_Y : HEADER_START_Y;
    }

    /**
     * Calculates the lower body boundary while reserving space for footer or page numbers.
     *
     * @param footerText configured footer text
     * @param pageNumbersEnabled whether page numbers are rendered
     * @return minimum y-position available to body content
     */
    public static int calculateBodyBottomY(String footerText, boolean pageNumbersEnabled) {
        if ((footerText != null && !footerText.isBlank()) || pageNumbersEnabled) {
            return BODY_BOTTOM_WITH_FOOTER_Y;
        }
        return BODY_BOTTOM_Y;
    }
}

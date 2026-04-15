package service.pdf.metrics;

import service.pdf.dto.PdfElementType;

import java.util.Map;

/**
 * Collects element-specific measurements and text styles used while rendering PDF body elements.
 */
public final class PdfElementMetrics {

    private static final int FONT_SIZE_DEFAULT = PdfMetricsConfig.intValue("fontSizes.default", 12);

    private static final Map<PdfElementType, Integer> FONT_SIZES = Map.of(
            PdfElementType.CHAPTER_HEADING, PdfMetricsConfig.intValue("fontSizes.chapterHeading", 20),
            PdfElementType.TASK_HEADING, PdfMetricsConfig.intValue("fontSizes.taskHeading", 13),
            PdfElementType.ANSWER_LABEL, PdfMetricsConfig.intValue("fontSizes.answerLabel", 12)
    );
    private static final Map<PdfElementType, Boolean> BOLD_STYLES = Map.of(
            PdfElementType.CHAPTER_HEADING, PdfMetricsConfig.booleanValue("boldStyles.chapterHeading", true),
            PdfElementType.TASK_HEADING, PdfMetricsConfig.booleanValue("boldStyles.taskHeading", true),
            PdfElementType.ANSWER_LABEL, PdfMetricsConfig.booleanValue("boldStyles.answerLabel", true)
    );

    private PdfElementMetrics() {
    }

    /**
     * Returns the configured font size for the given element type.
     *
     * @param elementType element type to style
     * @return configured font size or the default body font size
     */
    public static int fontSizeFor(PdfElementType elementType) {
        return FONT_SIZES.getOrDefault(elementType, FONT_SIZE_DEFAULT);
    }

    /**
     * Returns whether the given element type should be rendered with the bold PDF font.
     *
     * @param elementType element type to style
     * @return true when the element should be rendered bold
     */
    public static boolean boldFor(PdfElementType elementType) {
        return BOLD_STYLES.getOrDefault(elementType, false);
    }

    /**
     * Returns the height used by one body text line.
     *
     * @return line height in PDF units
     */
    public static int lineHeight() {
        return PdfLayoutMetrics.LINE_HEIGHT;
    }

    /**
     * Returns the reserved height for a chapter heading.
     *
     * @return chapter heading height in PDF units
     */
    public static int chapterHeadingHeight() {
        return PdfLayoutMetrics.CHAPTER_HEADING_HEIGHT;
    }

    /**
     * Returns the reserved height for a task heading.
     *
     * @return task heading height in PDF units
     */
    public static int taskHeadingHeight() {
        return PdfLayoutMetrics.TASK_HEADING_HEIGHT;
    }

    /**
     * Returns the minimum answer box height.
     *
     * @return minimum answer box height in PDF units
     */
    public static int answerBoxMinHeight() {
        return PdfLayoutMetrics.ANSWER_BOX_MIN_HEIGHT;
    }

    /**
     * Returns the inner answer box padding.
     *
     * @return padding in PDF units
     */
    public static int answerBoxPadding() {
        return PdfLayoutMetrics.ANSWER_BOX_PADDING;
    }

    /**
     * Returns the spacing after an answer block.
     *
     * @return spacing in PDF units
     */
    public static int answerBlockBottomSpacing() {
        return PdfLayoutMetrics.ANSWER_BLOCK_BOTTOM_SPACING;
    }

}

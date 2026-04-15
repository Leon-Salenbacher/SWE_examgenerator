package service.pdf.dto;

/**
 * Semantic types of renderable PDF body elements.
 */
public enum PdfElementType {
    /** Default body text. */
    TEXT,
    /** Chapter heading. */
    CHAPTER_HEADING,
    /** Task heading. */
    TASK_HEADING,
    /** Answer label text. */
    ANSWER_LABEL,
    /** Answer box rectangle. */
    ANSWER_BOX,
    /** Vertical spacing without visible output. */
    SPACER
}

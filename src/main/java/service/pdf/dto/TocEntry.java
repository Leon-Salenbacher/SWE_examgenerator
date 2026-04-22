package service.pdf.dto;

/**
 * One row in the generated table of contents.
 *
 * @param page logical page number of the chapter
 * @param chapter chapter title shown in the table of contents
 * @param possiblePoints total possible points for the chapter
 */
public record TocEntry(
        int page,
        String chapter,
        int possiblePoints
) {
}

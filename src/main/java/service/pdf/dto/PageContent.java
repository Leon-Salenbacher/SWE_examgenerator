package service.pdf.dto;

import service.exam.dto.PdfLayoutSettings;

import java.util.List;

/**
 * Describes the content and layout metadata for one generated PDF page.
 *
 * @param coverPage whether this page is a cover page
 * @param tableOfContentsPage whether this page is the table of contents
 * @param coverTitle cover title text
 * @param coverSubtitle cover subtitle text
 * @param bodyElements renderable body elements
 * @param tocEntries table-of-contents rows
 * @param layoutSettings layout settings used for this page
 * @param logicalPageNumber visible logical page number
 */
public record PageContent(
        boolean coverPage,
        boolean tableOfContentsPage,
        String coverTitle,
        String coverSubtitle,
        List<PdfElement> bodyElements,
        List<TocEntry> tocEntries,
        PdfLayoutSettings layoutSettings,
        int logicalPageNumber
) {
    /**
     * Creates a cover page descriptor.
     *
     * @param coverTitle cover title text
     * @param coverSubtitle cover subtitle text
     * @return cover page content
     */
    public static PageContent cover(String coverTitle, String coverSubtitle) {
        return new PageContent(true, false, coverTitle, coverSubtitle, List.of(), List.of(), null, 0);
    }

    /**
     * Creates a table-of-contents page descriptor.
     *
     * @param tocEntries table-of-contents rows
     * @param layoutSettings layout settings used for the page frame
     * @param logicalPageNumber visible logical page number
     * @return table-of-contents page content
     */
    public static PageContent tableOfContents(List<TocEntry> tocEntries, PdfLayoutSettings layoutSettings, int logicalPageNumber) {
        return new PageContent(false, true, "", "", List.of(), tocEntries, layoutSettings, logicalPageNumber);
    }

    /**
     * Creates a body page descriptor.
     *
     * @param bodyElements renderable body elements
     * @param layoutSettings layout settings used for the page frame
     * @param logicalPageNumber visible logical page number
     * @return body page content
     */
    public static PageContent body(List<PdfElement> bodyElements, PdfLayoutSettings layoutSettings, int logicalPageNumber) {
        return new PageContent(false, false, "", "", bodyElements, List.of(), layoutSettings, logicalPageNumber);
    }
}

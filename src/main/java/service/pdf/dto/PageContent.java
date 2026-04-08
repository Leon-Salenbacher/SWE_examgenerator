package service.pdf.dto;

import service.exam.dto.PdfLayoutSettings;

import java.util.List;

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
    public static PageContent cover(String coverTitle, String coverSubtitle) {
        return new PageContent(true, false, coverTitle, coverSubtitle, List.of(), List.of(), null, 0);
    }

    public static PageContent tableOfContents(List<TocEntry> tocEntries, PdfLayoutSettings layoutSettings, int logicalPageNumber) {
        return new PageContent(false, true, "", "", List.of(), tocEntries, layoutSettings, logicalPageNumber);
    }

    public static PageContent body(List<PdfElement> bodyElements, PdfLayoutSettings layoutSettings, int logicalPageNumber) {
        return new PageContent(false, false, "", "", bodyElements, List.of(), layoutSettings, logicalPageNumber);
    }
}

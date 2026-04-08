package service.pdf.dto;

import lombok.Builder;
import service.exam.dto.PdfLayoutSettings;

import java.util.List;

@Builder
public record PageContent(
        boolean coverPage,
        String coverTitle,
        String coverSubtitle,
        List<PdfElement> bodyElements,
        PdfLayoutSettings layoutSettings,
        int logicalPageNumber
) {
    public static PageContent cover(String coverTitle, String coverSubtitle) {
        return new PageContent(true, coverTitle, coverSubtitle, List.of(), null, 0);
    }

    public static PageContent body(List<PdfElement> bodyElements, PdfLayoutSettings layoutSettings, int logicalPageNumber) {
        return new PageContent(false, "", "", bodyElements, layoutSettings, logicalPageNumber);
    }
}

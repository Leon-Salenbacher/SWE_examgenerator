package service.pdf;

import service.exam.dto.PdfLayoutSettings;
import service.pdf.dto.PageContent;
import service.pdf.dto.PdfElement;

import java.util.ArrayList;
import java.util.List;

final class PdfPaginator {

    List<PageContent> paginate(List<PdfElement> elements, PdfLayoutSettings settings) {
        List<PageContent> pages = new ArrayList<>();
        if (settings.coverPageEnabled()) {
            pages.add(PageContent.cover(settings.coverTitle(), settings.coverSubtitle()));
        }

        int startY = PdfLayoutMetrics.calculateBodyStartY(settings.headerText());
        int bottomY = PdfLayoutMetrics.calculateBodyBottomY(settings.footerText(), settings.pageNumbersEnabled());
        int maxElementHeightPerPage = Math.max(1, startY - bottomY);

        List<PdfElement> currentPage = new ArrayList<>();
        int currentPageHeight = 0;
        int logicalPageNumber = 1;
        for (PdfElement element : elements) {
            if (!currentPage.isEmpty() && currentPageHeight + element.height() > maxElementHeightPerPage) {
                pages.add(PageContent.body(new ArrayList<>(currentPage), settings, logicalPageNumber));
                logicalPageNumber++;
                currentPage = new ArrayList<>();
                currentPageHeight = 0;
            }
            currentPage.add(element);
            currentPageHeight += element.height();
        }

        if (currentPage.isEmpty()) {
            currentPage.add(PdfElement.text(" "));
        }
        pages.add(PageContent.body(currentPage, settings, logicalPageNumber));
        return pages;
    }
}

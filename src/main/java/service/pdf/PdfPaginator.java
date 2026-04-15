package service.pdf;

import service.exam.dto.PdfLayoutSettings;
import service.pdf.dto.PageContent;
import service.pdf.dto.PdfElement;
import service.pdf.metrics.PdfLayoutMetrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits PDF body elements into pages according to layout boundaries and forced breaks.
 */
final class PdfPaginator {

    /**
     * Paginates elements and optionally prepends a cover page.
     *
     * @param elements body elements to paginate
     * @param settings layout settings that define body boundaries
     * @return generated pages
     */
    List<PageContent> paginate(List<PdfElement> elements, PdfLayoutSettings settings) {
        List<PageContent> pages = new ArrayList<>();
        if (settings.coverPageEnabled()) {
            pages.add(PageContent.cover(settings.coverTitle(), settings.coverSubtitle()));
        }
        pages.addAll(paginateBody(elements, settings, 1));
        return pages;
    }

    /**
     * Paginates body elements starting at the provided logical page number.
     *
     * @param elements body elements to paginate
     * @param settings layout settings that define body boundaries
     * @param firstLogicalPageNumber logical page number of the first body page
     * @return generated body pages
     */
    List<PageContent> paginateBody(List<PdfElement> elements, PdfLayoutSettings settings, int firstLogicalPageNumber) {
        List<PageContent> pages = new ArrayList<>();

        int startY = PdfLayoutMetrics.calculateBodyStartY(settings.headerText());
        int bottomY = PdfLayoutMetrics.calculateBodyBottomY(settings.footerText(), settings.pageNumbersEnabled());
        int maxElementHeightPerPage = Math.max(1, startY - bottomY);

        List<PdfElement> currentPage = new ArrayList<>();
        int currentPageHeight = 0;
        int logicalPageNumber = firstLogicalPageNumber;
        for (PdfElement element : elements) {
            if (!currentPage.isEmpty()
                    && (element.pageBreakBefore() || currentPageHeight + element.height() > maxElementHeightPerPage)) {
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

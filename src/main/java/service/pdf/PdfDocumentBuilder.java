package service.pdf;

import models.Points;
import service.exam.dto.PdfLayoutSettings;
import service.pdf.dto.PageContent;
import service.pdf.dto.PdfElement;
import service.pdf.dto.PdfElementType;
import service.pdf.metrics.PdfElementMetrics;
import service.pdf.metrics.PdfLayoutMetrics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the raw PDF document bytes from paginated page content.
 */
final class PdfDocumentBuilder {

    private static final int CATALOG_OBJECT_NUMBER = 1;
    private static final int PAGES_OBJECT_NUMBER = 2;
    private static final int REGULAR_FONT_OBJECT_NUMBER = 3;
    private static final int BOLD_FONT_OBJECT_NUMBER = 4;
    private static final int FIRST_PAGE_OBJECT_NUMBER = 5;
    private static final int PAGE_OBJECT_STRIDE = 2;

    private final PdfTextFormatter textFormatter;

    PdfDocumentBuilder(PdfTextFormatter textFormatter) {
        this.textFormatter = textFormatter;
    }

    /**
     * Serializes all pages into a complete PDF file.
     *
     * @param pages pages to write
     * @return binary PDF content
     * @throws IOException if the PDF byte stream cannot be written
     */
    byte[] buildPdfDocument(List<PageContent> pages) throws IOException {
        ByteArrayOutputStream document = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        writePdfHeader(document);
        writeCatalogObject(document, offsets);
        writePagesObject(document, offsets, pages.size());
        writeFontObjects(document, offsets);
        writePageObjects(document, offsets, pages);
        writeCrossReferenceAndTrailer(document, offsets);

        return document.toByteArray();
    }

    private void writePdfHeader(ByteArrayOutputStream document) throws IOException {
        write(document, "%PDF-1.4\n");
        write(document, "%\u00E2\u00E3\u00CF\u00D3\n");
    }

    private void writeCatalogObject(ByteArrayOutputStream document, List<Integer> offsets) throws IOException {
        writeObject(document, offsets, CATALOG_OBJECT_NUMBER,
                "<< /Type /Catalog /Pages " + PAGES_OBJECT_NUMBER + " 0 R >>");
    }

    private void writePagesObject(ByteArrayOutputStream document, List<Integer> offsets, int pageCount) throws IOException {
        StringBuilder pagesObject = new StringBuilder("<< /Type /Pages /Kids [");
        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            int pageObjectNumber = pageObjectNumber(pageIndex);
            pagesObject.append(pageObjectNumber).append(" 0 R ");
        }
        pagesObject.append("] /Count ").append(pageCount).append(" >>");
        writeObject(document, offsets, PAGES_OBJECT_NUMBER, pagesObject.toString());
    }

    private void writeFontObjects(ByteArrayOutputStream document, List<Integer> offsets) throws IOException {
        writeObject(document, offsets, REGULAR_FONT_OBJECT_NUMBER,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        writeObject(document, offsets, BOLD_FONT_OBJECT_NUMBER,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");
    }

    private void writePageObjects(
            ByteArrayOutputStream document,
            List<Integer> offsets,
            List<PageContent> pages
    ) throws IOException {
        for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
            int pageObjectNumber = pageObjectNumber(pageIndex);
            int contentObjectNumber = contentObjectNumber(pageIndex);

            String pageObject = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 " + REGULAR_FONT_OBJECT_NUMBER + " 0 R /F2 "
                    + BOLD_FONT_OBJECT_NUMBER + " 0 R >> >> "
                    + "/Contents " + contentObjectNumber + " 0 R >>";
            writeObject(document, offsets, pageObjectNumber, pageObject);

            byte[] contentBytes = buildContentStream(pages.get(pageIndex)).getBytes(PdfLayoutMetrics.PDF_CHARSET);
            writeStreamObject(document, offsets, contentObjectNumber, contentBytes);
        }
    }

    private int pageObjectNumber(int pageIndex) {
        return FIRST_PAGE_OBJECT_NUMBER + (pageIndex * PAGE_OBJECT_STRIDE);
    }

    private int contentObjectNumber(int pageIndex) {
        return pageObjectNumber(pageIndex) + 1;
    }

    private void writeCrossReferenceAndTrailer(ByteArrayOutputStream document, List<Integer> offsets) throws IOException {
        int xrefOffset = document.size();
        write(document, "xref\n");
        write(document, "0 " + offsets.size() + "\n");
        write(document, "0000000000 65535 f \n");
        for (int index = 1; index < offsets.size(); index++) {
            write(document, String.format("%010d 00000 n %n", offsets.get(index)));
        }

        write(document, "trailer\n");
        write(document, "<< /Size " + offsets.size() + " /Root 1 0 R >>\n");
        write(document, "startxref\n");
        write(document, Integer.toString(xrefOffset));
        write(document, "\n%%EOF");
    }

    /**
     * Builds the content stream for one page.
     *
     * @param page page content to render
     * @return raw PDF content stream text
     */
    private String buildContentStream(PageContent page) {
        StringBuilder builder = new StringBuilder();

        if (page.coverPage()) {
            appendCoverPage(builder, page);
        } else if (page.tableOfContentsPage()) {
            appendTableOfContents(builder, page);
        } else {
            appendExamContentPage(builder, page);
        }

        return builder.toString();
    }

    private void appendCoverPage(StringBuilder builder, PageContent page) {
        appendText(builder, 120, PdfLayoutMetrics.COVER_TITLE_Y, 28, true, page.coverTitle());
        if (hasText(page.coverSubtitle())) {
            appendText(builder, 120, PdfLayoutMetrics.COVER_SUBTITLE_Y, 16, false, page.coverSubtitle());
        }
    }

    private void appendExamContentPage(StringBuilder builder, PageContent page) {
        PdfLayoutSettings settings = page.layoutSettings();

        appendHeader(builder, settings);
        appendBodyElements(builder, page.bodyElements(), PdfLayoutMetrics.calculateBodyStartY(settings.headerText()));
        appendFooter(builder, settings);
        appendPageNumber(builder, settings, page.logicalPageNumber());
    }

    /**
     * Appends all body elements to the current page stream.
     *
     * @param builder target PDF stream
     * @param elements elements to render
     * @param startY first body y-position
     */
    private void appendBodyElements(StringBuilder builder, List<PdfElement> elements, int startY) {
        int currentY = startY;
        for (PdfElement element : elements) {
            if (element.answerBox()) {
                appendAnswerBox(builder, PdfLayoutMetrics.BODY_X, currentY - element.height(),
                        PdfLayoutMetrics.ANSWER_BOX_WIDTH, element.height(), element.boxLines());
            } else if (element.type() != PdfElementType.SPACER) {
                appendText(builder, PdfLayoutMetrics.BODY_X, currentY,
                        PdfElementMetrics.fontSizeFor(element.type()),
                        PdfElementMetrics.boldFor(element.type()),
                        element.text());
            }
            currentY -= element.height();
        }
    }


    /**
     * Appends the table of contents page.
     *
     * @param builder target PDF stream
     * @param page table-of-contents page content
     */
    private void appendTableOfContents(StringBuilder builder, PageContent page) {
        appendPageFrame(builder, page.layoutSettings(), page.logicalPageNumber());
        appendText(builder, PdfLayoutMetrics.BODY_X, PdfLayoutMetrics.TOC_TITLE_Y, 20, true, "Inhaltsverzeichnis");

        int topY = PdfLayoutMetrics.TOC_TABLE_TOP_Y;
        appendTableRow(builder, topY, TocTableRow.headerRow());
        int currentY = topY - PdfLayoutMetrics.TOC_ROW_HEIGHT;
        for (var entry : page.tocEntries()) {
            appendTableRow(builder, currentY, TocTableRow.entry(
                    Integer.toString(entry.page()),
                    entry.chapter(),
                    Points.format(entry.possiblePoints())
            ));
            currentY -= PdfLayoutMetrics.TOC_ROW_HEIGHT;
        }
    }

    /**
     * Appends header, footer, and page number text.
     *
     * @param builder target PDF stream
     * @param settings layout settings for the page frame
     * @param logicalPageNumber visible logical page number
     */
    private void appendPageFrame(StringBuilder builder, PdfLayoutSettings settings, int logicalPageNumber) {
        appendHeader(builder, settings);
        appendFooter(builder, settings);
        appendPageNumber(builder, settings, logicalPageNumber);
    }

    private void appendHeader(StringBuilder builder, PdfLayoutSettings settings) {
        if (hasText(settings.headerText())) {
            appendText(builder, 50, 812, 10, false, settings.headerText());
        }
    }

    private void appendFooter(StringBuilder builder, PdfLayoutSettings settings) {
        if (hasText(settings.footerText())) {
            appendText(builder, 50, PdfLayoutMetrics.FOOTER_Y, 10, false, settings.footerText());
        }
    }

    private void appendPageNumber(StringBuilder builder, PdfLayoutSettings settings, int logicalPageNumber) {
        if (settings.pageNumbersEnabled()) {
            appendText(builder, 500, PdfLayoutMetrics.PAGE_NUMBER_Y, 10, false, "Page " + logicalPageNumber);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * Appends one table-of-contents row.
     *
     * @param builder target PDF stream
     * @param y row baseline y-position
     * @param row all content that should be written in this row
     */
    private void appendTableRow(
            StringBuilder builder,
            int y,
            TocTableRow row
    ) {
        appendRectangle(builder, PdfLayoutMetrics.BODY_X, y - 16, 495, PdfLayoutMetrics.TOC_ROW_HEIGHT);
        appendVerticalLine(builder, 100, y - 16, PdfLayoutMetrics.TOC_ROW_HEIGHT);
        appendVerticalLine(builder, 350, y - 16, PdfLayoutMetrics.TOC_ROW_HEIGHT);
        appendVerticalLine(builder, 445, y - 16, PdfLayoutMetrics.TOC_ROW_HEIGHT);
        int textY = y - 7;
        appendCenteredText(builder, 50, 50, textY, 10, row.header(), row.page());
        appendCenteredText(builder, 100, 250, textY, 10, row.header(), truncate(row.chapter(), 42));
        appendCenteredText(builder, 350, 95, textY, 10, row.header(), row.possiblePoints());
        appendCenteredText(builder, 445, 100, textY, 10, row.header(), row.achievedPoints());
    }

    private record TocTableRow(
            boolean header,
            String page,
            String chapter,
            String possiblePoints,
            String achievedPoints
    ) {
        private static TocTableRow headerRow() {
            return new TocTableRow(true, "Seite", "Chapter", "M\u00f6gliche Punkte", "Erreichte Punkte");
        }

        private static TocTableRow entry(String page, String chapter, String possiblePoints) {
            return new TocTableRow(false, page, chapter, possiblePoints, "");
        }
    }

    /**
     * Truncates long table text with an ellipsis.
     *
     * @param value text to truncate
     * @param maxLength maximum allowed length
     * @return truncated text
     */
    private String truncate(String value, int maxLength) {
        String safeValue = value == null ? "" : value;
        return safeValue.length() <= maxLength ? safeValue : safeValue.substring(0, maxLength - 3) + "...";
    }

    /**
     * Appends a stroked rectangle.
     *
     * @param builder target PDF stream
     * @param x left x-position
     * @param y bottom y-position
     * @param width rectangle width
     * @param height rectangle height
     */
    private void appendRectangle(StringBuilder builder, int x, int y, int width, int height) {
        builder.append("q\n");
        builder.append("0.65 0.65 0.65 RG\n");
        builder.append("0.6 w\n");
        builder.append(x).append(" ").append(y).append(" ").append(width).append(" ").append(height).append(" re\n");
        builder.append("S\n");
        builder.append("Q\n");
    }

    /**
     * Appends a vertical line.
     *
     * @param builder target PDF stream
     * @param x x-position
     * @param y bottom y-position
     * @param height line height
     */
    private void appendVerticalLine(StringBuilder builder, int x, int y, int height) {
        builder.append("q\n");
        builder.append("0.65 0.65 0.65 RG\n");
        builder.append("0.6 w\n");
        builder.append(x).append(" ").append(y).append(" m\n");
        builder.append(x).append(" ").append(y + height).append(" l\n");
        builder.append("S\n");
        builder.append("Q\n");
    }

    /**
     * Appends text centered within a table cell.
     *
     * @param builder target PDF stream
     * @param cellX left cell x-position
     * @param cellWidth table cell width
     * @param y text y-position
     * @param fontSize font size
     * @param bold whether to use the bold font
     * @param text text to render
     */
    private void appendCenteredText(
            StringBuilder builder,
            int cellX,
            int cellWidth,
            int y,
            int fontSize,
            boolean bold,
            String text
    ) {
        String safeText = text == null ? "" : text;
        int textWidth = Math.max(1, (int) Math.round(safeText.length() * fontSize * 0.52));
        int x = cellX + Math.max(4, (cellWidth - textWidth) / 2);
        appendText(builder, x, y, fontSize, bold, safeText);
    }

    /**
     * Appends one text draw operation.
     *
     * @param builder target PDF stream
     * @param x text x-position
     * @param y text y-position
     * @param fontSize font size
     * @param bold whether to use the bold font
     * @param text text to render
     */
    private void appendText(StringBuilder builder, int x, int y, int fontSize, boolean bold, String text) {
        builder.append("BT\n");
        builder.append("1 0 0 1 ").append(x).append(" ").append(y).append(" Tm\n");
        builder.append(bold ? "/F2 " : "/F1 ").append(fontSize).append(" Tf\n");
        builder.append("(").append(textFormatter.escapePdfText(text)).append(") Tj\n");
        builder.append("ET\n");
    }

    /**
     * Appends an answer box rectangle and optional solution text.
     *
     * @param builder target PDF stream
     * @param x left x-position
     * @param y bottom y-position
     * @param width box width
     * @param height box height
     * @param boxLines lines to render inside the box
     */
    private void appendAnswerBox(
            StringBuilder builder,
            int x,
            int y,
            int width,
            int height,
            List<String> boxLines
    ) {
        builder.append("q\n");
        builder.append("0.55 0.55 0.55 RG\n");
        builder.append("0.8 w\n");
        builder.append(x).append(" ").append(y).append(" ").append(width).append(" ").append(height).append(" re\n");
        builder.append("S\n");
        builder.append("Q\n");

        int textY = y + height - PdfLayoutMetrics.ANSWER_BOX_PADDING - PdfLayoutMetrics.LINE_HEIGHT;
        int textX = x + PdfLayoutMetrics.ANSWER_BOX_PADDING;
        for (String boxLine : boxLines) {
            if (textY <= y + PdfLayoutMetrics.ANSWER_BOX_PADDING) {
                break;
            }
            appendText(builder, textX, textY, 11, false, boxLine);
            textY -= PdfLayoutMetrics.LINE_HEIGHT;
        }
    }

    /**
     * Writes one plain PDF object and stores its byte offset.
     *
     * @param document target PDF document stream
     * @param offsets collected PDF object offsets
     * @param objectNumber PDF object number
     * @param objectBody object body content
     * @throws IOException if writing fails
     */
    private void writeObject(ByteArrayOutputStream document, List<Integer> offsets, int objectNumber, String objectBody) throws IOException {
        offsets.add(document.size());
        write(document, objectNumber + " 0 obj\n");
        write(document, objectBody);
        write(document, "\nendobj\n");
    }

    /**
     * Writes one PDF stream object and stores its byte offset.
     *
     * @param document target PDF document stream
     * @param offsets collected PDF object offsets
     * @param objectNumber PDF object number
     * @param streamBytes stream bytes to embed
     * @throws IOException if writing fails
     */
    private void writeStreamObject(ByteArrayOutputStream document, List<Integer> offsets, int objectNumber, byte[] streamBytes) throws IOException {
        offsets.add(document.size());
        write(document, objectNumber + " 0 obj\n");
        write(document, "<< /Length " + streamBytes.length + " >>\n");
        write(document, "stream\n");
        document.write(streamBytes);
        write(document, "\nendstream\n");
        write(document, "endobj\n");
    }

    /**
     * Writes text using the configured PDF charset.
     *
     * @param document target PDF document stream
     * @param content text to write
     * @throws IOException if writing fails
     */
    private void write(ByteArrayOutputStream document, String content) throws IOException {
        document.write(content.getBytes(PdfLayoutMetrics.PDF_CHARSET));
    }
}

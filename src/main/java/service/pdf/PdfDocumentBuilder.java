package service.pdf;

import service.exam.dto.PdfLayoutSettings;
import service.pdf.dto.PageContent;
import service.pdf.dto.PdfElement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class PdfDocumentBuilder {

    private final PdfTextFormatter textFormatter;

    PdfDocumentBuilder(PdfTextFormatter textFormatter) {
        this.textFormatter = textFormatter;
    }

    byte[] buildPdfDocument(List<PageContent> pages) throws IOException {
        int pageCount = pages.size();
        int fontObjectNumber = 3;
        int nextObjectNumber = 4;

        ByteArrayOutputStream document = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        write(document, "%PDF-1.4\n");
        write(document, "%\u00E2\u00E3\u00CF\u00D3\n");

        writeObject(document, offsets, 1, "<< /Type /Catalog /Pages 2 0 R >>");

        StringBuilder pagesObject = new StringBuilder("<< /Type /Pages /Kids [");
        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            int pageObjectNumber = nextObjectNumber + (pageIndex * 2);
            pagesObject.append(pageObjectNumber).append(" 0 R ");
        }
        pagesObject.append("] /Count ").append(pageCount).append(" >>");
        writeObject(document, offsets, 2, pagesObject.toString());

        writeObject(document, offsets, fontObjectNumber,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");

        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            int pageObjectNumber = nextObjectNumber + (pageIndex * 2);
            int contentObjectNumber = pageObjectNumber + 1;

            String pageObject = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 " + fontObjectNumber + " 0 R >> >> "
                    + "/Contents " + contentObjectNumber + " 0 R >>";
            writeObject(document, offsets, pageObjectNumber, pageObject);

            byte[] contentBytes = buildContentStream(pages.get(pageIndex)).getBytes(PdfLayoutMetrics.PDF_CHARSET);
            writeStreamObject(document, offsets, contentObjectNumber, contentBytes);
        }

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

        return document.toByteArray();
    }

    private String buildContentStream(PageContent page) {
        StringBuilder builder = new StringBuilder();

        if (page.coverPage()) {
            appendText(builder, 120, PdfLayoutMetrics.COVER_TITLE_Y, 24, page.coverTitle());
            if (page.coverSubtitle() != null && !page.coverSubtitle().isBlank()) {
                appendText(builder, 120, PdfLayoutMetrics.COVER_SUBTITLE_Y, 16, page.coverSubtitle());
            }
        } else {
            PdfLayoutSettings settings = page.layoutSettings();
            if (settings.headerText() != null && !settings.headerText().isBlank()) {
                appendText(builder, 50, 812, 10, settings.headerText());
            }

            appendBodyElements(builder, page.bodyElements(), PdfLayoutMetrics.calculateBodyStartY(settings.headerText()));

            if (settings.footerText() != null && !settings.footerText().isBlank()) {
                appendText(builder, 50, PdfLayoutMetrics.FOOTER_Y, 10, settings.footerText());
            }

            if (settings.pageNumbersEnabled()) {
                appendText(builder, 500, PdfLayoutMetrics.PAGE_NUMBER_Y, 10, "Page " + page.logicalPageNumber());
            }
        }

        return builder.toString();
    }

    private void appendBodyElements(StringBuilder builder, List<PdfElement> elements, int startY) {
        int currentY = startY;
        for (PdfElement element : elements) {
            if (element.answerBox()) {
                appendAnswerBox(builder, PdfLayoutMetrics.BODY_X, currentY - element.height(),
                        PdfLayoutMetrics.ANSWER_BOX_WIDTH, element.height(), element.boxLines());
            } else {
                appendText(builder, PdfLayoutMetrics.BODY_X, currentY, 12, element.text());
            }
            currentY -= element.height();
        }
    }

    private void appendText(StringBuilder builder, int x, int y, int fontSize, String text) {
        builder.append("BT\n");
        builder.append("1 0 0 1 ").append(x).append(" ").append(y).append(" Tm\n");
        builder.append("/F1 ").append(fontSize).append(" Tf\n");
        builder.append("(").append(textFormatter.escapePdfText(text)).append(") Tj\n");
        builder.append("ET\n");
    }

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
            appendText(builder, textX, textY, 11, boxLine);
            textY -= PdfLayoutMetrics.LINE_HEIGHT;
        }
    }

    private void writeObject(ByteArrayOutputStream document, List<Integer> offsets, int objectNumber, String objectBody) throws IOException {
        offsets.add(document.size());
        write(document, objectNumber + " 0 obj\n");
        write(document, objectBody);
        write(document, "\nendobj\n");
    }

    private void writeStreamObject(ByteArrayOutputStream document, List<Integer> offsets, int objectNumber, byte[] streamBytes) throws IOException {
        offsets.add(document.size());
        write(document, objectNumber + " 0 obj\n");
        write(document, "<< /Length " + streamBytes.length + " >>\n");
        write(document, "stream\n");
        document.write(streamBytes);
        write(document, "\nendstream\n");
        write(document, "endobj\n");
    }

    private void write(ByteArrayOutputStream document, String content) throws IOException {
        document.write(content.getBytes(PdfLayoutMetrics.PDF_CHARSET));
    }
}

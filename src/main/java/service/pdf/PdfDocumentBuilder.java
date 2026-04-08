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
        int regularFontObjectNumber = 3;
        int boldFontObjectNumber = 4;
        int nextObjectNumber = 5;

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

        writeObject(document, offsets, regularFontObjectNumber,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        writeObject(document, offsets, boldFontObjectNumber,
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>");

        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            int pageObjectNumber = nextObjectNumber + (pageIndex * 2);
            int contentObjectNumber = pageObjectNumber + 1;

            String pageObject = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                    + "/Resources << /Font << /F1 " + regularFontObjectNumber + " 0 R /F2 " + boldFontObjectNumber + " 0 R >> >> "
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
            appendText(builder, 120, PdfLayoutMetrics.COVER_TITLE_Y, 28, true, page.coverTitle());
            if (page.coverSubtitle() != null && !page.coverSubtitle().isBlank()) {
                appendText(builder, 120, PdfLayoutMetrics.COVER_SUBTITLE_Y, 16, false, page.coverSubtitle());
            }
        } else if (page.tableOfContentsPage()) {
            appendTableOfContents(builder, page);
        } else {
            PdfLayoutSettings settings = page.layoutSettings();
            if (settings.headerText() != null && !settings.headerText().isBlank()) {
                appendText(builder, 50, 812, 10, false, settings.headerText());
            }

            appendBodyElements(builder, page.bodyElements(), PdfLayoutMetrics.calculateBodyStartY(settings.headerText()));

            if (settings.footerText() != null && !settings.footerText().isBlank()) {
                appendText(builder, 50, PdfLayoutMetrics.FOOTER_Y, 10, false, settings.footerText());
            }

            if (settings.pageNumbersEnabled()) {
                appendText(builder, 500, PdfLayoutMetrics.PAGE_NUMBER_Y, 10, false, "Page " + page.logicalPageNumber());
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
                appendText(builder, PdfLayoutMetrics.BODY_X, currentY, fontSizeFor(element), boldFor(element), element.text());
            }
            currentY -= element.height();
        }
    }

    private int fontSizeFor(PdfElement element) {
        return switch (element.type()) {
            case CHAPTER_HEADING -> 20;
            case TASK_HEADING -> 13;
            case ANSWER_LABEL -> 12;
            default -> 12;
        };
    }

    private boolean boldFor(PdfElement element) {
        return switch (element.type()) {
            case CHAPTER_HEADING, TASK_HEADING, ANSWER_LABEL -> true;
            default -> false;
        };
    }

    private void appendTableOfContents(StringBuilder builder, PageContent page) {
        appendPageFrame(builder, page.layoutSettings(), page.logicalPageNumber());
        appendText(builder, PdfLayoutMetrics.BODY_X, PdfLayoutMetrics.TOC_TITLE_Y, 20, true, "Inhaltsverzeichnis");

        int topY = PdfLayoutMetrics.TOC_TABLE_TOP_Y;
        appendTableRow(builder, topY, true, "Seite", "Chapter", "M\u00f6gliche Punkte", "Erreichte Punkte");
        int currentY = topY - PdfLayoutMetrics.TOC_ROW_HEIGHT;
        for (var entry : page.tocEntries()) {
            appendTableRow(builder, currentY, false,
                    Integer.toString(entry.page()),
                    entry.chapter(),
                    Integer.toString(entry.possiblePoints()),
                    "");
            currentY -= PdfLayoutMetrics.TOC_ROW_HEIGHT;
        }
    }

    private void appendPageFrame(StringBuilder builder, PdfLayoutSettings settings, int logicalPageNumber) {
        if (settings.headerText() != null && !settings.headerText().isBlank()) {
            appendText(builder, 50, 812, 10, false, settings.headerText());
        }
        if (settings.footerText() != null && !settings.footerText().isBlank()) {
            appendText(builder, 50, PdfLayoutMetrics.FOOTER_Y, 10, false, settings.footerText());
        }
        if (settings.pageNumbersEnabled()) {
            appendText(builder, 500, PdfLayoutMetrics.PAGE_NUMBER_Y, 10, false, "Page " + logicalPageNumber);
        }
    }

    private void appendTableRow(
            StringBuilder builder,
            int y,
            boolean header,
            String page,
            String chapter,
            String possiblePoints,
            String achievedPoints
    ) {
        appendRectangle(builder, PdfLayoutMetrics.BODY_X, y - 16, 495, PdfLayoutMetrics.TOC_ROW_HEIGHT);
        appendVerticalLine(builder, 100, y - 16, PdfLayoutMetrics.TOC_ROW_HEIGHT);
        appendVerticalLine(builder, 350, y - 16, PdfLayoutMetrics.TOC_ROW_HEIGHT);
        appendVerticalLine(builder, 445, y - 16, PdfLayoutMetrics.TOC_ROW_HEIGHT);
        int textY = y - 7;
        appendCenteredText(builder, 50, 50, textY, 10, header, page);
        appendCenteredText(builder, 100, 250, textY, 10, header, truncate(chapter, 42));
        appendCenteredText(builder, 350, 95, textY, 10, header, possiblePoints);
        appendCenteredText(builder, 445, 100, textY, 10, header, achievedPoints);
    }

    private String truncate(String value, int maxLength) {
        String safeValue = value == null ? "" : value;
        return safeValue.length() <= maxLength ? safeValue : safeValue.substring(0, maxLength - 3) + "...";
    }

    private void appendRectangle(StringBuilder builder, int x, int y, int width, int height) {
        builder.append("q\n");
        builder.append("0.65 0.65 0.65 RG\n");
        builder.append("0.6 w\n");
        builder.append(x).append(" ").append(y).append(" ").append(width).append(" ").append(height).append(" re\n");
        builder.append("S\n");
        builder.append("Q\n");
    }

    private void appendVerticalLine(StringBuilder builder, int x, int y, int height) {
        builder.append("q\n");
        builder.append("0.65 0.65 0.65 RG\n");
        builder.append("0.6 w\n");
        builder.append(x).append(" ").append(y).append(" m\n");
        builder.append(x).append(" ").append(y + height).append(" l\n");
        builder.append("S\n");
        builder.append("Q\n");
    }

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

    private void appendText(StringBuilder builder, int x, int y, int fontSize, boolean bold, String text) {
        builder.append("BT\n");
        builder.append("1 0 0 1 ").append(x).append(" ").append(y).append(" Tm\n");
        builder.append(bold ? "/F2 " : "/F1 ").append(fontSize).append(" Tf\n");
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
            appendText(builder, textX, textY, 11, false, boxLine);
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

package service.pdf;

import lombok.Getter;
import models.Chapter;
import models.Subtask;
import models.Variant;
import service.exam.dto.GeneratedChapter;
import service.exam.dto.GeneratedExam;
import service.exam.dto.GeneratedSubtask;
import service.exam.dto.PdfLayoutSettings;
import service.pdf.dto.PageContent;
import service.pdf.dto.PdfElement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class PdfExamWriter {

    private static final Charset PDF_CHARSET = Charset.forName("windows-1252");
    private static final CharsetEncoder PDF_ENCODER = PDF_CHARSET.newEncoder();
    private static final int MAX_CHARS_PER_LINE = 90;
    private static final int DEFAULT_START_Y = 790;
    private static final int HEADER_START_Y = 760;
    private static final int FOOTER_Y = 45;
    private static final int PAGE_NUMBER_Y = 28;
    private static final int COVER_TITLE_Y = 470;
    private static final int COVER_SUBTITLE_Y = 430;
    @Getter
    private static final int LINE_HEIGHT = 14;
    private static final int BODY_X = 50;
    private static final int ANSWER_BOX_WIDTH = 495;
    @Getter
    private static final int ANSWER_BOX_MIN_HEIGHT = 86;
    @Getter
    private static final int ANSWER_BOX_PADDING = 8;
    private static final int ANSWER_BOX_TEXT_MAX_CHARS = 78;

    public void write(Path outputPath, GeneratedExam exam, PdfLayoutSettings layoutSettings) throws IOException {
        write(outputPath, exam, layoutSettings, false);
    }

    public void write(
            Path outputPath,
            GeneratedExam exam,
            PdfLayoutSettings layoutSettings,
            boolean includeSolutions
    ) throws IOException {
        PdfLayoutSettings sanitizedSettings = (layoutSettings == null
                ? PdfLayoutSettings.defaults(exam.title())
                : layoutSettings).sanitize(exam.title());
        List<PdfElement> elements = buildElements(exam, includeSolutions);
        List<PageContent> pages = paginate(elements, sanitizedSettings);
        byte[] pdfBytes = buildPdfDocument(pages);

        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        Files.write(outputPath, pdfBytes);
    }

    private List<PdfElement> buildElements(GeneratedExam exam, boolean includeSolutions) {
        List<PdfElement> elements = new ArrayList<>();
        elements.add(PdfElement.text(exam.title()));
        elements.add(PdfElement.text("Total points: " + exam.totalPoints()));
        elements.add(PdfElement.text(""));

        int questionNumber = 1;
        for (GeneratedChapter generatedChapter : exam.chapters()) {
            Chapter chapter = generatedChapter.chapter();
            elements.add(PdfElement.text("Chapter: " + safeLabel(chapter.getTitle(), "Chapter " + chapter.getId())));

            for (GeneratedSubtask generatedSubtask : generatedChapter.subtasks()) {
                Subtask subtask = generatedSubtask.subtask();
                Variant variant = generatedSubtask.variant();

                elements.add(PdfElement.text(""));
                elements.add(PdfElement.text(questionNumber + ". " + safeLabel(subtask.getTitle(), "Task " + subtask.getId())
                        + " (" + subtask.getPoints() + " pts)"));
                wrap("Question: " + safeLabel(variant.getQuestion(), "No question text available."))
                        .forEach(line -> elements.add(PdfElement.text(line)));
                appendAnswerPlaceholder(elements, variant, includeSolutions);
                questionNumber++;
            }

            elements.add(PdfElement.text(""));
        }

        return elements;
    }

    private void appendAnswerPlaceholder(List<PdfElement> elements, Variant variant, boolean includeSolutions) {
        elements.add(PdfElement.text(""));
        elements.add(PdfElement.text("Answer:"));
        if (includeSolutions) {
            String solution = variant == null ? "" : variant.getSolution();
            if (solution != null && !solution.isBlank()) {
                elements.add(PdfElement.answerBox(wrap(solution.trim(), ANSWER_BOX_TEXT_MAX_CHARS)));
                return;
            }
        }

        elements.add(PdfElement.answerBox(List.of()));
    }

    private List<PageContent> paginate(List<PdfElement> elements, PdfLayoutSettings settings) {
        List<PageContent> pages = new ArrayList<>();
        if (settings.coverPageEnabled()) {
            pages.add(PageContent.cover(settings.coverTitle(), settings.coverSubtitle()));
        }

        int startY = calculateBodyStartY(settings);
        int bottomY = calculateBodyBottomY(settings);
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

    private byte[] buildPdfDocument(List<PageContent> pages) throws IOException {
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

            byte[] contentBytes = buildContentStream(pages.get(pageIndex)).getBytes(PDF_CHARSET);
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
            appendText(builder, 120, COVER_TITLE_Y, 24, page.coverTitle());
            if (page.coverSubtitle() != null && !page.coverSubtitle().isBlank()) {
                appendText(builder, 120, COVER_SUBTITLE_Y, 16, page.coverSubtitle());
            }
        } else {
            PdfLayoutSettings settings = page.layoutSettings();
            if (settings.headerText() != null && !settings.headerText().isBlank()) {
                appendText(builder, 50, 812, 10, settings.headerText());
            }

            appendBodyElements(builder, page.bodyElements(), calculateBodyStartY(settings));

            if (settings.footerText() != null && !settings.footerText().isBlank()) {
                appendText(builder, 50, FOOTER_Y, 10, settings.footerText());
            }

            if (settings.pageNumbersEnabled()) {
                appendText(builder, 500, PAGE_NUMBER_Y, 10, "Page " + page.logicalPageNumber());
            }
        }

        return builder.toString();
    }

    private void appendBodyElements(StringBuilder builder, List<PdfElement> elements, int startY) {
        int currentY = startY;
        for (PdfElement element : elements) {
            if (element.answerBox()) {
                appendAnswerBox(builder, BODY_X, currentY - element.height(), ANSWER_BOX_WIDTH, element.height(), element.boxLines());
            } else {
                appendText(builder, BODY_X, currentY, 12, element.text());
            }
            currentY -= element.height();
        }
    }

    private void appendText(StringBuilder builder, int x, int y, int fontSize, String text) {
        builder.append("BT\n");
        builder.append("1 0 0 1 ").append(x).append(" ").append(y).append(" Tm\n");
        builder.append("/F1 ").append(fontSize).append(" Tf\n");
        builder.append("(").append(escapePdfText(text)).append(") Tj\n");
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

        int textY = y + height - ANSWER_BOX_PADDING - LINE_HEIGHT;
        int textX = x + ANSWER_BOX_PADDING;
        for (String boxLine : boxLines) {
            if (textY <= y + ANSWER_BOX_PADDING) {
                break;
            }
            appendText(builder, textX, textY, 11, boxLine);
            textY -= LINE_HEIGHT;
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
        document.write(content.getBytes(PDF_CHARSET));
    }

    private List<String> wrap(String text) {
        return wrap(text, MAX_CHARS_PER_LINE);
    }

    private List<String> wrap(String text, int maxCharsPerLine) {
        List<String> lines = new ArrayList<>();
        String remaining = sanitize(text);

        while (remaining.length() > maxCharsPerLine) {
            int breakPosition = remaining.lastIndexOf(' ', maxCharsPerLine);
            if (breakPosition <= 0) {
                breakPosition = maxCharsPerLine;
            }
            lines.add(remaining.substring(0, breakPosition).trim());
            remaining = remaining.substring(breakPosition).trim();
        }

        if (!remaining.isBlank()) {
            lines.add(remaining);
        }

        if (lines.isEmpty()) {
            lines.add("");
        }
        return lines;
    }

    private String escapePdfText(String text) {
        return sanitize(text)
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private String sanitize(String text) {
        String value = text == null ? "" : text.replace('\r', ' ').replace('\n', ' ');
        StringBuilder sanitized = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            sanitized.append(PDF_ENCODER.canEncode(current) ? current : '?');
        }
        return sanitized.toString();
    }

    private String safeLabel(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private int calculateBodyStartY(PdfLayoutSettings settings) {
        return settings.headerText() == null || settings.headerText().isBlank() ? DEFAULT_START_Y : HEADER_START_Y;
    }

    private int calculateBodyBottomY(PdfLayoutSettings settings) {
        if ((settings.footerText() != null && !settings.footerText().isBlank()) || settings.pageNumbersEnabled()) {
            return 70;
        }
        return 45;
    }




}


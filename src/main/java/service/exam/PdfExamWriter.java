package service.exam;

import models.Chapter;
import models.Subtask;
import models.Variant;
import service.exam.dto.GeneratedChapter;
import service.exam.dto.GeneratedExam;
import service.exam.dto.GeneratedSubtask;
import service.exam.dto.PdfLayoutSettings;

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
    private static final int LINE_HEIGHT = 14;

    public void write(Path outputPath, GeneratedExam exam, PdfLayoutSettings layoutSettings) throws IOException {
        PdfLayoutSettings sanitizedSettings = (layoutSettings == null
                ? PdfLayoutSettings.defaults(exam.title())
                : layoutSettings).sanitize(exam.title());
        List<String> lines = buildLines(exam);
        List<PageContent> pages = paginate(lines, sanitizedSettings);
        byte[] pdfBytes = buildPdfDocument(pages);

        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        Files.write(outputPath, pdfBytes);
    }

    private List<String> buildLines(GeneratedExam exam) {
        List<String> lines = new ArrayList<>();
        lines.add(exam.title());
        lines.add("Total points: " + exam.totalPoints());
        lines.add("");

        int questionNumber = 1;
        for (GeneratedChapter generatedChapter : exam.chapters()) {
            Chapter chapter = generatedChapter.chapter();
            lines.add("Chapter: " + safeLabel(chapter.getTitle(), "Chapter " + chapter.getId()));

            for (GeneratedSubtask generatedSubtask : generatedChapter.subtasks()) {
                Subtask subtask = generatedSubtask.subtask();
                Variant variant = generatedSubtask.variant();

                lines.add("");
                lines.add(questionNumber + ". " + safeLabel(subtask.getTitle(), "Task " + subtask.getId())
                        + " (" + subtask.getPoints() + " pts)");
                lines.addAll(wrap("Question: " + safeLabel(variant.getQuestion(), "No question text available.")));
                questionNumber++;
            }

            lines.add("");
        }

        return lines;
    }

    private List<PageContent> paginate(List<String> lines, PdfLayoutSettings settings) {
        List<PageContent> pages = new ArrayList<>();
        if (settings.coverPageEnabled()) {
            pages.add(PageContent.cover(settings.coverTitle(), settings.coverSubtitle()));
        }

        int startY = calculateBodyStartY(settings);
        int bottomY = calculateBodyBottomY(settings);
        int maxLinesPerPage = Math.max(1, ((startY - bottomY) / LINE_HEIGHT) + 1);

        List<String> currentPage = new ArrayList<>();
        int logicalPageNumber = 1;
        for (String line : lines) {
            if (currentPage.size() >= maxLinesPerPage) {
                pages.add(PageContent.body(new ArrayList<>(currentPage), settings, logicalPageNumber));
                logicalPageNumber++;
                currentPage = new ArrayList<>();
            }
            currentPage.add(line);
        }

        if (currentPage.isEmpty()) {
            currentPage.add(" ");
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
        builder.append("BT\n");
        builder.append("/F1 12 Tf\n");
        builder.append("14 TL\n");

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

            appendBodyLines(builder, page.bodyLines(), calculateBodyStartY(settings));

            if (settings.footerText() != null && !settings.footerText().isBlank()) {
                appendText(builder, 50, FOOTER_Y, 10, settings.footerText());
            }

            if (settings.pageNumbersEnabled()) {
                appendText(builder, 500, PAGE_NUMBER_Y, 10, "Page " + page.logicalPageNumber());
            }
        }

        builder.append("ET\n");
        return builder.toString();
    }

    private void appendBodyLines(StringBuilder builder, List<String> lines, int startY) {
        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            appendText(builder, 50, startY - (lineIndex * LINE_HEIGHT), 12, lines.get(lineIndex));
        }
    }

    private void appendText(StringBuilder builder, int x, int y, int fontSize, String text) {
        builder.append("1 0 0 1 ").append(x).append(" ").append(y).append(" Tm\n");
        builder.append("/F1 ").append(fontSize).append(" Tf\n");
        builder.append("(").append(escapePdfText(text)).append(") Tj\n");
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
        List<String> lines = new ArrayList<>();
        String remaining = sanitize(text);

        while (remaining.length() > MAX_CHARS_PER_LINE) {
            int breakPosition = remaining.lastIndexOf(' ', MAX_CHARS_PER_LINE);
            if (breakPosition <= 0) {
                breakPosition = MAX_CHARS_PER_LINE;
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

    private record PageContent(
            boolean coverPage,
            String coverTitle,
            String coverSubtitle,
            List<String> bodyLines,
            PdfLayoutSettings layoutSettings,
            int logicalPageNumber
    ) {
        private static PageContent cover(String coverTitle, String coverSubtitle) {
            return new PageContent(true, coverTitle, coverSubtitle, List.of(), null, 0);
        }

        private static PageContent body(List<String> bodyLines, PdfLayoutSettings layoutSettings, int logicalPageNumber) {
            return new PageContent(false, "", "", bodyLines, layoutSettings, logicalPageNumber);
        }
    }
}


package service.pdf;

import service.exam.dto.GeneratedExam;
import service.exam.dto.GeneratedChapter;
import service.exam.dto.PdfLayoutSettings;
import service.pdf.dto.PageContent;
import service.pdf.dto.PdfElement;
import service.pdf.dto.PdfElementType;
import service.pdf.dto.TocEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class PdfExamWriter {

    private final PdfTextFormatter textFormatter = new PdfTextFormatter();
    private final PdfExamElementBuilder elementBuilder = new PdfExamElementBuilder(textFormatter);
    private final PdfPaginator paginator = new PdfPaginator();
    private final PdfDocumentBuilder documentBuilder = new PdfDocumentBuilder(textFormatter);

    public void write(Path outputPath, GeneratedExam exam, PdfLayoutSettings layoutSettings) throws IOException {
        write(outputPath, exam, layoutSettings, false);
    }

    public void write(
            Path outputPath,
            GeneratedExam exam,
            PdfLayoutSettings layoutSettings,
            boolean includeSolutions
    ) throws IOException {
        PdfLayoutSettings sanitizedSettings = sanitizeSettings(exam, layoutSettings);
        List<PdfElement> elements = elementBuilder.buildElements(exam, includeSolutions);
        List<PageContent> pages = buildPagesWithTableOfContents(exam, elements, sanitizedSettings);
        byte[] pdfBytes = documentBuilder.buildPdfDocument(pages);

        writeBytes(outputPath, pdfBytes);
    }

    public List<Path> writeExamFiles(
            Path examOutputPath,
            GeneratedExam exam,
            PdfLayoutSettings layoutSettings,
            boolean includeSolutionFile
    ) throws IOException {
        write(examOutputPath, exam, layoutSettings, false);
        if (!includeSolutionFile) {
            return List.of(examOutputPath);
        }

        Path solutionOutputPath = buildSolutionOutputPath(examOutputPath);
        write(solutionOutputPath, exam, layoutSettings, true);
        return List.of(examOutputPath, solutionOutputPath);
    }

    private PdfLayoutSettings sanitizeSettings(GeneratedExam exam, PdfLayoutSettings layoutSettings) {
        return (layoutSettings == null
                ? PdfLayoutSettings.defaults(exam.title())
                : layoutSettings).sanitize(exam.title());
    }

    private List<PageContent> buildPagesWithTableOfContents(
            GeneratedExam exam,
            List<PdfElement> elements,
            PdfLayoutSettings settings
    ) {
        List<PageContent> pages = new ArrayList<>();
        if (settings.coverPageEnabled()) {
            pages.add(PageContent.cover(settings.coverTitle(), settings.coverSubtitle()));
        }

        List<PageContent> bodyPages = paginator.paginateBody(elements, settings, 2);
        pages.add(PageContent.tableOfContents(buildTocEntries(exam, bodyPages), settings, 1));
        pages.addAll(bodyPages);
        return pages;
    }

    private List<TocEntry> buildTocEntries(GeneratedExam exam, List<PageContent> bodyPages) {
        List<TocEntry> entries = new ArrayList<>();
        List<Integer> chapterPages = findChapterPages(bodyPages);
        for (int chapterIndex = 0; chapterIndex < exam.chapters().size(); chapterIndex++) {
            String chapterTitle = textFormatter.safeLabel(
                    exam.chapters().get(chapterIndex).chapter().getTitle(),
                    "Chapter " + exam.chapters().get(chapterIndex).chapter().getId()
            );
            entries.add(new TocEntry(
                    chapterIndex < chapterPages.size() ? chapterPages.get(chapterIndex) : 0,
                    chapterTitle,
                    calculateChapterPoints(exam.chapters().get(chapterIndex))
            ));
        }
        return entries;
    }

    private List<Integer> findChapterPages(List<PageContent> bodyPages) {
        List<Integer> chapterPages = new ArrayList<>();
        for (PageContent page : bodyPages) {
            for (PdfElement element : page.bodyElements()) {
                if (element.type() == PdfElementType.CHAPTER_HEADING) {
                    chapterPages.add(page.logicalPageNumber());
                }
            }
        }
        return chapterPages;
    }

    private int calculateChapterPoints(GeneratedChapter chapter) {
        return chapter.subtasks().stream()
                .mapToInt(generatedSubtask -> generatedSubtask.subtask().getPoints())
                .sum();
    }

    private void writeBytes(Path outputPath, byte[] pdfBytes) throws IOException {
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        Files.write(outputPath, pdfBytes);
    }

    private Path buildSolutionOutputPath(Path examOutputPath) {
        Path fileNamePath = examOutputPath.getFileName();
        String fileName = fileNamePath == null ? "exam.pdf" : fileNamePath.toString();
        String solutionFileName;
        if (fileName.toLowerCase().endsWith(".pdf")) {
            solutionFileName = fileName.substring(0, fileName.length() - 4) + "_solutions.pdf";
        } else {
            solutionFileName = fileName + "_solutions.pdf";
        }

        Path parent = examOutputPath.getParent();
        return parent == null ? Path.of(solutionFileName) : parent.resolve(solutionFileName);
    }
}

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


/**
 * Coordinates PDF generation, pagination, table-of-contents creation, and file writing.
 */
public class PdfExamWriter {

    private final PdfTextFormatter textFormatter = new PdfTextFormatter();
    private final PdfExamElementBuilder elementBuilder = new PdfExamElementBuilder(textFormatter);
    private final PdfPaginator paginator = new PdfPaginator();
    private final PdfDocumentBuilder documentBuilder = new PdfDocumentBuilder(textFormatter);

    /**
     * Writes the exam PDF without embedded solutions.
     *
     * @param outputPath target PDF path
     * @param exam generated exam content
     * @param layoutSettings user-selected layout settings
     * @throws IOException if writing the PDF fails
     */
    public void write(Path outputPath, GeneratedExam exam, PdfLayoutSettings layoutSettings) throws IOException {
        write(outputPath, exam, layoutSettings, false);
    }

    /**
     * Writes one PDF file for the given exam.
     *
     * @param outputPath target PDF path
     * @param exam generated exam content
     * @param layoutSettings user-selected layout settings
     * @param includeSolutions whether solutions should be embedded in the PDF
     * @throws IOException if writing the PDF fails
     */
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

    /**
     * Writes the exam PDF and, optionally, a separate solution PDF.
     *
     * @param examOutputPath target path for the exam PDF
     * @param exam generated exam content
     * @param layoutSettings user-selected layout settings
     * @param includeSolutionFile whether to create a separate solution PDF
     * @return paths of all generated files
     * @throws IOException if writing either PDF fails
     */
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

    /**
     * Applies default layout settings and trims user-entered layout text.
     *
     * @param exam generated exam content
     * @param layoutSettings user-selected layout settings
     * @return sanitized settings
     */
    private PdfLayoutSettings sanitizeSettings(GeneratedExam exam, PdfLayoutSettings layoutSettings) {
        return (layoutSettings == null
                ? PdfLayoutSettings.defaults(exam.title())
                : layoutSettings).sanitize(exam.title());
    }

    /**
     * Builds cover, table-of-contents, and body pages in their final order.
     *
     * @param exam generated exam content
     * @param elements body elements to paginate
     * @param settings sanitized layout settings
     * @return final page list
     */
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

    /**
     * Builds table-of-contents entries from chapter headings found in paginated body pages.
     *
     * @param exam generated exam content
     * @param bodyPages paginated body pages
     * @return table-of-contents entries
     */
    private List<TocEntry> buildTocEntries(GeneratedExam exam, List<PageContent> bodyPages) {
        List<TocEntry> entries = new ArrayList<>();
        List<Integer> chapterPages = findChapterPages(bodyPages);
        for (int chapterIndex = 0; chapterIndex < exam.chapters().size(); chapterIndex++) {
            String chapterTitle = (chapterIndex + 1) + ". " + textFormatter.safeLabel(
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

    /**
     * Finds the logical page number for each chapter heading.
     *
     * @param bodyPages paginated body pages
     * @return logical page numbers containing chapter headings
     */
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

    /**
     * Calculates the sum of points in one generated chapter.
     *
     * @param chapter generated chapter content
     * @return total chapter points
     */
    private int calculateChapterPoints(GeneratedChapter chapter) {
        return chapter.subtasks().stream()
                .mapToInt(generatedSubtask -> generatedSubtask.subtask().getPoints())
                .sum();
    }

    /**
     * Writes PDF bytes to disk and creates the parent directory when needed.
     *
     * @param outputPath target path
     * @param pdfBytes PDF bytes to write
     * @throws IOException if directory creation or file writing fails
     */
    private void writeBytes(Path outputPath, byte[] pdfBytes) throws IOException {
        if (outputPath.getParent() != null) {
            Files.createDirectories(outputPath.getParent());
        }
        Files.write(outputPath, pdfBytes);
    }

    /**
     * Builds the sibling output path for a solution PDF.
     *
     * @param examOutputPath target path of the exam PDF
     * @return solution PDF path
     */
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

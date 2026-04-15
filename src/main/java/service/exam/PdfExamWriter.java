package service.exam;

import service.exam.dto.GeneratedExam;
import service.exam.dto.PdfLayoutSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Compatibility wrapper for older compiled UI classes that still reference the former package.
 */
public class PdfExamWriter {

    private final service.pdf.PdfExamWriter delegate = new service.pdf.PdfExamWriter();

    /**
     * Writes one PDF through the current PDF writer implementation.
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
        delegate.write(outputPath, exam, layoutSettings, includeSolutions);
    }

    /**
     * Writes the exam PDF and, optionally, a separate solution PDF through the current writer.
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
        return delegate.writeExamFiles(examOutputPath, exam, layoutSettings, includeSolutionFile);
    }
}

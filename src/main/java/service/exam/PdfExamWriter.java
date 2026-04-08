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

    public void write(
            Path outputPath,
            GeneratedExam exam,
            PdfLayoutSettings layoutSettings,
            boolean includeSolutions
    ) throws IOException {
        delegate.write(outputPath, exam, layoutSettings, includeSolutions);
    }

    public List<Path> writeExamFiles(
            Path examOutputPath,
            GeneratedExam exam,
            PdfLayoutSettings layoutSettings,
            boolean includeSolutionFile
    ) throws IOException {
        return delegate.writeExamFiles(examOutputPath, exam, layoutSettings, includeSolutionFile);
    }
}

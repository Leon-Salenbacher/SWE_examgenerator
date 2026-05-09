package service.pdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import service.exam.dto.PdfLayoutSettings;
import support.ExamTestData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPdfExamWriter {

    @TempDir
    Path tempDir;

    @Test
    public void test_writeExamFiles_goodcase01_writeExamPdfOnly() throws Exception {
        Path examPath = tempDir.resolve("exam.pdf");

        List<Path> writtenPaths = new PdfExamWriter().writeExamFiles(
                examPath,
                ExamTestData.generatedExam(),
                PdfLayoutSettings.defaults("Exam"),
                false
        );

        assertEquals(List.of(examPath), writtenPaths);
        assertTrue(Files.exists(examPath));
        assertPdfHeader(examPath);
    }

    @Test
    public void test_writeExamFiles_goodcase02_writeSeparateSolutionPdf() throws Exception {
        Path examPath = tempDir.resolve("nested").resolve("exam.pdf");
        Path solutionPath = tempDir.resolve("nested").resolve("exam_solutions.pdf");

        List<Path> writtenPaths = new PdfExamWriter().writeExamFiles(
                examPath,
                ExamTestData.generatedExam(),
                PdfLayoutSettings.defaults("Exam"),
                true
        );

        assertEquals(List.of(examPath, solutionPath), writtenPaths);
        assertTrue(Files.exists(examPath));
        assertTrue(Files.exists(solutionPath));
        assertPdfHeader(examPath);
        assertPdfHeader(solutionPath);
    }

    private void assertPdfHeader(Path path) throws Exception {
        byte[] bytes = Files.readAllBytes(path);
        assertEquals('%', bytes[0]);
        assertEquals('P', bytes[1]);
        assertEquals('D', bytes[2]);
        assertEquals('F', bytes[3]);
        assertEquals('-', bytes[4]);
        assertEquals('1', bytes[5]);
        assertEquals('.', bytes[6]);
        assertEquals('4', bytes[7]);
    }
}

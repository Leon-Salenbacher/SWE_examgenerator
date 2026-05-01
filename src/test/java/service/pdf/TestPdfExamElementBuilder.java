package service.pdf;

import models.Chapter;
import models.Subtask;
import models.Variant;
import org.junit.jupiter.api.Test;
import service.exam.dto.GeneratedChapter;
import service.exam.dto.GeneratedExam;
import service.exam.dto.GeneratedSubtask;
import service.exam.dto.PdfLayoutSettings;
import service.pdf.dto.PdfElement;
import service.pdf.metrics.PdfLayoutMetrics;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPdfExamElementBuilder {

    @Test
    public void test_buildElements_goodcase01_showChapterPointTotalInHeading() {
        PdfExamElementBuilder builder = new PdfExamElementBuilder(new PdfTextFormatter());
        Chapter chapter = chapter(1, "Requirements");

        List<PdfElement> elements = builder.buildElements(new GeneratedExam(
                "Exam",
                9.5,
                List.of(new GeneratedChapter(chapter, List.of(
                        generatedSubtask(1, "Task 1", 5.5),
                        generatedSubtask(2, "Task 2", 4)
                )))
        ), false);

        assertEquals("1. Requirements (9.5 pts)", elements.get(0).text());
    }

    @Test
    public void test_buildElements_goodcase02_useLayoutAnswerBoxScale() {
        PdfExamElementBuilder builder = new PdfExamElementBuilder(new PdfTextFormatter());
        Chapter chapter = chapter(1, "Requirements");

        List<PdfElement> elements = builder.buildElements(new GeneratedExam(
                "Exam",
                10,
                List.of(new GeneratedChapter(chapter, List.of(generatedSubtask(1, "Task 1", 10))))
        ), false, new PdfLayoutSettings(false, "Exam", "", "", "", true, 24));

        PdfElement answerBox = elements.stream()
                .filter(PdfElement::answerBox)
                .findFirst()
                .orElseThrow();

        assertEquals(Math.max(PdfLayoutMetrics.ANSWER_BOX_MIN_HEIGHT, 240), answerBox.height());
    }

    private Chapter chapter(int id, String title) {
        Chapter chapter = new Chapter();
        chapter.setId(id);
        chapter.setTitle(title);
        return chapter;
    }

    private GeneratedSubtask generatedSubtask(int id, String title, double points) {
        Subtask subtask = new Subtask();
        subtask.setId(id);
        subtask.setTitle(title);
        subtask.setPoints(points);

        Variant variant = new Variant();
        variant.setQuestion("Question " + id);

        return new GeneratedSubtask(subtask, variant);
    }
}

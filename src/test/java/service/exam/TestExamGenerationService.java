package service.exam;

import exceptions.ExamGenerationException;
import models.Chapter;
import models.ExamType;
import models.Subtask;
import models.SubtaskDifficulty;
import models.Variant;
import org.junit.jupiter.api.Test;
import service.exam.dto.GenerateExamValues;
import service.exam.dto.GeneratedExam;
import support.ExamTestData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestExamGenerationService {

    @Test
    public void test_generateExam_goodcase01_useOnlyExamSubtasksForRealExam() {
        ExamGenerationService service = new ExamGenerationService();
        Chapter chapter = chapterWithMixedExamTypes();

        GeneratedExam generatedExam = service.generateExam(new GenerateExamValues(
                "Klausur",
                12,
                List.of(chapter),
                ExamType.EXAM
        ));

        assertEquals(
                List.of("Exam easy", "Exam medium", "Exam hard"),
                generatedSubtaskTitles(generatedExam)
        );
    }

    @Test
    public void test_generateExam_goodcase02_useOnlyPracticeSubtasksForPracticeExam() {
        ExamGenerationService service = new ExamGenerationService();
        Chapter chapter = chapterWithMixedExamTypes();

        GeneratedExam generatedExam = service.generateExam(new GenerateExamValues(
                "Probeklausur",
                12,
                List.of(chapter),
                ExamType.PRACTICE
        ));

        assertEquals(
                List.of("Practice easy", "Practice medium", "Practice hard"),
                generatedSubtaskTitles(generatedExam)
        );
    }

    @Test
    public void test_calculateAvailableTotalPoints_goodcase01_filterPointOptionsByExamType() {
        ExamGenerationService service = new ExamGenerationService();
        Chapter chapter = new Chapter();
        chapter.setId(1);
        chapter.setTitle("Chapter");
        chapter.setChildElements(List.of(
                subtask(1, "Exam easy", 4, SubtaskDifficulty.EASY, ExamType.EXAM),
                subtask(2, "Exam medium", 4, SubtaskDifficulty.MEDIUM, ExamType.EXAM),
                subtask(3, "Exam hard", 4, SubtaskDifficulty.HARD, ExamType.EXAM),
                subtask(4, "Practice easy", 6, SubtaskDifficulty.EASY, ExamType.PRACTICE),
                subtask(5, "Practice medium", 6, SubtaskDifficulty.MEDIUM, ExamType.PRACTICE),
                subtask(6, "Practice hard", 6, SubtaskDifficulty.HARD, ExamType.PRACTICE)
        ));

        assertEquals(List.of(4.0, 8.0, 12.0), service.calculateAvailableTotalPoints(List.of(chapter), ExamType.EXAM));
        assertEquals(List.of(6.0, 12.0, 18.0), service.calculateAvailableTotalPoints(List.of(chapter), ExamType.PRACTICE));
    }

    @Test
    public void test_generateExam_goodcase03_balanceDifficultiesAsCloselyAsPossible() {
        ExamGenerationService service = new ExamGenerationService();
        Chapter chapter = new Chapter();
        chapter.setId(1);
        chapter.setTitle("Chapter");
        chapter.setChildElements(List.of(
                subtask(1, "Easy large", 5, SubtaskDifficulty.EASY, ExamType.EXAM),
                subtask(2, "Easy small", 2, SubtaskDifficulty.EASY, ExamType.EXAM),
                subtask(3, "Medium", 4, SubtaskDifficulty.MEDIUM, ExamType.EXAM),
                subtask(4, "Hard", 4, SubtaskDifficulty.HARD, ExamType.EXAM),
                subtask(5, "Hard small", 1, SubtaskDifficulty.HARD, ExamType.EXAM)
        ));

        GeneratedExam generatedExam = service.generateExam(new GenerateExamValues(
                "Klausur",
                10,
                List.of(chapter),
                ExamType.EXAM
        ));

        assertEquals(
                List.of("Easy small", "Medium", "Hard"),
                generatedSubtaskTitles(generatedExam)
        );
    }

    @Test
    public void test_generateExam_goodcase04_nullExamTypeFallsBackToDefaultExamType() {
        ExamGenerationService service = new ExamGenerationService();
        Chapter chapter = ExamTestData.chapter(1, "Chapter", List.of(
                subtask(1, "Exam task", 2, SubtaskDifficulty.MEDIUM, ExamType.EXAM),
                subtask(2, "Practice task", 2, SubtaskDifficulty.MEDIUM, ExamType.PRACTICE)
        ));

        GeneratedExam generatedExam = service.generateExam(new GenerateExamValues(
                "Klausur",
                2,
                List.of(chapter),
                null
        ));

        assertEquals(List.of("Exam task"), generatedSubtaskTitles(generatedExam));
    }

    @Test
    public void test_generateExam_badcase01_rejectBlankTitle() {
        ExamGenerationException exception = assertThrows(ExamGenerationException.class,
                () -> new ExamGenerationService().generateExam(new GenerateExamValues(
                        "   ",
                        2,
                        List.of(chapterWithSingleExamTask()),
                        ExamType.EXAM
                )));

        assertEquals(ExamGenerationException.Reason.INVALID_TITLE, exception.getReason());
    }

    @Test
    public void test_generateExam_badcase02_rejectInvalidPointStep() {
        ExamGenerationException exception = assertThrows(ExamGenerationException.class,
                () -> new ExamGenerationService().generateExam(new GenerateExamValues(
                        "Klausur",
                        1.25,
                        List.of(chapterWithSingleExamTask()),
                        ExamType.EXAM
                )));

        assertEquals(ExamGenerationException.Reason.INVALID_POINTS, exception.getReason());
    }

    @Test
    public void test_generateExam_badcase03_rejectEmptyChapterSelection() {
        ExamGenerationException exception = assertThrows(ExamGenerationException.class,
                () -> new ExamGenerationService().generateExam(new GenerateExamValues(
                        "Klausur",
                        2,
                        List.of(),
                        ExamType.EXAM
                )));

        assertEquals(ExamGenerationException.Reason.EMPTY_SELECTION, exception.getReason());
    }

    @Test
    public void test_generateExam_badcase04_rejectChapterWithoutGeneratableVariants() {
        Chapter chapter = ExamTestData.chapter(1, "Chapter", List.of(
                ExamTestData.subtaskWithoutVariants(1, "Task without variant", 2)
        ));

        ExamGenerationException exception = assertThrows(ExamGenerationException.class,
                () -> new ExamGenerationService().generateExam(new GenerateExamValues(
                        "Klausur",
                        2,
                        List.of(chapter),
                        ExamType.EXAM
                )));

        assertEquals(ExamGenerationException.Reason.NO_GENERATABLE_SUBTASKS, exception.getReason());
    }

    @Test
    public void test_generateExam_badcase05_reportUnreachablePoints() {
        ExamGenerationException exception = assertThrows(ExamGenerationException.class,
                () -> new ExamGenerationService().generateExam(new GenerateExamValues(
                        "Klausur",
                        3,
                        List.of(chapterWithSingleExamTask()),
                        ExamType.EXAM
                )));

        assertEquals(ExamGenerationException.Reason.POINTS_NOT_REACHABLE, exception.getReason());
        assertEquals(3.0, exception.getRequestedPoints());
        assertEquals(2.0, exception.getClosestReachablePoints());
        assertEquals(2.0, exception.getMaxReachablePoints());
    }

    private Chapter chapterWithMixedExamTypes() {
        Chapter chapter = new Chapter();
        chapter.setId(1);
        chapter.setTitle("Chapter");
        chapter.setChildElements(List.of(
                subtask(1, "Exam easy", 4, SubtaskDifficulty.EASY, ExamType.EXAM),
                subtask(2, "Practice easy", 4, SubtaskDifficulty.EASY, ExamType.PRACTICE),
                subtask(3, "Exam medium", 4, SubtaskDifficulty.MEDIUM, ExamType.EXAM),
                subtask(4, "Practice medium", 4, SubtaskDifficulty.MEDIUM, ExamType.PRACTICE),
                subtask(5, "Exam hard", 4, SubtaskDifficulty.HARD, ExamType.EXAM),
                subtask(6, "Practice hard", 4, SubtaskDifficulty.HARD, ExamType.PRACTICE)
        ));
        return chapter;
    }

    private Chapter chapterWithSingleExamTask() {
        return ExamTestData.chapter(1, "Chapter", List.of(
                subtask(1, "Exam task", 2, SubtaskDifficulty.MEDIUM, ExamType.EXAM)
        ));
    }

    private Subtask subtask(int id, String title, double points, SubtaskDifficulty difficulty, ExamType examType) {
        Subtask subtask = new Subtask();
        subtask.setId(id);
        subtask.setChapterId(1);
        subtask.setTitle(title);
        subtask.setPoints(points);
        subtask.setDifficulty(difficulty);
        subtask.setLabels(List.of(examType.getLabel()));
        subtask.setChildElements(List.of(variant(id)));
        return subtask;
    }

    private Variant variant(int id) {
        Variant variant = new Variant();
        variant.setId(id);
        variant.setTitle("Variant " + id);
        variant.setQuestion("Question " + id);
        variant.setSolution("Solution " + id);
        return variant;
    }

    private List<String> generatedSubtaskTitles(GeneratedExam generatedExam) {
        return generatedExam.chapters().stream()
                .flatMap(chapter -> chapter.subtasks().stream())
                .map(generatedSubtask -> generatedSubtask.subtask().getTitle())
                .toList();
    }
}

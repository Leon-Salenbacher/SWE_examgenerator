package support;

import models.Chapter;
import models.ExamType;
import models.Subtask;
import models.SubtaskDifficulty;
import models.Variant;
import service.exam.dto.GeneratedChapter;
import service.exam.dto.GeneratedExam;
import service.exam.dto.GeneratedSubtask;

import java.util.List;

public final class ExamTestData {

    private ExamTestData() {
    }

    public static Chapter chapter(int id, String title, List<Subtask> subtasks) {
        Chapter chapter = new Chapter();
        chapter.setId(id);
        chapter.setTitle(title);
        chapter.setChildElements(subtasks);
        return chapter;
    }

    public static Subtask subtask(int id, String title, double points, SubtaskDifficulty difficulty, ExamType examType) {
        Subtask subtask = new Subtask();
        subtask.setId(id);
        subtask.setChapterId(1);
        subtask.setTitle(title);
        subtask.setPoints(points);
        subtask.setDifficulty(difficulty);
        subtask.setLabels(examType == null ? List.of() : List.of(examType.getLabel()));
        subtask.setChildElements(List.of(variant(id)));
        return subtask;
    }

    public static Subtask subtaskWithoutVariants(int id, String title, double points) {
        Subtask subtask = new Subtask();
        subtask.setId(id);
        subtask.setChapterId(1);
        subtask.setTitle(title);
        subtask.setPoints(points);
        subtask.setDifficulty(SubtaskDifficulty.MEDIUM);
        subtask.setLabels(List.of(ExamType.EXAM.getLabel()));
        subtask.setChildElements(List.of());
        return subtask;
    }

    public static Variant variant(int id) {
        Variant variant = new Variant();
        variant.setId(id);
        variant.setTitle("Variant " + id);
        variant.setQuestion("Question " + id);
        variant.setSolution("Solution " + id);
        return variant;
    }

    public static GeneratedExam generatedExam() {
        Chapter chapter = new Chapter();
        chapter.setId(1);
        chapter.setTitle("Requirements");

        Subtask subtask = new Subtask();
        subtask.setId(1);
        subtask.setTitle("Task 1");
        subtask.setPoints(5);

        return new GeneratedExam(
                "Exam",
                5,
                List.of(new GeneratedChapter(chapter, List.of(new GeneratedSubtask(subtask, variant(1)))))
        );
    }
}

package service.pdf;

import models.Chapter;
import models.Subtask;
import models.Variant;
import service.exam.dto.GeneratedChapter;
import service.exam.dto.GeneratedExam;
import service.exam.dto.GeneratedSubtask;
import service.pdf.dto.PdfElement;

import java.util.ArrayList;
import java.util.List;

final class PdfExamElementBuilder {

    private final PdfTextFormatter textFormatter;

    PdfExamElementBuilder(PdfTextFormatter textFormatter) {
        this.textFormatter = textFormatter;
    }

    List<PdfElement> buildElements(GeneratedExam exam, boolean includeSolutions) {
        List<PdfElement> elements = new ArrayList<>();
        elements.add(PdfElement.text(exam.title()));
        elements.add(PdfElement.text("Total points: " + exam.totalPoints()));
        elements.add(PdfElement.text(""));

        int questionNumber = 1;
        for (GeneratedChapter generatedChapter : exam.chapters()) {
            Chapter chapter = generatedChapter.chapter();
            elements.add(PdfElement.text("Chapter: " + textFormatter.safeLabel(chapter.getTitle(), "Chapter " + chapter.getId())));

            for (GeneratedSubtask generatedSubtask : generatedChapter.subtasks()) {
                Subtask subtask = generatedSubtask.subtask();
                Variant variant = generatedSubtask.variant();

                elements.add(PdfElement.text(""));
                elements.add(PdfElement.text(questionNumber + ". " + textFormatter.safeLabel(subtask.getTitle(), "Task " + subtask.getId())
                        + " (" + subtask.getPoints() + " pts)"));
                textFormatter.wrap("Question: " + textFormatter.safeLabel(variant.getQuestion(), "No question text available."))
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
                elements.add(PdfElement.answerBox(textFormatter.wrap(solution.trim(), PdfLayoutMetrics.ANSWER_BOX_TEXT_MAX_CHARS)));
                return;
            }
        }

        elements.add(PdfElement.answerBox(List.of()));
    }
}

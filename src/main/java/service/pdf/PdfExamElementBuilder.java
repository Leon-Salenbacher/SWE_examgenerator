package service.pdf;

import models.Chapter;
import models.Points;
import models.Subtask;
import models.Variant;
import service.exam.dto.GeneratedChapter;
import service.exam.dto.GeneratedExam;
import service.exam.dto.GeneratedSubtask;
import service.pdf.dto.PdfElement;
import service.pdf.metrics.PdfLayoutMetrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts a generated exam into renderable PDF elements.
 */
final class PdfExamElementBuilder {

    private final PdfTextFormatter textFormatter;

    PdfExamElementBuilder(PdfTextFormatter textFormatter) {
        this.textFormatter = textFormatter;
    }

    /**
     * Builds the ordered body elements for all chapters, tasks, questions, and answer blocks.
     *
     * @param exam generated exam data
     * @param includeSolutions whether solution text should be rendered into the answer boxes
     * @return renderable body elements
     */
    List<PdfElement> buildElements(GeneratedExam exam, boolean includeSolutions) {
        List<PdfElement> elements = new ArrayList<>();

        int chapterNumber = 1;
        for (GeneratedChapter generatedChapter : exam.chapters()) {
            Chapter chapter = generatedChapter.chapter();
            double chapterPoints = calculateChapterPoints(generatedChapter);
            elements.add(PdfElement.chapterHeading(chapterNumber + ". "
                    + textFormatter.safeLabel(chapter.getTitle(), "Chapter " + chapter.getId())
                    + " (" + Points.format(chapterPoints) + " pts)"));

            int subtaskNumber = 0;
            for (GeneratedSubtask generatedSubtask : generatedChapter.subtasks()) {
                Subtask subtask = generatedSubtask.subtask();
                Variant variant = generatedSubtask.variant();
                String subtaskLabel = formatAlphabeticLabel(subtaskNumber);

                elements.add(PdfElement.text(""));
                elements.add(PdfElement.taskHeading(subtaskLabel + ") " + textFormatter.safeLabel(subtask.getTitle(), "Task " + subtask.getId())
                        + " (" + Points.format(subtask.getPoints()) + " pts)"));
                textFormatter.wrap("Question: " + textFormatter.safeLabel(variant.getQuestion(), "No question text available."))
                        .forEach(line -> elements.add(PdfElement.text(line)));
                appendAnswerPlaceholder(elements, subtask.getPoints(), variant, includeSolutions);
                subtaskNumber++;
            }

            elements.add(PdfElement.text(""));
            chapterNumber++;
        }

        return elements;
    }

    /**
     * Calculates the sum of all selected task points in one generated chapter.
     *
     * @param chapter generated chapter content
     * @return total points for the chapter
     */
    private double calculateChapterPoints(GeneratedChapter chapter) {
        return chapter.subtasks().stream()
                .mapToDouble(generatedSubtask -> generatedSubtask.subtask().getPoints())
                .sum();
    }

    /**
     * Formats a zero-based task index as an alphabetic label such as a, b, or aa.
     *
     * @param zeroBasedIndex zero-based task index
     * @return alphabetic label for the task
     */
    private String formatAlphabeticLabel(int zeroBasedIndex) {
        StringBuilder label = new StringBuilder();
        int index = zeroBasedIndex;
        do {
            label.insert(0, (char) ('a' + (index % 26)));
            index = (index / 26) - 1;
        } while (index >= 0);
        return label.toString();
    }

    /**
     * Appends an answer label, answer box, and bottom spacing for one task.
     *
     * @param elements target element list
     * @param points task points used to size the answer box
     * @param variant selected variant that may contain a solution
     * @param includeSolutions whether to render the solution text
     */
    private void appendAnswerPlaceholder(List<PdfElement> elements, double points, Variant variant, boolean includeSolutions) {
        elements.add(PdfElement.text(""));
        elements.add(PdfElement.answerLabel("Answer:"));
        if (includeSolutions) {
            String solution = variant == null ? "" : variant.getSolution();
            if (solution != null && !solution.isBlank()) {
                elements.add(PdfElement.answerBox(textFormatter.wrap(solution.trim(), PdfLayoutMetrics.ANSWER_BOX_TEXT_MAX_CHARS), points));
                elements.add(PdfElement.answerBlockBottomSpacing());
                return;
            }
        }

        elements.add(PdfElement.answerBox(List.of(), points));
        elements.add(PdfElement.answerBlockBottomSpacing());
    }
}

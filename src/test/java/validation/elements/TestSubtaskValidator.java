package validation.elements;

import models.Subtask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSubtaskValidator {

    @Test
    public void test_validate_goodcase01_acceptNonNegativeHalfStepPoints() {
        Subtask subtask = subtask("Task", 2.5);

        ValidationResult validationResult = new SubtaskValidator().validate(subtask);

        assertTrue(validationResult.isValid());
    }

    @Test
    public void test_validate_goodcase02_acceptZeroPointsForDraftSubtask() {
        Subtask subtask = subtask("Task", 0);

        ValidationResult validationResult = new SubtaskValidator().validate(subtask);

        assertTrue(validationResult.isValid());
    }

    @Test
    public void test_validate_badcase01_rejectNegativePoints() {
        Subtask subtask = subtask("Task", -0.5);

        ValidationResult validationResult = new SubtaskValidator().validate(subtask);

        assertFalse(validationResult.isValid());
    }

    @Test
    public void test_validate_badcase02_rejectNonHalfStepPoints() {
        Subtask subtask = subtask("Task", 1.25);

        ValidationResult validationResult = new SubtaskValidator().validate(subtask);

        assertFalse(validationResult.isValid());
    }

    @Test
    public void test_validate_badcase03_rejectBlankTitle() {
        Subtask subtask = subtask(" ", 1);

        ValidationResult validationResult = new SubtaskValidator().validate(subtask);

        assertFalse(validationResult.isValid());
    }

    private Subtask subtask(String title, double points) {
        Subtask subtask = new Subtask();
        subtask.setTitle(title);
        subtask.setPoints(points);
        return subtask;
    }
}

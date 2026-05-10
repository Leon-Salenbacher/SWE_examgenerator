package validation.elements;

import models.Points;
import models.Subtask;

/**
 * Validator for subtasks, including point-specific rules.
 */
public class SubtaskValidator extends AbstractTitleValidator<Subtask> {
    @Override
    protected ValidationResult validateInternal(Subtask element) {
        if (element.getPoints() < 0) {
            return ValidationResult.error(localizationService.get("validation.points.nonNegative"));
        }
        if (!Points.isHalfStep(element.getPoints())) {
            return ValidationResult.error(localizationService.get("validation.points.halfStep"));
        }

        return ValidationResult.ok();
    }
}

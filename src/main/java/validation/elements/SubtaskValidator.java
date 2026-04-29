package validation.elements;

import models.Points;
import models.Subtask;

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

package validation;

import objects.Subtask;

public class SubtaskValidator extends AbstractTitleValidator<Subtask> {
    @Override
    protected ValidationResult validateInternal(Subtask element) {
        if (element.getPoints() < 0) {
            return ValidationResult.error(localizationService.get("validation.points.nonNegative"));
        }

        return ValidationResult.ok();
    }
}

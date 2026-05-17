package validation.elements;

import models.Variant;

/**
 * Validator for variants, including the required question text.
 */
public class VariantValidator extends AbstractTitleValidator<Variant> {
    @Override
    protected ValidationResult validateInternal(Variant element) {
        if (isBlank(element.getQuestion())) {
            return ValidationResult.error(localizationService.get("validation.question.required"));
        }

        return ValidationResult.ok();
    }
}

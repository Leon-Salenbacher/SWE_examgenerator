package validation.elements;

import models.Variant;

public class VariantValidator extends AbstractTitleValidator<Variant> {
    @Override
    protected ValidationResult validateInternal(Variant element) {
        if (isBlank(element.getQuestion())) {
            return ValidationResult.error(localizationService.get("validation.question.required"));
        }

        return ValidationResult.ok();
    }
}

package validation;

import objects.ChildObject;
import service.impl.LocalizationService;

public abstract class AbstractTitleValidator<T extends ChildObject> {
    protected final LocalizationService localizationService = LocalizationService.getInstance();

    public ValidationResult validate(T element) {
        if (element == null) {
            return ValidationResult.error(localizationService.get("validation.element.required"));
        }

        if (isBlank(element.getTitle())) {
            return ValidationResult.error(localizationService.get("validation.title.required"));
        }

        return validateInternal(element);
    }

    protected abstract ValidationResult validateInternal(T element);

    protected boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

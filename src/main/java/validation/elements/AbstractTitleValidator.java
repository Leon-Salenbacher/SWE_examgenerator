package validation.elements;

import models.ChildObject;
import service.impl.LocalizationService;

/**
 * Shared validator base for editable objects that require a non-blank title.
 *
 * @param <T> validated domain object type
 */
public abstract class AbstractTitleValidator<T extends ChildObject> {
    protected final LocalizationService localizationService = LocalizationService.getInstance();

    /**
     * Validates the common title rule before delegating to type-specific checks.
     *
     * @param element object to validate
     * @return validation result for the object
     */
    public ValidationResult validate(T element) {
        if (element == null) {
            return ValidationResult.error(localizationService.get("validation.element.required"));
        }

        if (isBlank(element.getTitle())) {
            return ValidationResult.error(localizationService.get("validation.title.required"));
        }

        return validateInternal(element);
    }

    /**
     * Performs type-specific validation after the title was accepted.
     *
     * @param element object to validate
     * @return validation result for type-specific rules
     */
    protected abstract ValidationResult validateInternal(T element);

    /**
     * @param value text value
     * @return {@code true} when the value is null or blank
     */
    protected boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

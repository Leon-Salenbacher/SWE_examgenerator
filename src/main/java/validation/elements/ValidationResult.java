package validation.elements;

/**
 * Immutable validation result returned by editor validators.
 *
 * @param isValid whether validation succeeded
 * @param message localized error message, or an empty string for success
 */
public record ValidationResult(boolean isValid, String message) {
    /**
     * @return successful validation result
     */
    public static ValidationResult ok() {
        return new ValidationResult(true, "");
    }

    /**
     * Creates a failed validation result.
     *
     * @param message localized validation message
     * @return failed validation result
     */
    public static ValidationResult error(String message) {
        return new ValidationResult(false, message);
    }
}

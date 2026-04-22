package validation.elements;

public record ValidationResult(boolean isValid, String message) {
    public static ValidationResult ok() {
        return new ValidationResult(true, "");
    }

    public static ValidationResult error(String message) {
        return new ValidationResult(false, message);
    }
}

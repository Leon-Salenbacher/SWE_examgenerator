package models;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility methods for parsing, validating and formatting point values.
 */
public final class Points {
    private static final BigDecimal HALF_POINT_FACTOR = BigDecimal.valueOf(2);

    private Points() {
    }

    /**
     * Parses user input while accepting both comma and dot decimal separators.
     *
     * @param rawValue raw value from UI or XML
     * @return parsed point value, or {@code 0} for empty input
     */
    public static double parse(String rawValue) {
        String normalized = normalizeInput(rawValue);
        if (normalized == null || normalized.isBlank()) {
            return 0;
        }
        if (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isBlank()) {
            return 0;
        }
        return new BigDecimal(normalized).doubleValue();
    }

    /**
     * Normalizes decimal separators and surrounding whitespace.
     *
     * @param rawValue raw point value
     * @return normalized string or {@code null}
     */
    public static String normalizeInput(String rawValue) {
        return rawValue == null ? null : rawValue.trim().replace(',', '.');
    }

    /**
     * Checks whether a point value can be represented in 0.5 steps.
     *
     * @param points point value to validate
     * @return {@code true} for whole or half-point values
     */
    public static boolean isHalfStep(double points) {
        BigDecimal scaledPoints = BigDecimal.valueOf(points).multiply(HALF_POINT_FACTOR);
        try {
            scaledPoints.setScale(0, RoundingMode.UNNECESSARY);
            return true;
        } catch (ArithmeticException ignored) {
            return false;
        }
    }

    /**
     * Converts a point value into integer half-point units.
     *
     * @param points point value using 0.5 steps
     * @return half-point representation
     */
    public static int toHalfPoints(double points) {
        return BigDecimal.valueOf(points)
                .multiply(HALF_POINT_FACTOR)
                .setScale(0, RoundingMode.UNNECESSARY)
                .intValueExact();
    }

    /**
     * Converts integer half-point units back to a decimal point value.
     *
     * @param halfPoints half-point representation
     * @return decimal point value
     */
    public static double fromHalfPoints(int halfPoints) {
        return halfPoints / HALF_POINT_FACTOR.doubleValue();
    }

    /**
     * Formats points without unnecessary trailing zeros.
     *
     * @param points point value to format
     * @return compact decimal string
     */
    public static String format(double points) {
        BigDecimal normalized = BigDecimal.valueOf(points).stripTrailingZeros();
        if (normalized.scale() < 0) {
            normalized = normalized.setScale(0);
        }
        return normalized.toPlainString();
    }
}

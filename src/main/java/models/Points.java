package models;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Points {
    private static final BigDecimal HALF_POINT_FACTOR = BigDecimal.valueOf(2);

    private Points() {
    }

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

    public static String normalizeInput(String rawValue) {
        return rawValue == null ? null : rawValue.trim().replace(',', '.');
    }

    public static boolean isHalfStep(double points) {
        BigDecimal scaledPoints = BigDecimal.valueOf(points).multiply(HALF_POINT_FACTOR);
        try {
            scaledPoints.setScale(0, RoundingMode.UNNECESSARY);
            return true;
        } catch (ArithmeticException ignored) {
            return false;
        }
    }

    public static int toHalfPoints(double points) {
        return BigDecimal.valueOf(points)
                .multiply(HALF_POINT_FACTOR)
                .setScale(0, RoundingMode.UNNECESSARY)
                .intValueExact();
    }

    public static double fromHalfPoints(int halfPoints) {
        return halfPoints / HALF_POINT_FACTOR.doubleValue();
    }

    public static String format(double points) {
        BigDecimal normalized = BigDecimal.valueOf(points).stripTrailingZeros();
        if (normalized.scale() < 0) {
            normalized = normalized.setScale(0);
        }
        return normalized.toPlainString();
    }
}

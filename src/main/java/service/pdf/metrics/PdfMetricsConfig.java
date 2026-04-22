package service.pdf.metrics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads the flat PDF metric values from the bundled {@code pdf-metrics.yml} resource.
 */
final class PdfMetricsConfig {

    private static final String CONFIG_RESOURCE = "/pdf-metrics.yml";
    private static final Map<String, String> VALUES = loadValues();

    private PdfMetricsConfig() {
    }

    /**
     * Reads an integer metric value.
     *
     * @param key dotted metric key
     * @param fallback fallback used when the key is missing or invalid
     * @return configured integer value or fallback
     */
    static int intValue(String key, int fallback) {
        String value = VALUES.get(key);
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    /**
     * Reads a boolean metric value.
     *
     * @param key dotted metric key
     * @param fallback fallback used when the key is missing
     * @return configured boolean value or fallback
     */
    static boolean booleanValue(String key, boolean fallback) {
        String value = VALUES.get(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    /**
     * Reads a string metric value.
     *
     * @param key dotted metric key
     * @param fallback fallback used when the key is missing
     * @return configured string value or fallback
     */
    static String stringValue(String key, String fallback) {
        String value = VALUES.get(key);
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * Reads a charset metric value.
     *
     * @param key dotted metric key
     * @param fallback fallback used when the key is missing or unsupported
     * @return configured charset or fallback charset
     */
    static Charset charsetValue(String key, String fallback) {
        String charsetName = stringValue(key, fallback);
        if (!Charset.isSupported(charsetName)) {
            charsetName = fallback;
        }
        return Charset.forName(charsetName);
    }

    private static Map<String, String> loadValues() {
        Map<String, String> values = new HashMap<>();
        try (InputStream input = PdfMetricsConfig.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (input == null) {
                return values;
            }
            parse(input, values);
        } catch (IOException exception) {
            return values;
        }
        return values;
    }

    private static void parse(InputStream input, Map<String, String> values) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String section = "";
            String line;
            while ((line = reader.readLine()) != null) {
                String withoutComment = stripComment(line);
                if (withoutComment.isBlank()) {
                    continue;
                }

                String trimmed = withoutComment.trim();
                if (!Character.isWhitespace(withoutComment.charAt(0)) && trimmed.endsWith(":")) {
                    section = trimmed.substring(0, trimmed.length() - 1);
                    continue;
                }

                int separatorIndex = trimmed.indexOf(':');
                if (separatorIndex <= 0 || section.isBlank()) {
                    continue;
                }

                String key = trimmed.substring(0, separatorIndex).trim();
                String value = trimmed.substring(separatorIndex + 1).trim();
                if (!key.isBlank() && !value.isBlank()) {
                    values.put(section + "." + key, unquote(value));
                }
            }
        }
    }

    private static String stripComment(String line) {
        int commentIndex = line.indexOf('#');
        return commentIndex < 0 ? line : line.substring(0, commentIndex);
    }

    private static String unquote(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}

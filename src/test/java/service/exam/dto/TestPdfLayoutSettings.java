package service.exam.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPdfLayoutSettings {

    @Test
    public void test_defaults_goodcase01_useExamTitleAndStandardValues() {
        PdfLayoutSettings settings = PdfLayoutSettings.defaults("Exam 2026");

        assertFalse(settings.coverPageEnabled());
        assertEquals("Exam 2026", settings.coverTitle());
        assertEquals("", settings.coverSubtitle());
        assertEquals("", settings.headerText());
        assertEquals("", settings.footerText());
        assertTrue(settings.pageNumbersEnabled());
        assertEquals(PdfLayoutSettings.DEFAULT_ANSWER_BOX_HEIGHT_PER_POINT, settings.answerBoxHeightPerPoint());
    }

    @Test
    public void test_sanitize_goodcase01_trimTextAndUseFallbacks() {
        PdfLayoutSettings settings = new PdfLayoutSettings(
                true,
                "   ",
                " Subtitle ",
                " Header ",
                null,
                false,
                24
        );

        PdfLayoutSettings sanitized = settings.sanitize("Fallback Exam");

        assertTrue(sanitized.coverPageEnabled());
        assertEquals("Fallback Exam", sanitized.coverTitle());
        assertEquals("Subtitle", sanitized.coverSubtitle());
        assertEquals("Header", sanitized.headerText());
        assertEquals("", sanitized.footerText());
        assertFalse(sanitized.pageNumbersEnabled());
        assertEquals(24, sanitized.answerBoxHeightPerPoint());
    }

    @Test
    public void test_sanitize_goodcase02_clampAnswerBoxHeight() {
        assertEquals(0, PdfLayoutSettings.sanitizeAnswerBoxHeightPerPoint(-1));
        assertEquals(60, PdfLayoutSettings.sanitizeAnswerBoxHeightPerPoint(99));
        assertEquals(18, PdfLayoutSettings.sanitizeAnswerBoxHeightPerPoint(18));
    }

    @Test
    public void test_summary_goodcase01_describeSanitizedAnswerBoxHeight() {
        PdfLayoutSettings settings = new PdfLayoutSettings(true, "Exam", "", "Header", "", true, 99);

        String summary = settings.summary();

        assertTrue(summary.contains("Deckblatt aktiv"));
        assertTrue(summary.contains("Header aktiv"));
        assertTrue(summary.contains("Seitenzahlen aktiv"));
        assertTrue(summary.contains("Antwortfelder: 60/Punkt"));
    }
}

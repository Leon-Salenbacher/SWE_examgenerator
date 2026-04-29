package service.pdf;

import org.junit.jupiter.api.Test;
import service.pdf.dto.PdfElement;
import service.pdf.metrics.PdfElementMetrics;
import service.pdf.metrics.PdfLayoutMetrics;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPdfElement {

    @Test
    public void test_answerBox_goodcase01_scaleHeightByPoints() {
        PdfElement fivePointBox = PdfElement.answerBox(List.of(), 5);
        PdfElement tenPointBox = PdfElement.answerBox(List.of(), 10);

        assertEquals(expectedPointHeight(5), fivePointBox.height());
        assertEquals(expectedPointHeight(10), tenPointBox.height());
        assertTrue(tenPointBox.height() > fivePointBox.height());
    }

    @Test
    public void test_answerBox_goodcase02_keepEnoughSpaceForSolutionText() {
        List<String> solutionLines = Collections.nCopies(20, "Solution line");

        PdfElement answerBox = PdfElement.answerBox(solutionLines, 1);

        assertEquals(expectedContentHeight(solutionLines), answerBox.height());
    }

    private int expectedPointHeight(double points) {
        return Math.max(
                PdfLayoutMetrics.ANSWER_BOX_MIN_HEIGHT,
                (int) Math.ceil(points * PdfLayoutMetrics.ANSWER_BOX_HEIGHT_PER_POINT)
        );
    }

    private int expectedContentHeight(List<String> lines) {
        return (lines.size() * PdfElementMetrics.lineHeight())
                + (PdfElementMetrics.answerBoxPadding() * 2)
                + PdfElementMetrics.lineHeight();
    }
}

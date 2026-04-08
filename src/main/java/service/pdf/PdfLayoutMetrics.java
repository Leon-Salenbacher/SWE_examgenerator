package service.pdf;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

final class PdfLayoutMetrics {

    static final Charset PDF_CHARSET = Charset.forName("windows-1252");
    static final CharsetEncoder PDF_ENCODER = PDF_CHARSET.newEncoder();
    static final int MAX_CHARS_PER_LINE = 90;
    static final int DEFAULT_START_Y = 790;
    static final int HEADER_START_Y = 760;
    static final int FOOTER_Y = 45;
    static final int PAGE_NUMBER_Y = 28;
    static final int COVER_TITLE_Y = 470;
    static final int COVER_SUBTITLE_Y = 430;
    static final int LINE_HEIGHT = 14;
    static final int BODY_X = 50;
    static final int ANSWER_BOX_WIDTH = 495;
    static final int ANSWER_BOX_MIN_HEIGHT = 86;
    static final int ANSWER_BOX_PADDING = 8;
    static final int ANSWER_BOX_TEXT_MAX_CHARS = 78;

    private PdfLayoutMetrics() {
    }

    static int calculateBodyStartY(String headerText) {
        return headerText == null || headerText.isBlank() ? DEFAULT_START_Y : HEADER_START_Y;
    }

    static int calculateBodyBottomY(String footerText, boolean pageNumbersEnabled) {
        if ((footerText != null && !footerText.isBlank()) || pageNumbersEnabled) {
            return 70;
        }
        return 45;
    }
}

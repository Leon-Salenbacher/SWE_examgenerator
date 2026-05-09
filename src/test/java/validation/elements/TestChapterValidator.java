package validation.elements;

import models.Chapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestChapterValidator {

    @Test
    public void test_validate_goodcase01_acceptChapterWithTitle() {
        Chapter chapter = new Chapter();
        chapter.setTitle("Chapter 1");

        ValidationResult validationResult = new ChapterValidator().validate(chapter);

        assertTrue(validationResult.isValid());
    }

    @Test
    public void test_validate_badcase01_rejectMissingChapter() {
        ValidationResult validationResult = new ChapterValidator().validate(null);

        assertFalse(validationResult.isValid());
    }

    @Test
    public void test_validate_badcase02_rejectBlankTitle() {
        Chapter chapter = new Chapter();
        chapter.setTitle("   ");

        ValidationResult validationResult = new ChapterValidator().validate(chapter);

        assertFalse(validationResult.isValid());
    }
}

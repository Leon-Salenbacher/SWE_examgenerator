package validation.elements;

import models.Variant;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestVariantValidator {

    @Test
    public void test_validate_goodcase01_acceptVariantWithoutSolution() {
        Variant variant = Variant.builder()
                .id(1)
                .title("Variante A")
                .question("Frage A")
                .build();

        ValidationResult validationResult = new VariantValidator().validate(variant);

        assertTrue(validationResult.isValid());
    }

    @Test
    public void test_validate_badcase01_rejectBlankQuestion() {
        Variant variant = Variant.builder()
                .id(1)
                .title("Variante A")
                .question("   ")
                .build();

        ValidationResult validationResult = new VariantValidator().validate(variant);

        assertFalse(validationResult.isValid());
    }

    @Test
    public void test_validate_goodcase02_acceptBlankTitleWhenQuestionIsPresent() {
        Variant variant = Variant.builder()
                .id(1)
                .title("")
                .question("Frage A")
                .build();

        ValidationResult validationResult = new VariantValidator().validate(variant);

        assertTrue(validationResult.isValid());
    }
}

package validation.elements;

import models.Chapter;

public class ChapterValidator extends AbstractTitleValidator<Chapter> {
    @Override
    protected ValidationResult validateInternal(Chapter element) {
        return ValidationResult.ok();
    }
}

package repository.detailed;

import objects.Variant;
import repository.ChildRepository;

public interface VariantRepository extends ChildRepository<Variant> {
    String ELEMENT_TAG_NAME = "Variant";

    String QUESTION_ATTRIBUTE_LABEL = "question";
    String SOLUTION_ATTRIBUTE_LABEL = "solution";
}

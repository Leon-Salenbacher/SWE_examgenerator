package repository.detailed;

import models.Variant;
import repository.ChildRepository;

/**
 * Repository contract for persisted {@link Variant} entites.
 * <p>
 *     Variants are leaf objects inside the repository model and therefore only need
 *     the standard CRUD operations from {@link ChildRepository}.
 * </p>
 */
public interface VariantRepository extends ChildRepository<Variant> {

    /** XML tag name used for variant elements. */
    String ELEMENT_TAG_NAME = "Variant";

    /** XML attribute name storing the question text. */
    String QUESTION_ATTRIBUTE_LABEL = "question";

    /** XML attribute name storing the solution text. */
    String SOLUTION_ATTRIBUTE_LABEL = "solution";
}

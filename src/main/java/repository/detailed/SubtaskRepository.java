package repository.detailed;

import objects.Subtask;
import objects.Variant;
import repository.ParentRepository;

/**
 * Repository contract for persisted {@link Subtask} aggregates.
 * <p>
 *     Subtask are stored as parent element and can contain nested
 *     {@link Variant} elements that belong to the same aggregate.
 * </p>
 */
public interface SubtaskRepository extends ParentRepository<Subtask, Variant> {
    /** XML tag name used for subtask elements. */
    String ELEMENT_TAG_NAME = "Subtask";

    /** XML tag name used for nested variant elements. */
    String CHILD_TAG_NAME = "Variant";

    /** XML attribute name that stores the awarded points of a subtask. */
    String POINT_ATTRIBUTE_LABEL = "points";

    /** XML attribute name that stores the owning chapter id. */
    String PARENT_ID_ATTRIBUTE_LABEL = "chapterId";

    /** XML attribute name that stores the label collection of a subtask. */
    String LABELS_ATTRIBUTE_LABEL = "labels";



}

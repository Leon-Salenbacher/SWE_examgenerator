package repository.detailed;

import objects.Chapter;
import objects.Subtask;
import repository.ParentRepository;

/**
 * Repository contract for persisted {@link Chapter} aggregates.
 * <p>
 *     A chapter is stored as a parent XML element that may contain nested
 *     {@link Subtask} elements.
 * </p>
 */
public interface ChapterRepository extends ParentRepository<Chapter, Subtask> {

    /**
     * XML tag name used for chapter elements
     */
    String ELEMENT_TAG_NAME = "Chapter";

    /**
     * XML tag name used for nested subtask elements.
     */
    String CHILD_TAG_NAME = "Subtask";
}

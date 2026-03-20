package repository.detailed;

import objects.Subtask;
import objects.Variant;
import repository.ParentRepository;

public interface SubtaskRepository extends ParentRepository<Subtask, Variant> {
    String ELEMENT_TAG_NAME = "Subtask";
    String CHILD_TAG_NAME = "Variant";

    String POINT_ATTRIBUTE_LABEL = "points";
    String PARENT_ID_ATTRIBUTE_LABEL = "chapterId";
    String LABELS_ATTRIBUTE_LABEL = "labels";



}

package repository.detailed;

import objects.Subtask;
import objects.Variant;
import repository.ParentRepository;

public interface SubtaskRepository extends ParentRepository<Subtask, Variant> {
    public final static String ELEMENT_TAG_NAME = "Subtask";
    public final static String CHILD_TAG_NAME = "Variant";

    public final static String POINT_ATTRIBUTE_LABEL = "points";
    public final static String PARENT_ID_ATTRIBUTE_LABEL = "chapterId";



}

package repository;

import objects.Subtask;
import objects.Variant;

public interface SubtaskRepository extends ParentRepository<Subtask, Variant> {
    public final static String POINT_ATTRIBUTE_LABEL = "points";
    public final static String PARENT_ID_ATTRIBUTE_LABEL = "chapterId";



}

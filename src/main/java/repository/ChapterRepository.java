package repository;

import objects.Chapter;
import objects.Subtask;

public interface ChapterRepository extends ParentRepository<Chapter, Subtask> {

    public static final String ELEMENT_TAG_NAME = "Chapter";
    public static final String CHILD_TAG_NAME = "Subtask";
}

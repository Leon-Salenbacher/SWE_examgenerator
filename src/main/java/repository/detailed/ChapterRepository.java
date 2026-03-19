package repository.detailed;

import objects.Chapter;
import objects.Subtask;
import repository.ParentRepository;

public interface ChapterRepository extends ParentRepository<Chapter, Subtask> {

    public static final String ELEMENT_TAG_NAME = "Chapter";
    public static final String CHILD_TAG_NAME = "Subtask";
}

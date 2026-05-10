package models;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Top-level exam content group that owns a list of subtasks.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class Chapter extends ParentObject<Subtask>{
    public static final String ELEMENT_TAG_NAME = "Chapter";
    public static final String CHILD_ELEMENT_TAG_NAME = "Subtask";

}

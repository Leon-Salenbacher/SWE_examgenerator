package service.impl.elements;

import models.Chapter;
import models.Subtask;
import repository.ParentRepository;

/**
 * Service for creating, updating and loading chapter aggregates.
 */
public class ChapterServiceImpl
        extends ParentServiceImpl<
            Chapter,
            Subtask,
            ChapterServiceImpl.ChapterCommand
        > {
    private final ParentRepository<Chapter, Subtask> repository;

    /**
     * Creates a chapter service backed by the provided repository.
     *
     * @param repository chapter repository
     */
    public ChapterServiceImpl(ParentRepository<Chapter, Subtask> repository) {
        super(repository);
        this.repository = repository;
    }




    @Override
    protected Chapter mapCreateCommand(ChapterCommand command){
        Chapter chapter = new Chapter();
        chapter.setId(nextId());
        chapter.setTitle(command.title());
        return chapter;
    }

    @Override
    protected Chapter mapUpdateCommand(Chapter current, ChapterCommand command){
        current.setTitle(command.title());
        return current;
    }


    /**
     * Command object for chapter create and update operations.
     */
    public interface ChapterCommand extends ParentCommand {
    }

    private int nextId() {
        return repository.findAll().stream()
                .mapToInt(Chapter::getId)
                .max()
                .orElse(0) + 1;
    }


}

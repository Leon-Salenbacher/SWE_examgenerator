package service.impl.elements;

import models.Chapter;
import models.Subtask;
import repository.ParentRepository;

public class ChapterServiceImpl
        extends ParentServiceImpl<
            Chapter,
            Subtask,
            ChapterServiceImpl.ChapterCommand
        > {
    private final ParentRepository<Chapter, Subtask> repository;

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


    public interface ChapterCommand extends ParentCommand {
    }

    private int nextId() {
        return repository.findAll().stream()
                .mapToInt(Chapter::getId)
                .max()
                .orElse(0) + 1;
    }


}

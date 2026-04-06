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

    public ChapterServiceImpl(ParentRepository<Chapter, Subtask> repository) {
        super(repository);
    }




    @Override
    protected Chapter mapCreateCommand(ChapterCommand command){
        Chapter chapter = new Chapter();
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


}

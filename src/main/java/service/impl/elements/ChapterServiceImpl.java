package service.impl.elements;

import objects.Chapter;
import objects.Subtask;
import repository.ParentRepository;
import service.elements.ParentService;

import java.util.NoSuchElementException;

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

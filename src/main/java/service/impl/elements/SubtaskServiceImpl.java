package service.impl.elements;

import models.Subtask;
import models.SubtaskDifficulty;
import models.Variant;
import repository.ParentRepository;

import java.util.ArrayList;
import java.util.List;

public class SubtaskServiceImpl
    extends ParentServiceImpl <
        Subtask,
        Variant,
        SubtaskServiceImpl.SubtaskCommand
        > {

    public SubtaskServiceImpl(ParentRepository<Subtask, Variant> repository){
        super(repository);
    }

    @Override
    protected Subtask mapCreateCommand(SubtaskCommand command){
        Subtask subtask = new Subtask();
        subtask.setTitle(command.title());
        subtask.setPoints(command.points());
        if(command.parentId() == null){
            throw new IllegalStateException("Parent id must be null");
        }
        subtask.setChapterId(command.parentId());
        subtask.setDifficulty(defaultDifficulty(command.difficulty()));
        subtask.setLabels(command.labels() == null ? new ArrayList<>() : new ArrayList<>(command.labels()));
        return subtask;
    }

    @Override
    protected Subtask mapUpdateCommand(Subtask current, SubtaskCommand command){
        current.setTitle(command.title());
        current.setPoints(command.points());
        if(command.parentId() != null){
            current.setChapterId(command.parentId());
        }
        current.setDifficulty(defaultDifficulty(command.difficulty()));
        current.setLabels(command.labels() == null ? new ArrayList<>() : new ArrayList<>(command.labels()));
        return current;
    }

    public interface SubtaskCommand extends ParentCommand{
        double points();
        SubtaskDifficulty difficulty();
        List<String> labels();
    }

    private SubtaskDifficulty defaultDifficulty(SubtaskDifficulty difficulty) {
        return difficulty == null ? SubtaskDifficulty.MEDIUM : difficulty;
    }
}

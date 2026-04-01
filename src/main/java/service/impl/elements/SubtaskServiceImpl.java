package service.impl.elements;

import objects.Subtask;
import objects.Variant;
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
        current.setLabels(command.labels() == null ? new ArrayList<>() : new ArrayList<>(command.labels()));
        return current;
    }

    public interface SubtaskCommand extends ParentCommand{
        int points();
        List<String> labels();
    }
}

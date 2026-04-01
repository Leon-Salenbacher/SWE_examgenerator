package service.impl.elements;

import objects.Variant;
import repository.ChildRepository;
import service.elements.ChildService;
import service.impl.DataObjectServiceImpl;

public class VariantServiceImpl
    extends DataObjectServiceImpl<
        Variant,
        VariantServiceImpl.VariantCommand
        >
    implements ChildService<Variant, VariantServiceImpl.VariantCommand> {

    public VariantServiceImpl(ChildRepository<Variant> repository){
        super(repository);
    }

    @Override
    protected Variant mapCreateCommand(VariantCommand command){
        Variant variant = new Variant();
        variant.setTitle(command.title());
        variant.setQuestion(command.question());
        variant.setSolution(command.solution());
        return variant;
    }

    @Override
    protected Variant mapUpdateCommand(Variant current, VariantCommand command){
        current.setTitle(command.title());
        current.setQuestion(command.question());
        current.setSolution(command.solution());
        return current;
    }

    public interface VariantCommand extends ChildService.ChildCommand{
        String question();
        String solution();
    }
}

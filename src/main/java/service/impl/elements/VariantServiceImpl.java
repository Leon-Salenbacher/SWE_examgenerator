package service.impl.elements;

import models.Variant;
import repository.ChildRepository;
import service.elements.ChildService;
import service.impl.DataObjectServiceImpl;

/**
 * Service for creating, updating and loading variants.
 */
public class VariantServiceImpl
    extends DataObjectServiceImpl<
        Variant,
        VariantServiceImpl.VariantCommand
        >
    implements ChildService<Variant, VariantServiceImpl.VariantCommand> {

    /**
     * Creates a variant service backed by the provided repository.
     *
     * @param repository variant repository
     */
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

    /**
     * Command object for variant create and update operations.
     */
    public interface VariantCommand extends ChildService.ChildCommand{
        /** @return question text */
        String question();
        /** @return solution text */
        String solution();
    }
}

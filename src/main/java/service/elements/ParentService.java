package service.elements;

import objects.Chapter;
import objects.ParentObject;

public interface ParentService<
            T extends ParentObject<?>,
            C extends
        >
        extends DataObjectService<
            T,

        > {

    public T create(ParentCreateDto newParent);

    public T update(ParentUpdateDto updateParent);

    public void delete(String id);

    public record ParentCreateDto extends CreateDto(
            String title
    ){}

    public record ParentUpdateDto(
            String id,
            String title
    ){}
}

package service;

import objects.ParentObject;

public interface ParentService<T extends ParentObject<?>> {

    public T create(ParentCreateDto newParent);

    public T update(ParentUpdateDto updateParent);

    public void delete(String id);

    public record ParentCreateDto(
            String title
    ){}

    public record ParentUpdateDto(
            String id,
            String title
    ){}
}

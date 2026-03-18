package service.elements;

import objects.DataObject;

public interface DataObjectService<
        T extends DataObject,
        C extends DataObjectService.CreateDto,
        U extends DataObjectService.UpdateDto
        > {

    /**
     * Creates a new Element based on the information given with {@link C}
     * @param newElement
     * @return
     */
    public T create(C newElement) ;

    public T update(U updatedElement) throws ;

    public void delete(String id);


    public record CreateDto(){

    }

    public record UpdateDto(){}

}

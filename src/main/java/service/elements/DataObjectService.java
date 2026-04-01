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
    T create(C newElement) ;

    T update(U updatedElement) throws ;

    void delete(String id);


    record CreateDto(){

    }

    public record UpdateDto(){}

}

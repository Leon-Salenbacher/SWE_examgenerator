package repository;

import objects.ChildObject;

/**
 * The Repository to manage the persistence of {@link ChildObject}s
 *
 * @param <T> The target element.
 */
public interface ChildRepository <T extends ChildObject> extends Repository<T> {

}

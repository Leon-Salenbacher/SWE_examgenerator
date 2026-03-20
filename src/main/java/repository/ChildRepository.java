package repository;

import objects.ChildObject;

/**
 * Marker contract for repositories that persist simple {@link ChildObject} instances.
 *
 * @param <T> Entity type handled by the repository
 */
public interface ChildRepository <T extends ChildObject> extends Repository<T> {

}

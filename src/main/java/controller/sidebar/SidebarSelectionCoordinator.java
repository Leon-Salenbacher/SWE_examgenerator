package controller.sidebar;

import javafx.scene.Node;
import models.ChildObject;

/**
 * Coordinates selection state between sidebar row controllers and the sidebar root.
 */
@FunctionalInterface
public interface SidebarSelectionCoordinator {
    /**
     * Selects the row that represents the given domain object.
     *
     * @param newlySelected row node to mark as selected
     * @param data selected domain object
     */
    public void select(Node newlySelected, ChildObject data);
}

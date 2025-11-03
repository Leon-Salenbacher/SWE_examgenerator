package controller.sidebar;

import javafx.scene.Node;
import objects.ChildObject;

@FunctionalInterface
public interface SidebarSelectionCoordinator {
    public void select(Node newlySelected, ChildObject data);
}

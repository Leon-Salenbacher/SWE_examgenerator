package controller.sidebar;

import javafx.scene.Node;
import models.ChildObject;

@FunctionalInterface
public interface SidebarSelectionCoordinator {
    public void select(Node newlySelected, ChildObject data);
}

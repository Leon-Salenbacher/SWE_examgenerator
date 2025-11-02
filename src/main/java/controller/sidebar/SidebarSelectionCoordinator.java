package controller.sidebar;

import javafx.scene.Node;

@FunctionalInterface
public interface SidebarSelectionCoordinator {
    public void select(Node newlySelected);
}

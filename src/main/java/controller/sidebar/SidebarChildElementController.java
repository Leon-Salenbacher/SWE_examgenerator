package controller.sidebar;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class SidebarChildElementController {
    @FXML
    protected Label title;
    @FXML
    protected VBox root;

    protected SidebarSelectionCoordinator selectionCoordinator;


    public void setTitle(String titleText){
        title.setText(titleText);
    }

    public void setSelectionCoordinator(SidebarSelectionCoordinator sidebarSelectionCoordinator){
        this.selectionCoordinator = sidebarSelectionCoordinator;
    }

    @FXML
    protected void handleSelect(MouseEvent event){
        if(selectionCoordinator != null){
            selectionCoordinator.select(root);
        }

        ObservableList<String> styleClasses = root.getStyleClass();
        if(!styleClasses.contains("selected")){
            styleClasses.add("selected");
        }
    }

}

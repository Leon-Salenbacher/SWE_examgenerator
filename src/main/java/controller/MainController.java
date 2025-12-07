package controller;

import controller.editor.EditorHostController;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import objects.ChildObject;

public class MainController {
    @FXML
    private Label label;
    @FXML
    private SidebarController sidebarController;
    @FXML
    private EditorHostController editorHostController;

    @FXML
    private void initialize(){
        if(sidebarController != null && editorHostController != null){
            sidebarController.setSelectionListener(this::handleSelection);
        }
    }

    @FXML
    private void handleSelection(ChildObject selection){
        editorHostController.displayObject(selection);
    }
}
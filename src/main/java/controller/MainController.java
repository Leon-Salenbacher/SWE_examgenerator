package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {
    @FXML
    private Label label;
    @FXML
    private SidebarController sidebarController;
    @FXML
    private ChapterEditorController chapterEditorController;

    @FXML
    private void initialize(){
        if(sidebarController != null && chapterEditorController != null){
            sidebarController.setChapterSelectionListener(chapterEditorController::displayChapter);
        }
    }

    @FXML
    private void onClick() {
        label.setText("Button clicked!");
    }
}
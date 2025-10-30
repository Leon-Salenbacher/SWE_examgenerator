package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class SidebarController {
    @FXML
    private Label label;
    @FXML private VBox chapterBox;

    @FXML
    private void initialize(){
        this.loadChapters();
    }

    private void loadChapters() {
        try {


            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/Sidebar_ParentElement.fxml"));
            Node node = loader.load();

            Sidebar_ChapterController itemCtrl = loader.getController();
            itemCtrl.setTitle();

            chapterBox.getChildren().add(node);

        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void setChapters(){
        loadChapters();
    }
}

package controller;

import controller.sidebar.SidebarElementController;
import controller.sidebar.SidebarParentElementController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import objects.Chapter;
import objects.Subtask;
import objects.Variant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SidebarController {
    @FXML
    private Label label;
    @FXML private VBox chapterBox;

    @FXML
    private void initialize(){
        this.loadChapters();
    }

    private void loadChapters() {
        List<Chapter> chapters = new ArrayList<Chapter>();
        chapters.add(new Chapter(1, "Chapter 1", Arrays.asList(new Subtask(1, "subtask 1", 1, Arrays.asList(new Variant(1, "Question 1", "Solution 1"), new Variant(2, "Question 2", "Solution 2"))), new Subtask(2, "subtask 2", 1))));
        chapters.add(new Chapter(2, "Chapter 2"));

        for(int i = 0; i < chapters.size(); i++){
            Node node = SidebarElementController.createElement(chapters.get(i));
            chapterBox.getChildren().add(node);
    }
    }
    public void setChapters(){
        loadChapters();
    }
}

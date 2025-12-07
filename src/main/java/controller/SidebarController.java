package controller;

import controller.sidebar.SidebarElementController;
import controller.sidebar.SidebarParentElementController;
import controller.sidebar.SidebarSelectionCoordinator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import objects.Chapter;
import objects.ChildObject;
import objects.Subtask;
import objects.Variant;
import service.LocalizationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class SidebarController implements SidebarSelectionCoordinator {

    @FXML private VBox chapterBox;
    @FXML
    private Label headingLabel;
    private Node selectedNode;
    private Consumer<ChildObject> selectionListener;
    private final LocalizationService localizationService = LocalizationService.getInstance();

    @FXML
    private void initialize(){
        this.loadChapters();
        applyTranslations();
        localizationService.localeProperty().addListener((obs, oldLocale, newLocale) -> applyTranslations());
    }

    private void loadChapters() {
        List<Chapter> chapters = new ArrayList<Chapter>();
        chapters.add(
                new Chapter(
                        1,
                        "Chapter 1",
                        Arrays.asList(
                                new Subtask(
                                        1,
                                        "subtask 1",
                                        5,
                                        1,
                                        Arrays.asList(
                                                new Variant(
                                                        1,
                                                        "Question 1",
                                                        "Solution 1"
                                                ),
                                                new Variant(
                                                        2,
                                                        "Question 2",
                                                        "Solution 2"
                                                )
                                        )
                                ),
                                new Subtask(
                                        2,
                                        "subtask 2",
                                        7,
                                        1
                                )
                        )
                )
        );
        chapters.add(new Chapter(2, "Chapter 2"));

        for(int i = 0; i < chapters.size(); i++){
            Node node = SidebarElementController.createElement(chapters.get(i), this);
            chapterBox.getChildren().add(node);
    }
    }
    public void setChapters(){
        loadChapters();
    }

    @Override
    public void select(Node newlySelected, ChildObject data){
        if(selectedNode == newlySelected){
            return;
        }

        if(selectedNode != null){
            selectedNode.getStyleClass().remove("selected");
        }

        selectedNode= newlySelected;

        if(selectionListener != null){
            selectionListener.accept(data);
        }
    }


    public void setSelectionListener(Consumer<ChildObject> selectionListener) {
        this.selectionListener = selectionListener;
    }

    private void applyTranslations() {
        if (headingLabel != null) {
            headingLabel.setText(localizationService.get("sidebar.heading"));
        }
    }

}

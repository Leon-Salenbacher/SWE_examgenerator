package controller;

import config.ApplicationContext;
import controller.sidebar.SidebarElementController;
import controller.sidebar.SidebarSelectionCoordinator;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import objects.Chapter;
import objects.ChildObject;
import objects.Subtask;
import objects.Variant;
import service.impl.LocalizationService;
import service.impl.elements.ChapterServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SidebarController implements SidebarSelectionCoordinator {

    @FXML private VBox chapterBox;
    @FXML
    private Label headingLabel;
    private Node selectedNode;
    private Consumer<ChildObject> selectionListener;
    private final LocalizationService localizationService = LocalizationService.getInstance();
    private final ChapterServiceImpl chapterService;

    public SidebarController() {
        chapterService = ApplicationContext.getInstance().getChapterService();
    }

    @FXML
    private void initialize(){
        this.loadChapters();
        applyTranslations();
        localizationService.localeProperty().addListener((obs, oldLocale, newLocale) -> applyTranslations());
    }

    private void loadChapters() {
        List<Chapter> chapters = chapterService.getAll();
        chapterBox.getChildren().clear();

        for(Chapter chapter : chapters){
            Node node = SidebarElementController.createElement(chapter, this);
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

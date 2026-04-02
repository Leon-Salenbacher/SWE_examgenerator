package controller;

import config.ApplicationContext;
import controller.sidebar.SidebarElementController;
import controller.sidebar.SidebarSelectionCoordinator;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import controller.sidebar.SidebarChildElementController;
import controller.sidebar.SidebarParentElementController;
import objects.Chapter;
import objects.ChildObject;
import service.impl.LocalizationService;
import service.impl.elements.ChapterServiceImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class SidebarController implements SidebarSelectionCoordinator {

    @FXML private VBox chapterBox;
    @FXML
    private Label headingLabel;
    private Node selectedNode;
    private Consumer<ChildObject> selectionListener;
    private final LocalizationService localizationService = LocalizationService.getInstance();
    private final ChapterServiceImpl chapterService;
    private String selectedKey;

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
        Set<String> expandedKeys = captureExpandedKeys();
        loadChapters();
        restoreExpandedState(expandedKeys);
        restoreSelection();
    }

    public void refreshAndRevealSelection(ChildObject target) {
        Set<String> expandedKeys = captureExpandedKeys();
        loadChapters();
        restoreExpandedState(expandedKeys);
        if (target == null) {
            restoreSelection();
            return;
        }

        SidebarChildElementController controller = findController(target);
        if (controller != null) {
            controller.selectNode();
        } else {
            restoreSelection();
        }
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
        selectedKey = createKey(data);

        if(selectionListener != null){
            selectionListener.accept(data);
        }
    }


    public void setSelectionListener(Consumer<ChildObject> selectionListener) {
        this.selectionListener = selectionListener;
    }

    private SidebarChildElementController findController(ChildObject target) {
        for (Node chapterNode : chapterBox.getChildren()) {
            Object userData = chapterNode.getUserData();
            if (userData instanceof SidebarParentElementController parentController) {
                SidebarChildElementController match = parentController.revealPathTo(target);
                if (match != null) {
                    return match;
                }
            } else if (userData instanceof SidebarChildElementController childController && childController.matchesData(target)) {
                return childController;
            }
        }

        return null;
    }

    private Set<String> captureExpandedKeys() {
        Set<String> expandedKeys = new HashSet<>();
        for (Node chapterNode : chapterBox.getChildren()) {
            collectExpandedKeys(chapterNode, expandedKeys);
        }
        return expandedKeys;
    }

    private void collectExpandedKeys(Node node, Set<String> expandedKeys) {
        Object userData = node.getUserData();
        if (userData instanceof SidebarParentElementController parentController) {
            if (parentController.isOpen()) {
                expandedKeys.add(createKey(parentController.getData()));
            }
            for (Node childNode : parentController.getChildNodes()) {
                collectExpandedKeys(childNode, expandedKeys);
            }
        }
    }

    private void restoreExpandedState(Set<String> expandedKeys) {
        for (Node chapterNode : chapterBox.getChildren()) {
            applyExpandedState(chapterNode, expandedKeys);
        }
    }

    private void applyExpandedState(Node node, Set<String> expandedKeys) {
        Object userData = node.getUserData();
        if (userData instanceof SidebarParentElementController parentController) {
            parentController.applyOpenState(expandedKeys.contains(createKey(parentController.getData())));
            for (Node childNode : parentController.getChildNodes()) {
                applyExpandedState(childNode, expandedKeys);
            }
        }
    }

    private void restoreSelection() {
        if (selectedKey == null) {
            selectedNode = null;
            return;
        }

        SidebarChildElementController controller = findControllerByKey(selectedKey);
        if (controller != null) {
            controller.selectNode();
        } else {
            selectedNode = null;
        }
    }

    private SidebarChildElementController findControllerByKey(String key) {
        for (Node chapterNode : chapterBox.getChildren()) {
            SidebarChildElementController controller = findControllerByKey(chapterNode, key);
            if (controller != null) {
                return controller;
            }
        }
        return null;
    }

    private SidebarChildElementController findControllerByKey(Node node, String key) {
        Object userData = node.getUserData();
        if (userData instanceof SidebarChildElementController childController) {
            if (key.equals(createKey(childController.getData()))) {
                return childController;
            }
        }

        if (userData instanceof SidebarParentElementController parentController) {
            for (Node childNode : parentController.getChildNodes()) {
                SidebarChildElementController nested = findControllerByKey(childNode, key);
                if (nested != null) {
                    return nested;
                }
            }
        }

        return null;
    }

    private String createKey(ChildObject data) {
        if (data == null) {
            return null;
        }
        return data.getClass().getName() + "#" + data.getId();
    }

    private void applyTranslations() {
        if (headingLabel != null) {
            headingLabel.setText(localizationService.get("sidebar.heading"));
        }
    }

}

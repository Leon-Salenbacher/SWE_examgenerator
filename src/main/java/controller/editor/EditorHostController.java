package controller.editor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import objects.ChildObject;
import objects.ParentObject;

import java.io.IOException;

public class EditorHostController {
    @FXML
    private StackPane contentHost;

    private Node parentEditorRoot;
    private Node childEditorRoot;

    private ParentEditorController parentEditorController;
    private ChildEditorController childEditorController;

    @FXML
    private void initialize(){
        showPlaceholder();
    }

    public void displayObject(ChildObject data){
        if(data == null){
            showPlaceholder();
            return;
        }

        if(data instanceof ParentObject<?>){
            showParentEditor((ParentObject<? extends ChildObject>) data);
        }else{
            showChildEditor(data);
        }
    }

    private void showParentEditor(ParentObject<? extends ChildObject> data){
        ensureParentEditor();
        parentEditorController.displayParent(data);
        setContent(parentEditorRoot);
    }

    private void showChildEditor(ChildObject data){
        ensureChildEditor();
        childEditorController.displayChild(data);
        setContent(childEditorRoot);
    }

    private void setContent(Node node){
        contentHost.getChildren().setAll(node);
    }

    private void showPlaceholder(){
        Label placeholder = new Label("Bitte Element aus der Sidebar auswählen");
        placeholder.getStyleClass().add("section-label");
        contentHost.getChildren().setAll(placeholder);
    }

    private void ensureParentEditor(){
        if(parentEditorController != null){
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/elements/ParentEditor.fxml"));
        try {
            parentEditorRoot = loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load parent editor", e);
        }

        parentEditorController = loader.getController();
        parentEditorController.setSelectionHandler(this::displayObject);
    }

    private void ensureChildEditor() {
        if (childEditorController != null) {
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/elements/ChildEditor.fxml"));
        try {
            childEditorRoot = loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load child editor", e);
        }

        childEditorController = loader.getController();
    }
}

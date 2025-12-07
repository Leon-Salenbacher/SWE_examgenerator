package controller.editor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import objects.Chapter;
import objects.ChildObject;
import objects.ParentObject;
import objects.Subtask;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class ParentEditorController {
    @FXML
    private Label headerLabel;

    @FXML
    private TextField titleField;

    @FXML
    private VBox pointsBox;

    @FXML
    private TextField pointsField;

    @FXML
    private Label childSectionLabel;

    @FXML
    private VBox childList;

    private ParentObject<? extends ChildObject> currentParent;
    private Consumer<ChildObject> selectionHandler;

    @FXML
    private void initialize(){
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(currentParent instanceof Chapter){
                ((Chapter) currentParent).setTitle(newValue);
                headerLabel.setText(defaultText(newValue, "Kapitel"));
            }else if(currentParent instanceof Subtask){
                ((Subtask) currentParent).setTitle(newValue);
                headerLabel.setText(defaultText(newValue, "Subtask"));
            }
        });

        pointsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(!(currentParent instanceof Subtask)){
                return;
            }

            try{
                int parsed = newValue == null || newValue.isBlank() ? 0 : Integer.parseInt(newValue);
                ((Subtask) currentParent).setPoints(parsed);
            }catch(NumberFormatException ignored){
                //keep previos value, when input is not a valid integer
                pointsField.setText(oldValue);
            }
        });

        displayPlaceholder();
    }

    public void displayParent(ParentObject<? extends ChildObject> parent){
        this.currentParent = parent;
        if(parent == null){
            displayPlaceholder();
            return;
        }

        if(parent instanceof Chapter){
            displayChapter((Chapter) parent);
        }else if(parent instanceof Subtask){
            displaySubtask((Subtask) parent);
        }else{
            displayGeneric(parent);
        }
    }

    public void setSelectionHandler(Consumer<ChildObject> selectionHandler){
        this.selectionHandler = selectionHandler;
    }

    private void renderChildren(List<? extends ChildObject> children){
        childList.getChildren().clear();

        if(children == null || children.isEmpty()){
            childList.getChildren().add(createEmptyRow());
            return;
        }

        for(ChildObject child : children){
            childList.getChildren().add(createChildRow(child));
        }
    }

    private Node createChildRow(ChildObject child){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/Editor_ChildRow.fxml"));
        try{
            Node node = loader.load();
            EditorChildRowController controller = loader.getController();
            controller.configure(child, selectionHandler);
            return node;
        }catch (IOException e){
            throw new IllegalStateException("Unabel to load editor child row", e);
        }
    }

    private Node createEmptyRow(){
        Label placeholder = new Label("Keine Einträge vorhanden");
        placeholder.getStyleClass().add("subtask-item");
        return placeholder;
    }

    private void displayPlaceholder(){
        headerLabel.setText("Bitte Element wählen");
        titleField.clear();
        pointsField.clear();
        pointsBox.setVisible(false);
        pointsBox.setManaged(false);
        childList.getChildren().setAll(createEmptyRow());
        currentParent = null;
    }

    private String defaultText(String value, String fallback){
        return value == null ? fallback : value;
    }

    private void displayChapter(Chapter chapter){
        headerLabel.setText(defaultText(chapter.getTitle(), "Kapitel"));
        titleField.setText(defaultText(chapter.getTitle(), ""));
        togglePoints(false);
        childSectionLabel.setText("Subtasks");
        renderChildren(chapter.getChildElements());
    }

    private void displaySubtask(Subtask subtask){
        headerLabel.setText(defaultText(subtask.getTitle(), "Subtask"));
        titleField.setText(defaultText(subtask.getTitle(), ""));
        togglePoints(true);
        pointsField.setText(String.valueOf(subtask.getPoints()));
        childSectionLabel.setText("Varianten");
        renderChildren(subtask.getChildElements());
    }

    private void displayGeneric(ParentObject<? extends ChildObject> parent){
        headerLabel.setText(defaultText(parent.getTitle(), "Element"));
        titleField.setText(defaultText(parent.getTitle(), ""));
        togglePoints(false);
        childSectionLabel.setText("Untereelemente");
        renderChildren(parent.getChildElements());
    }

    private void togglePoints(boolean visible){
        pointsBox.setVisible(visible);
        pointsBox.setManaged(visible);
    }
}

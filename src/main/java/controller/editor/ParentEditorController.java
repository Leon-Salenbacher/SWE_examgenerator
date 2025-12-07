package controller.editor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import objects.Chapter;
import objects.ChildObject;
import objects.ParentObject;
import objects.Subtask;
import service.LocalizationService;

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
    private VBox labelsBox;

    @FXML
    private Label labelsLabel;

    @FXML
    private TextField labelsField;

    @FXML
    private Label titleLabel;
    @FXML
    private Label pointsLabel;
    @FXML
    private Button deleteButton;
    @FXML
    private Button saveButton;

    @FXML
    private VBox childList;

    private ParentObject<? extends ChildObject> currentParent;
    private Consumer<ChildObject> selectionHandler;
    private final LocalizationService localizationService = LocalizationService.getInstance();

    @FXML
    private void initialize(){
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(currentParent instanceof Chapter){
                ((Chapter) currentParent).setTitle(newValue);
                headerLabel.setText(defaultText(newValue, localizationService.get("parentEditor.header.chapter")));
            }else if(currentParent instanceof Subtask){
                ((Subtask) currentParent).setTitle(newValue);
                headerLabel.setText(defaultText(newValue, localizationService.get("parentEditor.header.subtask")));
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

        labelsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!(currentParent instanceof Subtask)) {
                return;
            }
            ((Subtask) currentParent).setLabels(parseLabels(newValue));
        });

        displayPlaceholder();
        localizationService.localeProperty().addListener((obs, oldLocale, newLocale) -> applyTranslations());
        applyTranslations();
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
        Label placeholder = new Label(localizationService.get("editor.noEntries"));
        placeholder.getStyleClass().add("subtask-item");
        return placeholder;
    }

    private void displayPlaceholder(){
        headerLabel.setText(localizationService.get("editor.placeholder"));
        titleField.clear();
        pointsField.clear();
        pointsBox.setVisible(false);
        pointsBox.setManaged(false);
        toggleLabels(false);
        labelsField.clear();
        childList.getChildren().setAll(createEmptyRow());
        currentParent = null;
    }

    private String defaultText(String value, String fallback){
        return value == null ? fallback : value;
    }

    private void displayChapter(Chapter chapter){
        headerLabel.setText(defaultText(chapter.getTitle(), localizationService.get("parentEditor.header.chapter")));
        titleField.setText(defaultText(chapter.getTitle(), ""));
        togglePoints(false);
        toggleLabels(false);
        childSectionLabel.setText(localizationService.get("parentEditor.childSection.subtasks"));
        renderChildren(chapter.getChildElements());
    }

    private void displaySubtask(Subtask subtask){
        headerLabel.setText(defaultText(subtask.getTitle(), "Subtask"));
        headerLabel.setText(defaultText(subtask.getTitle(), localizationService.get("parentEditor.header.subtask")));
        titleField.setText(defaultText(subtask.getTitle(), ""));
        togglePoints(true);
        toggleLabels(true);
        pointsField.setText(String.valueOf(subtask.getPoints()));
        labelsField.setText(String.join(", ", defaultLabels(subtask.getLabels())));
        childSectionLabel.setText(localizationService.get("parentEditor.childSection.variants"));
        renderChildren(subtask.getChildElements());
    }

    private void displayGeneric(ParentObject<? extends ChildObject> parent){
        headerLabel.setText(defaultText(parent.getTitle(), localizationService.get("parentEditor.header.generic")));
        titleField.setText(defaultText(parent.getTitle(), ""));
        togglePoints(false);
        childSectionLabel.setText(localizationService.get("parentEditor.childSection.generic"));
        renderChildren(parent.getChildElements());
    }

    private void togglePoints(boolean visible){
        pointsBox.setVisible(visible);
        pointsBox.setManaged(visible);
    }

    private void toggleLabels(boolean visible){
        labelsBox.setVisible(visible);
        labelsBox.setManaged(visible);
    }

    private List<String> parseLabels(String input){
        if(input == null || input.isBlank()){
            return List.of();
        }

        return List.of(input.split(","))
                .stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private List<String> defaultLabels(List<String> labels){
        return labels == null ? List.of() : labels;
    }

    private void applyTranslations() {
        titleLabel.setText(localizationService.get("parentEditor.title"));
        titleField.setPromptText(localizationService.get("parentEditor.title.prompt"));
        pointsLabel.setText(localizationService.get("parentEditor.points"));
        pointsField.setPromptText(localizationService.get("parentEditor.points.prompt"));
        labelsLabel.setText(localizationService.get("parentEditor.labels"));
        labelsField.setPromptText(localizationService.get("parentEditor.labels.prompt"));
        deleteButton.setText(localizationService.get("editor.delete"));
        saveButton.setText(localizationService.get("editor.save"));

        if (currentParent == null) {
            displayPlaceholder();
        } else if (currentParent instanceof Chapter) {
            displayChapter((Chapter) currentParent);
        } else if (currentParent instanceof Subtask) {
            displaySubtask((Subtask) currentParent);
        } else {
            displayGeneric(currentParent);
        }
    }
}

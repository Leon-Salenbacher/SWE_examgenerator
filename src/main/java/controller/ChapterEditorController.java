package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import objects.Chapter;
import objects.Subtask;
import org.w3c.dom.html.HTMLTableRowElement;

import java.util.List;

public class ChapterEditorController {

    @FXML
    private Label headerLabel;

    @FXML
    private TextField titleField;

    @FXML
    private VBox subtasksBox;

    @FXML
    private void initialize(){
        displayNoSelectionState();
    }

    public void displayChapter(Chapter chapter){
        if(chapter == null){
            displayNoSelectionState();
            return;
        }

        headerLabel.setText((defaultString(chapter.getTitle(), "")));
        titleField.setText(defaultString(chapter.getTitle(), ""));
        renderSubtasks(chapter.getSubtasks());
    }

    private void renderSubtasks(List<Subtask> subtasks){
        subtasksBox.getChildren().clear();

        if(subtasks == null || subtasks.isEmpty()){
            Label emptyLabel = new Label("No Subtasks");
            emptyLabel.getStyleClass().add("subtask-item");
            subtasksBox.getChildren().add(emptyLabel);
            return;
        }

        for(Subtask subtask: subtasks){
            subtasksBox.getChildren().add(createSubtaskRow(subtask));
        }
    }

    private HBox createSubtaskRow(Subtask subtask){
        Label label = new Label(buildSubtaskLabel(subtask));
        label.getStyleClass().add("subtask-item");

        HBox row = new HBox(label);
        row.setSpacing(8.0);
        return row;
    }

    private String buildSubtaskLabel(Subtask subtask){
        String title = defaultString(subtask != null ? subtask.getTitle() : null, "Unbenannter Subtask");
        int variantCount = subtask != null && subtask.getVariants()!= null ? subtask.getVariants().size() : 0;;
        return variantCount > 0
                ? String.format("%s (%d Varianten)", title, variantCount)
                : title;
    }

    private void displayNoSelectionState(){
        headerLabel.setText("Select  a Chapter");
        titleField.clear();
        subtasksBox.getChildren().clear();

        Label placeholder = new Label("Bitte wähle ein Kapitel aus der Sidebar aus.");
        placeholder.getStyleClass().add("subtask-item");
        subtasksBox.getChildren().add(placeholder);
    }

    private String defaultString(String value, String fallback){
        return value == null ? fallback : value;
    }
}

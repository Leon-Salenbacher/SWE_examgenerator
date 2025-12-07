package controller.editor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import objects.ChildObject;
import objects.Variant;


public class ChildEditorController {

    @FXML
    private Label headerLabel;

    @FXML
    private TextArea questionField;

    @FXML
    private TextArea solutionField;

    private Variant currentVariant;

    public void displayChild(ChildObject child){
        if(child == null){
            displayPlaceholder();
            return;
        }

        if(child instanceof Variant){
            displayVariant((Variant) child);
            return;
        }

        headerLabel.setText(child.getTitle());
        questionField.clear();
        solutionField.clear();
    }

    private void displayVariant(Variant variant){
        this.currentVariant = variant;
        headerLabel.setText(defaultText(variant.getQuestion(), "Variante " + variant.getId()));
        questionField.setText(variant.getQuestion());
        solutionField.setText(variant.getSolution());
    }

    private void displayPlaceholder(){
        headerLabel.setText("Bitte Element wählen");
        questionField.clear();
        solutionField.clear();
        currentVariant = null;
    }

    @FXML
    private void intialize(){
        questionField.textProperty().addListener((observable, oldValue, newValue ) ->{
            if(currentVariant != null){
                currentVariant.setQuestion(newValue);
                headerLabel.setText(defaultText(newValue, "Variante " + currentVariant.getId()));
            }
        });

        solutionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if(currentVariant != null){
                currentVariant.setSolution(newValue);
            }
        });
    }

    private String defaultText(String value, String fallback){
        return value == null || value.isBlank() ? fallback : value;
    }

}

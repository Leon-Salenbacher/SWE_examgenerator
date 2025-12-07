package controller.editor;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import objects.ChildObject;
import objects.Variant;
import service.LocalizationService;


public class ChildEditorController {

    @FXML
    private Label headerLabel;

    @FXML
    private Label questionLabel;

    @FXML
    private TextArea questionField;

    @FXML
    private Label solutionLabel;

    @FXML
    private TextArea solutionField;

    @FXML
    private Button deleteButton;

    @FXML
    private Button saveButton;

    private Variant currentVariant;
    private final LocalizationService localizationService = LocalizationService.getInstance();

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
        headerLabel.setText(defaultText(variant.getQuestion(), localizationService.get("childEditor.header.variant", variant.getId())));
        questionField.setText(variant.getQuestion());
        solutionField.setText(variant.getSolution());
    }

    private void displayPlaceholder(){
        headerLabel.setText(localizationService.get("editor.placeholder"));
        questionField.clear();
        solutionField.clear();
        currentVariant = null;
    }

    @FXML
    private void initialize(){
        localizationService.localeProperty().addListener((obs, oldLocale, newLocale) -> applyTranslations());
        applyTranslations();

        questionField.textProperty().addListener((observable, oldValue, newValue ) ->{
            if(currentVariant != null){
                currentVariant.setQuestion(newValue);
                headerLabel.setText(defaultText(newValue, localizationService.get("childEditor.header.variant", currentVariant.getId())));
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

    private void applyTranslations() {
        questionLabel.setText(localizationService.get("childEditor.question"));
        questionField.setPromptText(localizationService.get("childEditor.question.prompt"));
        solutionLabel.setText(localizationService.get("childEditor.solution"));
        solutionField.setPromptText(localizationService.get("childEditor.solution.prompt"));
        deleteButton.setText(localizationService.get("editor.delete"));
        saveButton.setText(localizationService.get("editor.save"));

        if (currentVariant == null) {
            headerLabel.setText(localizationService.get("editor.placeholder"));
        } else {
            headerLabel.setText(defaultText(currentVariant.getQuestion(),
                    localizationService.get("childEditor.header.variant", currentVariant.getId())));
        }
    }
}

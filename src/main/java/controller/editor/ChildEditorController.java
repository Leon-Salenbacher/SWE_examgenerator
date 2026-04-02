package controller.editor;

import config.ApplicationContext;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import objects.ChildObject;
import objects.Variant;
import service.impl.LocalizationService;
import service.impl.elements.VariantServiceImpl;

public class ChildEditorController {

    @FXML
    private Label headerLabel;

    @FXML
    private Label questionLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private TextField titleField;

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

    @FXML
    private Label actionFeedbackLabel;

    private Variant currentVariant;
    private Runnable dataChangedHandler;
    private final LocalizationService localizationService = LocalizationService.getInstance();
    private final VariantServiceImpl variantService = ApplicationContext.getInstance().getVariantService();
    private static final String FEEDBACK_SUCCESS_STYLE = "feedback-success";
    private static final String FEEDBACK_ERROR_STYLE = "feedback-error";

    public void setDataChangedHandler(Runnable dataChangedHandler) {
        this.dataChangedHandler = dataChangedHandler;
    }

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
        titleField.clear();
        questionField.clear();
        solutionField.clear();
        clearFeedback();
    }

    private void displayVariant(Variant variant){
        this.currentVariant = variant;
        headerLabel.setText(defaultText(variant.getQuestion(), localizationService.get("childEditor.header.variant", variant.getId())));
        titleField.setText(variant.getTitle());
        questionField.setText(variant.getQuestion());
        solutionField.setText(variant.getSolution());
        clearFeedback();
    }

    private void displayPlaceholder(){
        headerLabel.setText(localizationService.get("editor.placeholder"));
        titleField.clear();
        questionField.clear();
        solutionField.clear();
        currentVariant = null;
        clearFeedback();
    }

    @FXML
    private void initialize(){
        localizationService.localeProperty().addListener((obs, oldLocale, newLocale) -> applyTranslations());
        applyTranslations();

        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearFeedback();
            if(currentVariant != null){
                currentVariant.setTitle(newValue);
            }
        });

        questionField.textProperty().addListener((observable, oldValue, newValue ) ->{
            clearFeedback();
            if(currentVariant != null){
                currentVariant.setQuestion(newValue);
                headerLabel.setText(defaultText(newValue, localizationService.get("childEditor.header.variant", currentVariant.getId())));
            }
        });

        solutionField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearFeedback();
            if(currentVariant != null){
                currentVariant.setSolution(newValue);
            }
        });
    }

    private String defaultText(String value, String fallback){
        return value == null || value.isBlank() ? fallback : value;
    }

    private void applyTranslations() {
        titleLabel.setText(localizationService.get("childEditor.title"));
        titleField.setPromptText(localizationService.get("childEditor.title.prompt"));
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

    @FXML
    private void handleSave(){
        if(currentVariant == null){
            return;
        }

        try {
            VariantServiceImpl.VariantCommand command = new VariantServiceImpl.VariantCommand() {
                @Override
                public String title() {
                    return titleField.getText();
                }

                @Override
                public String question() {
                    return questionField.getText();
                }

                @Override
                public String solution() {
                    return solutionField.getText();
                }

                @Override
                public Integer parentId() {
                    return null;
                }
            };

            currentVariant = variantService.update(currentVariant.getId(), command);
            headerLabel.setText(defaultText(currentVariant.getQuestion(), localizationService.get("childEditor.header.variant", currentVariant.getId())));
            showSuccessFeedback(localizationService.get("editor.save.success"));
            if (dataChangedHandler != null) {
                dataChangedHandler.run();
            }
        } catch (Exception exception) {
            showErrorFeedback(localizationService.get("editor.save.failed", messageOrFallback(exception)));
        }
    }

    private void showSuccessFeedback(String message) {
        actionFeedbackLabel.getStyleClass().removeAll(FEEDBACK_SUCCESS_STYLE, FEEDBACK_ERROR_STYLE);
        actionFeedbackLabel.getStyleClass().add(FEEDBACK_SUCCESS_STYLE);
        actionFeedbackLabel.setText(message);
    }

    private void showErrorFeedback(String message) {
        actionFeedbackLabel.getStyleClass().removeAll(FEEDBACK_SUCCESS_STYLE, FEEDBACK_ERROR_STYLE);
        actionFeedbackLabel.getStyleClass().add(FEEDBACK_ERROR_STYLE);
        actionFeedbackLabel.setText(message);
    }

    private void clearFeedback() {
        actionFeedbackLabel.setText("");
        actionFeedbackLabel.getStyleClass().removeAll(FEEDBACK_SUCCESS_STYLE, FEEDBACK_ERROR_STYLE);
    }

    private String messageOrFallback(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? localizationService.get("editor.error.unknown")
                : message;
    }
}

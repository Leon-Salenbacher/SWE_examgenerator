package controller.editor;

import config.ApplicationContext;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import models.ChildObject;
import models.Subtask;
import models.Variant;
import service.impl.LocalizationService;
import service.impl.elements.SubtaskServiceImpl;
import service.impl.elements.VariantServiceImpl;
import validation.elements.ValidationResult;
import validation.elements.VariantValidator;
import javafx.util.Duration;

public class ChildEditorController {

    @FXML
    private Label typeLabel;

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
    private java.util.function.Consumer<ChildObject> displayHandler;
    private java.util.function.Consumer<EditorFeedbackRequest> feedbackHandler;
    private java.util.function.Consumer<ChildObject> navigationHandler;
    private final LocalizationService localizationService = LocalizationService.getInstance();
    private final VariantServiceImpl variantService = ApplicationContext.getInstance().getVariantService();
    private final SubtaskServiceImpl subtaskService = ApplicationContext.getInstance().getSubtaskService();
    private final VariantValidator variantValidator = new VariantValidator();
    private static final String FEEDBACK_SUCCESS_STYLE = "feedback-success";
    private static final String FEEDBACK_ERROR_STYLE = "feedback-error";
    private PauseTransition feedbackHideTransition;

    public void setDataChangedHandler(Runnable dataChangedHandler) {
        this.dataChangedHandler = dataChangedHandler;
    }

    public void setDisplayHandler(java.util.function.Consumer<ChildObject> displayHandler) {
        this.displayHandler = displayHandler;
    }

    public void setFeedbackHandler(java.util.function.Consumer<EditorFeedbackRequest> feedbackHandler) {
        this.feedbackHandler = feedbackHandler;
    }

    public void setNavigationHandler(java.util.function.Consumer<ChildObject> navigationHandler) {
        this.navigationHandler = navigationHandler;
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

        titleField.clear();
        questionField.clear();
        solutionField.clear();
        clearFeedback();
    }

    private void displayVariant(Variant variant){
        this.currentVariant = variant;
        typeLabel.setText("Variant");
        titleField.setText(variant.getTitle());
        questionField.setText(variant.getQuestion());
        solutionField.setText(variant.getSolution());
        clearFeedback();
    }

    private void displayPlaceholder(){
        typeLabel.setText("Editor");
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
        typeLabel.setText(currentVariant == null ? "Editor" : "Variant");
        titleLabel.setText(localizationService.get("childEditor.title"));
        titleField.setPromptText(localizationService.get("childEditor.title.prompt"));
        questionLabel.setText(localizationService.get("childEditor.question"));
        questionField.setPromptText(localizationService.get("childEditor.question.prompt"));
        solutionLabel.setText(localizationService.get("childEditor.solution"));
        solutionField.setPromptText(localizationService.get("childEditor.solution.prompt"));
        deleteButton.setText(localizationService.get("editor.delete"));
        saveButton.setText(localizationService.get("editor.save"));

    }

    @FXML
    private void handleSave(){
        if(currentVariant == null){
            return;
        }

        try {
            ValidationResult validationResult = variantValidator.validate(currentVariant);
            if (!validationResult.isValid()) {
                showErrorFeedback(validationResult.message());
                return;
            }

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
            if (navigationHandler != null) {
                navigationHandler.accept(currentVariant);
            } else if (dataChangedHandler != null) {
                dataChangedHandler.run();
            }
            if (feedbackHandler != null) {
                feedbackHandler.accept(new EditorFeedbackRequest(currentVariant, localizationService.get("editor.save.success"), true));
            } else {
                showSuccessFeedback(localizationService.get("editor.save.success"));
            }
        } catch (Exception exception) {
            showErrorFeedback(localizationService.get("editor.save.failed", messageOrFallback(exception)));
        }
    }

    @FXML
    private void handleDelete() {
        if (currentVariant == null || !confirmDelete()) {
            return;
        }

        try {
            Subtask parentSubtask = findParentSubtask(currentVariant);
            variantService.delete(currentVariant.getId());
            currentVariant = null;

            if (parentSubtask == null) {
                notifyDataChanged();
                displayPlaceholder();
                showSuccessFeedback(localizationService.get("editor.delete.success"));
                return;
            }

            Subtask refreshedParent = subtaskService.getById(parentSubtask.getId());
            if (navigationHandler != null) {
                navigationHandler.accept(refreshedParent);
            } else {
                notifyDataChanged();
                if (displayHandler != null) {
                    displayHandler.accept(refreshedParent);
                } else {
                    displayPlaceholder();
                }
            }

            if (feedbackHandler != null) {
                feedbackHandler.accept(new EditorFeedbackRequest(refreshedParent, localizationService.get("editor.delete.success"), true));
            } else {
                showSuccessFeedback(localizationService.get("editor.delete.success"));
            }
        } catch (Exception exception) {
            showErrorFeedback(localizationService.get("editor.delete.failed", messageOrFallback(exception)));
        }
    }

    private void notifyDataChanged() {
        if (dataChangedHandler != null) {
            dataChangedHandler.run();
        }
    }

    private Subtask findParentSubtask(Variant variant) {
        if (variant == null) {
            return null;
        }

        return subtaskService.getAll().stream()
                .filter(subtask -> subtask.getChildElements().stream()
                        .anyMatch(child -> child.getId() == variant.getId()))
                .findFirst()
                .orElse(null);
    }

    private boolean confirmDelete() {
        ButtonType deleteType = new ButtonType(
                localizationService.get("editor.delete.confirm.action"),
                ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelType = new ButtonType(
                localizationService.get("editor.delete.confirm.cancel"),
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(localizationService.get("editor.delete.confirm.title"));
        alert.setHeaderText(localizationService.get("editor.delete.confirm.header"));
        alert.setContentText(localizationService.get("editor.delete.confirm.content"));
        alert.getButtonTypes().setAll(deleteType, cancelType);

        return alert.showAndWait()
                .filter(deleteType::equals)
                .isPresent();
    }

    private void showSuccessFeedback(String message) {
        showFeedback(message, FEEDBACK_SUCCESS_STYLE);
    }

    private void showErrorFeedback(String message) {
        showFeedback(message, FEEDBACK_ERROR_STYLE);
    }

    private void showFeedback(String message, String styleClass) {
        if (feedbackHideTransition != null) {
            feedbackHideTransition.stop();
        }
        actionFeedbackLabel.getStyleClass().removeAll(FEEDBACK_SUCCESS_STYLE, FEEDBACK_ERROR_STYLE);
        actionFeedbackLabel.getStyleClass().add(styleClass);
        actionFeedbackLabel.setText(message);
        actionFeedbackLabel.setVisible(true);
        actionFeedbackLabel.setManaged(true);
        actionFeedbackLabel.setOpacity(1);
        actionFeedbackLabel.setScaleX(1);
        actionFeedbackLabel.setScaleY(1);

        ScaleTransition pulse = new ScaleTransition(Duration.millis(180), actionFeedbackLabel);
        pulse.setFromX(0.96);
        pulse.setFromY(0.96);
        pulse.setToX(1.0);
        pulse.setToY(1.0);
        pulse.play();

        feedbackHideTransition = new PauseTransition(Duration.seconds(5));
        feedbackHideTransition.setOnFinished(event -> {
            FadeTransition fade = new FadeTransition(Duration.millis(260), actionFeedbackLabel);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(finished -> clearFeedback());
            fade.play();
        });
        feedbackHideTransition.play();
    }

    public void showTransientFeedback(String message, boolean success) {
        showFeedback(message, success ? FEEDBACK_SUCCESS_STYLE : FEEDBACK_ERROR_STYLE);
    }

    private void clearFeedback() {
        if (feedbackHideTransition != null) {
            feedbackHideTransition.stop();
        }
        actionFeedbackLabel.setText("");
        actionFeedbackLabel.getStyleClass().removeAll(FEEDBACK_SUCCESS_STYLE, FEEDBACK_ERROR_STYLE);
        actionFeedbackLabel.setVisible(false);
        actionFeedbackLabel.setManaged(false);
        actionFeedbackLabel.setOpacity(1);
    }

    private String messageOrFallback(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? localizationService.get("editor.error.unknown")
                : message;
    }
}

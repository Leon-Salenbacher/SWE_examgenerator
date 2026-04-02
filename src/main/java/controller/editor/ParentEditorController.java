package controller.editor;

import config.ApplicationContext;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import objects.Chapter;
import objects.ChildObject;
import objects.ParentObject;
import objects.Subtask;
import objects.Variant;
import service.impl.LocalizationService;
import service.impl.elements.ChapterServiceImpl;
import service.impl.elements.SubtaskServiceImpl;
import validation.ChapterValidator;
import validation.SubtaskValidator;
import validation.ValidationResult;
import validation.VariantValidator;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ParentEditorController {
    @FXML
    private Label typeLabel;

    @FXML
    private TextField titleField;

    @FXML
    private VBox pointsBox;

    @FXML
    private TextField pointsField;

    @FXML
    private Label childSectionLabel;

    @FXML
    private Label actionFeedbackLabel;

    @FXML
    private VBox labelsBox;

    @FXML
    private Label labelsLabel;

    @FXML
    private TextField labelsField;

    @FXML
    private Label titleLabel;

    @FXML
    private VBox questionBox;

    @FXML
    private Label questionLabel;

    @FXML
    private TextArea questionField;

    @FXML
    private VBox solutionBox;

    @FXML
    private Label solutionLabel;

    @FXML
    private TextArea solutionField;

    @FXML
    private Label pointsLabel;

    @FXML
    private Button deleteButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button createButton;

    @FXML
    private Button addChild;

    @FXML
    private VBox childList;

    private ParentObject<? extends ChildObject> currentParent;
    private Consumer<ChildObject> selectionHandler;
    private Consumer<ChildObject> displayHandler;
    private Consumer<EditorFeedbackRequest> feedbackHandler;
    private Consumer<ChildObject> navigationHandler;
    private Runnable dataChangedHandler;
    private boolean createMode;
    private final LocalizationService localizationService = LocalizationService.getInstance();
    private final ChapterServiceImpl chapterService = ApplicationContext.getInstance().getChapterService();
    private final SubtaskServiceImpl subtaskService = ApplicationContext.getInstance().getSubtaskService();
    private final ChapterValidator chapterValidator = new ChapterValidator();
    private final SubtaskValidator subtaskValidator = new SubtaskValidator();
    private final VariantValidator variantValidator = new VariantValidator();
    private static final String FEEDBACK_SUCCESS_STYLE = "feedback-success";
    private static final String FEEDBACK_ERROR_STYLE = "feedback-error";
    private PauseTransition feedbackHideTransition;

    @FXML
    private void initialize(){
        pointsField.setTextFormatter(createNumericFormatter());

        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearFeedback();
            if (createMode) {
                return;
            }
            if(currentParent instanceof Chapter){
                ((Chapter) currentParent).setTitle(newValue);
            }else if(currentParent instanceof Subtask){
                ((Subtask) currentParent).setTitle(newValue);
            }
        });

        pointsField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearFeedback();
            if (createMode) {
                return;
            }
            if(!(currentParent instanceof Subtask)){
                return;
            }

            try{
                int parsed = newValue == null || newValue.isBlank() ? 0 : Integer.parseInt(newValue);
                ((Subtask) currentParent).setPoints(parsed);
            }catch(NumberFormatException ignored){
                pointsField.setText(oldValue);
            }
        });

        labelsField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearFeedback();
            if (createMode) {
                return;
            }
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

    public void setDisplayHandler(Consumer<ChildObject> displayHandler) {
        this.displayHandler = displayHandler;
    }

    public void setFeedbackHandler(Consumer<EditorFeedbackRequest> feedbackHandler) {
        this.feedbackHandler = feedbackHandler;
    }

    public void setNavigationHandler(Consumer<ChildObject> navigationHandler) {
        this.navigationHandler = navigationHandler;
    }

    public void setDataChangedHandler(Runnable dataChangedHandler) {
        this.dataChangedHandler = dataChangedHandler;
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
        createMode = false;
        typeLabel.setText("Editor");
        titleField.clear();
        pointsField.clear();
        pointsBox.setVisible(false);
        pointsBox.setManaged(false);
        toggleLabels(false);
        toggleVariantFields(false);
        labelsField.clear();
        questionField.clear();
        solutionField.clear();
        childList.getChildren().setAll(createEmptyRow());
        currentParent = null;
        clearFeedback();
        updateActionButtons();
    }

    private String defaultText(String value, String fallback){
        return value == null || value.isBlank() ? fallback : value;
    }

    private void displayChapter(Chapter chapter){
        createMode = false;
        typeLabel.setText(localizationService.get("parentEditor.header.chapter"));
        titleField.setText(defaultText(chapter.getTitle(), ""));
        togglePoints(false);
        toggleLabels(false);
        toggleVariantFields(false);
        childSectionLabel.setText(localizationService.get("parentEditor.childSection.subtasks"));
        renderChildren(chapter.getChildElements());
        clearFeedback();
        updateActionButtons();
    }

    private void displaySubtask(Subtask subtask){
        createMode = false;
        typeLabel.setText(localizationService.get("parentEditor.header.subtask"));
        titleField.setText(defaultText(subtask.getTitle(), ""));
        togglePoints(true);
        toggleLabels(true);
        toggleVariantFields(false);
        pointsField.setText(String.valueOf(subtask.getPoints()));
        labelsField.setText(String.join(", ", defaultLabels(subtask.getLabels())));
        childSectionLabel.setText(localizationService.get("parentEditor.childSection.variants"));
        renderChildren(subtask.getChildElements());
        clearFeedback();
        updateActionButtons();
    }

    private void displayGeneric(ParentObject<? extends ChildObject> parent){
        createMode = false;
        typeLabel.setText(localizationService.get("parentEditor.header.generic"));
        titleField.setText(defaultText(parent.getTitle(), ""));
        togglePoints(false);
        toggleLabels(false);
        toggleVariantFields(false);
        childSectionLabel.setText(localizationService.get("parentEditor.childSection.generic"));
        renderChildren(parent.getChildElements());
        clearFeedback();
        updateActionButtons();
    }

    private void togglePoints(boolean visible){
        pointsBox.setVisible(visible);
        pointsBox.setManaged(visible);
    }

    private void toggleLabels(boolean visible){
        labelsBox.setVisible(visible);
        labelsBox.setManaged(visible);
    }

    private void toggleVariantFields(boolean visible) {
        questionBox.setVisible(visible);
        questionBox.setManaged(visible);
        solutionBox.setVisible(visible);
        solutionBox.setManaged(visible);
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
        questionLabel.setText(localizationService.get("childEditor.question"));
        questionField.setPromptText(localizationService.get("childEditor.question.prompt"));
        solutionLabel.setText(localizationService.get("childEditor.solution"));
        solutionField.setPromptText(localizationService.get("childEditor.solution.prompt"));
        deleteButton.setText(localizationService.get("editor.delete"));
        saveButton.setText(localizationService.get("editor.save"));
        addChild.setText(localizationService.get("editor.createChild"));
        createButton.setText(localizationService.get("editor.create"));

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

    private void updateActionButtons() {
        boolean hasParent = currentParent != null;
        boolean showChildActions = hasParent && !createMode;
        addChild.setVisible(showChildActions);
        addChild.setManaged(showChildActions);
        childSectionLabel.setVisible(showChildActions);
        childSectionLabel.setManaged(showChildActions);
        childList.setVisible(showChildActions);
        childList.setManaged(showChildActions);

        createButton.setVisible(createMode);
        createButton.setManaged(createMode);
        saveButton.setVisible(!createMode);
        saveButton.setManaged(!createMode);
        deleteButton.setVisible(!createMode);
        deleteButton.setManaged(!createMode);
    }

    private void notifyDataChanged() {
        if (dataChangedHandler != null) {
            dataChangedHandler.run();
        }
    }

    private int nextSubtaskId() {
        return subtaskService.getAll().stream()
                .mapToInt(Subtask::getId)
                .max()
                .orElse(0) + 1;
    }

    private int nextVariantId() {
        return ApplicationContext.getInstance().getVariantService().getAll().stream()
                .mapToInt(Variant::getId)
                .max()
                .orElse(0) + 1;
    }

    private int parsePointsInput() {
        String value = pointsField.getText();
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    public void toggleAddNewChild(){
        if (currentParent == null) {
            return;
        }

        createMode = true;
        titleField.clear();
        pointsField.clear();
        labelsField.clear();
        questionField.clear();
        solutionField.clear();
        clearFeedback();

        if (currentParent instanceof Chapter) {
            typeLabel.setText(localizationService.get("parentEditor.header.createSubtask"));
            togglePoints(true);
            toggleLabels(true);
            toggleVariantFields(false);
        } else if (currentParent instanceof Subtask) {
            typeLabel.setText(localizationService.get("parentEditor.header.createVariant"));
            togglePoints(false);
            toggleLabels(false);
            toggleVariantFields(true);
        } else {
            typeLabel.setText(localizationService.get("parentEditor.header.generic"));
            togglePoints(false);
            toggleLabels(false);
            toggleVariantFields(false);
        }

        updateActionButtons();
    }

    @FXML
    private void handleSave(){
        if(currentParent == null){
            return;
        }

        try {
            if(currentParent instanceof Chapter chapter){
                ValidationResult validationResult = chapterValidator.validate(chapter);
                if (!validationResult.isValid()) {
                    showErrorFeedback(validationResult.message());
                    return;
                }

                ChapterServiceImpl.ChapterCommand command = new ChapterServiceImpl.ChapterCommand() {
                    @Override
                    public String title() {
                        return titleField.getText();
                    }

                    @Override
                    public Integer parentId() {
                        return null;
                    }
                };
                currentParent = chapterService.update(chapter.getId(), command);
                displayParent(currentParent);
                if (navigationHandler != null) {
                    navigationHandler.accept(currentParent);
                } else {
                    notifyDataChanged();
                }
                if (feedbackHandler != null) {
                    feedbackHandler.accept(new EditorFeedbackRequest(currentParent, localizationService.get("editor.save.success"), true));
                } else {
                    showSuccessFeedback(localizationService.get("editor.save.success"));
                }
                return;
            }

            if(currentParent instanceof Subtask subtask){
                ValidationResult validationResult = subtaskValidator.validate(subtask);
                if (!validationResult.isValid()) {
                    showErrorFeedback(validationResult.message());
                    return;
                }

                SubtaskServiceImpl.SubtaskCommand command = new SubtaskServiceImpl.SubtaskCommand() {
                    @Override
                    public String title() {
                        return titleField.getText();
                    }

                    @Override
                    public int points() {
                        String value = pointsField.getText();
                        return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
                    }

                    @Override
                    public List<String> labels() {
                        return parseLabels(labelsField.getText());
                    }

                    @Override
                    public Integer parentId() {
                        return subtask.getChapterId();
                    }
                };

                currentParent = subtaskService.update(subtask.getId(), command);
                displayParent(currentParent);
                if (navigationHandler != null) {
                    navigationHandler.accept(currentParent);
                } else {
                    notifyDataChanged();
                }
                if (feedbackHandler != null) {
                    feedbackHandler.accept(new EditorFeedbackRequest(currentParent, localizationService.get("editor.save.success"), true));
                } else {
                    showSuccessFeedback(localizationService.get("editor.save.success"));
                }
            }
        } catch (Exception exception) {
            showErrorFeedback(localizationService.get("editor.save.failed", messageOrFallback(exception)));
        }
    }

    @FXML
    private void handleCreate() {
        if (currentParent == null || !createMode) {
            return;
        }

        try {
            if (currentParent instanceof Chapter chapter) {
                Subtask createdSubtask = new Subtask();
                createdSubtask.setId(nextSubtaskId());
                createdSubtask.setChapterId(chapter.getId());
                createdSubtask.setTitle(titleField.getText());
                createdSubtask.setPoints(parsePointsInput());
                createdSubtask.setLabels(new ArrayList<>(parseLabels(labelsField.getText())));

                ValidationResult validationResult = subtaskValidator.validate(createdSubtask);
                if (!validationResult.isValid()) {
                    showErrorFeedback(validationResult.message());
                    return;
                }

                Chapter updatedChapter = chapterService.getById(chapter.getId());
                List<Subtask> children = new ArrayList<>(updatedChapter.getChildElements());
                children.add(createdSubtask);
                updatedChapter.setChildElements(children);
                ApplicationContext.getInstance().getChapterRepository().update(updatedChapter);

                if (navigationHandler != null) {
                    navigationHandler.accept(createdSubtask);
                }
                if (feedbackHandler != null) {
                    feedbackHandler.accept(new EditorFeedbackRequest(createdSubtask, localizationService.get("editor.create.success"), true));
                    return;
                }

                showSuccessFeedback(localizationService.get("editor.create.success"));
                notifyDataChanged();
                if (selectionHandler != null) {
                    selectionHandler.accept(createdSubtask);
                } else {
                    displayParent(updatedChapter);
                }
                return;
            }

            if (currentParent instanceof Subtask subtask) {
                Variant createdVariant = new Variant();
                createdVariant.setId(nextVariantId());
                createdVariant.setTitle(titleField.getText());
                createdVariant.setQuestion(questionField.getText());
                createdVariant.setSolution(solutionField.getText());

                ValidationResult validationResult = variantValidator.validate(createdVariant);
                if (!validationResult.isValid()) {
                    showErrorFeedback(validationResult.message());
                    return;
                }

                Subtask updatedSubtask = subtaskService.getById(subtask.getId());
                List<Variant> children = new ArrayList<>(updatedSubtask.getChildElements());
                children.add(createdVariant);
                updatedSubtask.setChildElements(children);
                ApplicationContext.getInstance().getSubtaskRepository().update(updatedSubtask);

                if (navigationHandler != null) {
                    navigationHandler.accept(createdVariant);
                }
                if (feedbackHandler != null) {
                    feedbackHandler.accept(new EditorFeedbackRequest(createdVariant, localizationService.get("editor.create.success"), true));
                    return;
                }

                showSuccessFeedback(localizationService.get("editor.create.success"));
                notifyDataChanged();
                if (selectionHandler != null) {
                    selectionHandler.accept(createdVariant);
                } else {
                    displayParent(updatedSubtask);
                }
            }
        } catch (Exception exception) {
            showErrorFeedback(localizationService.get("editor.create.failed", messageOrFallback(exception)));
        }
    }

    private TextFormatter<String> createNumericFormatter() {
        return new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null);
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

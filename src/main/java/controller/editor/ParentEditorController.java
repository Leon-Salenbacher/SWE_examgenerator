package controller.editor;

import config.ApplicationContext;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import models.Chapter;
import models.ChildObject;
import models.ExamType;
import models.ParentObject;
import models.Points;
import models.Subtask;
import models.SubtaskDifficulty;
import models.Variant;
import service.impl.LocalizationService;
import service.impl.elements.ChapterServiceImpl;
import service.impl.elements.SubtaskServiceImpl;
import validation.elements.ChapterValidator;
import validation.elements.SubtaskValidator;
import validation.elements.ValidationResult;
import validation.elements.VariantValidator;
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
    private VBox difficultyBoxContainer;

    @FXML
    private Label difficultyLabel;

    @FXML
    private ComboBox<SubtaskDifficulty> difficultyBox;

    @FXML
    private VBox usageBoxContainer;

    @FXML
    private Label usageLabel;

    @FXML
    private ComboBox<ExamType> usageBox;

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
    private boolean createChapterMode;
    private boolean updatingFields;
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
        pointsField.setTextFormatter(createPointsFormatter());
        difficultyBox.getItems().setAll(SubtaskDifficulty.values());
        difficultyBox.getStyleClass().add("difficulty-combo");
        difficultyBox.setCellFactory(listView -> new DifficultyListCell());
        difficultyBox.setButtonCell(new DifficultyListCell());
        usageBox.getItems().setAll(ExamType.values());
        usageBox.getStyleClass().add("exam-type-combo");
        usageBox.setCellFactory(listView -> new ExamTypeListCell());
        usageBox.setButtonCell(new ExamTypeListCell());
        usageBox.getSelectionModel().select(ExamType.defaultType());

        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearFeedback();
            if (updatingFields) {
                return;
            }
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
            if (updatingFields) {
                return;
            }
            if (!isValidPointsInput(newValue)) {
                showHalfStepPointsError();
                return;
            }
            if (createMode) {
                return;
            }
            if(!(currentParent instanceof Subtask)){
                return;
            }

            double parsed = parsePointsInput(newValue);
            ((Subtask) currentParent).setPoints(parsed);
        });

        labelsField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearFeedback();
            if (updatingFields) {
                return;
            }
            if (createMode) {
                return;
            }
            if (!(currentParent instanceof Subtask)) {
                return;
            }
            Subtask subtask = (Subtask) currentParent;
            subtask.setLabels(parseLabels(newValue));
            setFieldValues(() -> usageBox.setValue(subtask.getExamType()));
        });

        difficultyBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            clearFeedback();
            if (updatingFields) {
                return;
            }
            if (createMode || !(currentParent instanceof Subtask) || newValue == null) {
                return;
            }
            ((Subtask) currentParent).setDifficulty(newValue);
        });

        usageBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            clearFeedback();
            if (updatingFields) {
                return;
            }
            if (createMode || !(currentParent instanceof Subtask) || newValue == null) {
                return;
            }
            Subtask subtask = (Subtask) currentParent;
            subtask.setExamType(newValue);
            setFieldValues(() -> labelsField.setText(String.join(", ", defaultLabels(subtask.getLabels()))));
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

    public void displayCreateChapter() {
        currentParent = null;
        createMode = true;
        createChapterMode = true;
        setFieldValues(() -> {
            typeLabel.setText(localizationService.get("parentEditor.header.createChapter"));
            titleField.clear();
            pointsField.clear();
            difficultyBox.setValue(SubtaskDifficulty.MEDIUM);
            usageBox.setValue(ExamType.defaultType());
            labelsField.clear();
            questionField.clear();
            solutionField.clear();
        });
        togglePoints(false);
        toggleDifficulty(false);
        toggleUsage(false);
        toggleLabels(false);
        toggleVariantFields(false);
        childSectionLabel.setText(localizationService.get("parentEditor.childSection.subtasks"));
        childList.getChildren().clear();
        clearFeedback();
        updateActionButtons();
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
        createChapterMode = false;
        setFieldValues(() -> {
            typeLabel.setText("Editor");
            titleField.clear();
            pointsField.clear();
            usageBox.setValue(ExamType.defaultType());
            labelsField.clear();
            questionField.clear();
            solutionField.clear();
        });
        pointsBox.setVisible(false);
        pointsBox.setManaged(false);
        toggleDifficulty(false);
        toggleUsage(false);
        toggleLabels(false);
        toggleVariantFields(false);
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
        createChapterMode = false;
        setFieldValues(() -> {
            typeLabel.setText(localizationService.get("parentEditor.header.chapter"));
            titleField.setText(defaultText(chapter.getTitle(), ""));
        });
        togglePoints(false);
        toggleDifficulty(false);
        toggleUsage(false);
        toggleLabels(false);
        toggleVariantFields(false);
        childSectionLabel.setText(localizationService.get("parentEditor.childSection.subtasks"));
        renderChildren(chapter.getChildElements());
        clearFeedback();
        updateActionButtons();
    }

    private void displaySubtask(Subtask subtask){
        createMode = false;
        createChapterMode = false;
        setFieldValues(() -> {
            typeLabel.setText(localizationService.get("parentEditor.header.subtask"));
            titleField.setText(defaultText(subtask.getTitle(), ""));
            pointsField.setText(Points.format(subtask.getPoints()));
            difficultyBox.setValue(defaultDifficulty(subtask.getDifficulty()));
            usageBox.setValue(subtask.getExamType());
            labelsField.setText(String.join(", ", defaultLabels(subtask.getLabels())));
        });
        togglePoints(true);
        toggleDifficulty(true);
        toggleUsage(true);
        toggleLabels(true);
        toggleVariantFields(false);
        childSectionLabel.setText(localizationService.get("parentEditor.childSection.variants"));
        renderChildren(subtask.getChildElements());
        clearFeedback();
        updateActionButtons();
    }

    private void displayGeneric(ParentObject<? extends ChildObject> parent){
        createMode = false;
        createChapterMode = false;
        setFieldValues(() -> {
            typeLabel.setText(localizationService.get("parentEditor.header.generic"));
            titleField.setText(defaultText(parent.getTitle(), ""));
        });
        togglePoints(false);
        toggleDifficulty(false);
        toggleUsage(false);
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

    private void toggleDifficulty(boolean visible){
        difficultyBoxContainer.setVisible(visible);
        difficultyBoxContainer.setManaged(visible);
    }

    private void toggleUsage(boolean visible){
        usageBoxContainer.setVisible(visible);
        usageBoxContainer.setManaged(visible);
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

    private List<String> labelsWithSelectedExamType() {
        return ExamType.replaceExamTypeLabel(parseLabels(labelsField.getText()), defaultExamType(usageBox.getValue()));
    }

    private List<String> defaultLabels(List<String> labels){
        return labels == null ? List.of() : labels;
    }

    private SubtaskDifficulty defaultDifficulty(SubtaskDifficulty difficulty) {
        return difficulty == null ? SubtaskDifficulty.MEDIUM : difficulty;
    }

    private ExamType defaultExamType(ExamType examType) {
        return examType == null ? ExamType.defaultType() : examType;
    }

    private void setFieldValues(Runnable update) {
        updatingFields = true;
        try {
            update.run();
        } finally {
            updatingFields = false;
        }
    }

    private void applyTranslations() {
        titleLabel.setText(localizationService.get("parentEditor.title"));
        titleField.setPromptText(localizationService.get("parentEditor.title.prompt"));
        pointsLabel.setText(localizationService.get("parentEditor.points"));
        pointsField.setPromptText(localizationService.get("parentEditor.points.prompt"));
        difficultyLabel.setText(localizationService.get("parentEditor.difficulty"));
        usageLabel.setText(localizationService.get("parentEditor.usage"));
        usageBox.setButtonCell(new ExamTypeListCell());
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

        if (createChapterMode) {
            typeLabel.setText(localizationService.get("parentEditor.header.createChapter"));
            childSectionLabel.setText(localizationService.get("parentEditor.childSection.subtasks"));
            updateActionButtons();
        } else if (currentParent == null) {
            displayPlaceholder();
        } else if (createMode) {
            displayCreateChildForm();
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
        saveButton.setVisible(hasParent && !createMode);
        saveButton.setManaged(hasParent && !createMode);
        deleteButton.setVisible(hasParent && !createMode);
        deleteButton.setManaged(hasParent && !createMode);
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

    private double parsePointsInput() {
        return parsePointsInput(pointsField.getText());
    }

    private double parsePointsInput(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        double points = Points.parse(value);
        if (!Points.isHalfStep(points)) {
            throw new NumberFormatException("Points must use 0.5 steps.");
        }
        return points;
    }

    private Double parsePointsInputOrShowError() {
        if (isValidPointsInput(pointsField.getText())) {
            return parsePointsInput();
        }
        showHalfStepPointsError();
        return null;
    }

    private boolean isValidPointsInput(String value) {
        try {
            parsePointsInput(value);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private void showHalfStepPointsError() {
        showErrorFeedback(localizationService.get("validation.points.halfStep"));
    }

    public void toggleAddNewChild(){
        if (currentParent == null) {
            return;
        }

        createMode = true;
        createChapterMode = false;
        setFieldValues(() -> {
            titleField.clear();
            pointsField.clear();
            difficultyBox.setValue(SubtaskDifficulty.MEDIUM);
            usageBox.setValue(ExamType.defaultType());
            labelsField.clear();
            questionField.clear();
            solutionField.clear();
        });
        clearFeedback();
        displayCreateChildForm();
    }

    private void displayCreateChildForm() {
        if (currentParent instanceof Chapter) {
            typeLabel.setText(localizationService.get("parentEditor.header.createSubtask"));
            togglePoints(true);
            toggleDifficulty(true);
            toggleUsage(true);
            toggleLabels(true);
            toggleVariantFields(false);
        } else if (currentParent instanceof Subtask) {
            typeLabel.setText(localizationService.get("parentEditor.header.createVariant"));
            togglePoints(false);
            toggleDifficulty(false);
            toggleUsage(false);
            toggleLabels(false);
            toggleVariantFields(true);
        } else {
            typeLabel.setText(localizationService.get("parentEditor.header.generic"));
            togglePoints(false);
            toggleDifficulty(false);
            toggleUsage(false);
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
                Double enteredPoints = parsePointsInputOrShowError();
                if (enteredPoints == null) {
                    return;
                }
                subtask.setPoints(enteredPoints);

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
                    public double points() {
                        return enteredPoints;
                    }

                    @Override
                    public SubtaskDifficulty difficulty() {
                        return defaultDifficulty(difficultyBox.getValue());
                    }

                    @Override
                    public List<String> labels() {
                        return labelsWithSelectedExamType();
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
    private void handleDelete() {
        if (currentParent == null || createMode || !confirmDelete()) {
            return;
        }

        try {
            if (currentParent instanceof Chapter chapter) {
                chapterService.delete(chapter.getId());
                notifyDataChanged();
                displayPlaceholder();
                showSuccessFeedback(localizationService.get("editor.delete.success"));
                return;
            }

            if (currentParent instanceof Subtask subtask) {
                int chapterId = subtask.getChapterId();
                subtaskService.delete(subtask.getId());
                notifyDataChanged();

                Chapter parentChapter = chapterService.getById(chapterId);
                currentParent = parentChapter;
                if (navigationHandler != null) {
                    navigationHandler.accept(parentChapter);
                } else {
                    displayParent(parentChapter);
                }

                if (feedbackHandler != null) {
                    feedbackHandler.accept(new EditorFeedbackRequest(parentChapter, localizationService.get("editor.delete.success"), true));
                } else {
                    showSuccessFeedback(localizationService.get("editor.delete.success"));
                }
            }
        } catch (Exception exception) {
            showErrorFeedback(localizationService.get("editor.delete.failed", messageOrFallback(exception)));
        }
    }

    @FXML
    private void handleCreate() {
        if (!createMode) {
            return;
        }

        try {
            if (createChapterMode) {
                Chapter draftChapter = new Chapter();
                draftChapter.setTitle(titleField.getText());

                ValidationResult validationResult = chapterValidator.validate(draftChapter);
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

                Chapter createdChapter = chapterService.create(command);
                currentParent = createdChapter;
                createMode = false;
                createChapterMode = false;
                displayParent(createdChapter);

                if (navigationHandler != null) {
                    navigationHandler.accept(createdChapter);
                } else {
                    notifyDataChanged();
                }
                if (feedbackHandler != null) {
                    feedbackHandler.accept(new EditorFeedbackRequest(createdChapter, localizationService.get("editor.create.success"), true));
                } else {
                    showSuccessFeedback(localizationService.get("editor.create.success"));
                }
                return;
            }

            if (currentParent == null) {
                return;
            }

            if (currentParent instanceof Chapter chapter) {
                Double enteredPoints = parsePointsInputOrShowError();
                if (enteredPoints == null) {
                    return;
                }

                Subtask createdSubtask = new Subtask();
                createdSubtask.setId(nextSubtaskId());
                createdSubtask.setChapterId(chapter.getId());
                createdSubtask.setTitle(titleField.getText());
                createdSubtask.setPoints(enteredPoints);
                createdSubtask.setDifficulty(defaultDifficulty(difficultyBox.getValue()));
                createdSubtask.setLabels(new ArrayList<>(labelsWithSelectedExamType()));

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

    private TextFormatter<String> createPointsFormatter() {
        return new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("(|\\d+([\\.,]\\d*)?)")) {
                return change;
            }
            showHalfStepPointsError();
            return null;
        });
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

    private final class DifficultyListCell extends ListCell<SubtaskDifficulty> {
        @Override
        protected void updateItem(SubtaskDifficulty item, boolean empty) {
            super.updateItem(item, empty);
            getStyleClass().removeAll(
                    "difficulty-cell-easy",
                    "difficulty-cell-medium",
                    "difficulty-cell-hard"
            );

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Region icon = new Region();
            icon.getStyleClass().addAll("difficulty-icon", "difficulty-icon-" + item.getXmlValue());

            Label text = new Label(localizationService.get("difficulty." + item.getXmlValue()));
            text.getStyleClass().add("difficulty-text");

            HBox content = new HBox(8, icon, text);
            content.getStyleClass().add("difficulty-option");
            getStyleClass().add("difficulty-cell-" + item.getXmlValue());

            setText(null);
            setGraphic(content);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }

    private final class ExamTypeListCell extends ListCell<ExamType> {
        @Override
        protected void updateItem(ExamType item, boolean empty) {
            super.updateItem(item, empty);
            getStyleClass().removeAll("exam-type-cell-exam", "exam-type-cell-practice");

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Region icon = new Region();
            icon.getStyleClass().addAll("exam-type-icon", "exam-type-icon-" + item.name().toLowerCase());

            Label text = new Label(localizationService.get(item.getLocalizationKey()));
            text.getStyleClass().add("exam-type-text");

            HBox content = new HBox(8, icon, text);
            content.getStyleClass().add("exam-type-option");
            getStyleClass().add("exam-type-cell-" + item.name().toLowerCase());

            setText(null);
            setGraphic(content);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }
}

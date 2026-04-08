package controller.exam;

import config.ApplicationContext;
import exceptions.ExamGenerationException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Chapter;
import service.exam.ExamGenerationService;
import service.pdf.PdfExamWriter;
import service.exam.dto.GenerateExamValues;
import service.exam.dto.GeneratedExam;
import service.exam.dto.PdfLayoutSettings;
import service.impl.LocalizationService;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;

public class ExamGenerationDialogController {

    @FXML
    private Label chapterLabel;
    @FXML
    private ComboBox<Chapter> availableChapterBox;
    @FXML
    private Button addButton;
    @FXML
    private ListView<Chapter> selectedChapterList;
    @FXML
    private Button removeButton;
    @FXML
    private Button moveUpButton;
    @FXML
    private Button moveDownButton;
    @FXML
    private Label titleLabel;
    @FXML
    private TextField titleField;
    @FXML
    private Label pointsLabel;
    @FXML
    private TextField pointsField;
    @FXML
    private Label layoutLabel;
    @FXML
    private Button layoutButton;
    @FXML
    private Label layoutSummaryLabel;
    @FXML
    private CheckBox includeSolutionsCheckBox;
    @FXML
    private Label statusLabel;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Button cancelButton;
    @FXML
    private Button generateButton;

    private final LocalizationService localizationService = LocalizationService.getInstance();
    private final ExamGenerationService examGenerationService = new ExamGenerationService();
    private final PdfExamWriter pdfExamWriter = new PdfExamWriter();
    private final PdfLayoutDialog pdfLayoutDialog = new PdfLayoutDialog();

    private final ObservableList<Chapter> selectedChapters = FXCollections.observableArrayList();
    private final ObservableList<Chapter> availableChapters = FXCollections.observableArrayList();
    private List<Chapter> allChapters = List.of();
    private Stage dialogStage;
    private PdfLayoutSettings currentLayoutSettings;

    @FXML
    private void initialize() {
        selectedChapterList.setItems(selectedChapters);
        selectedChapterList.setCellFactory(listView -> new ChapterListCell());

        availableChapterBox.setItems(availableChapters);
        availableChapterBox.setCellFactory(listView -> new ChapterListCell());
        availableChapterBox.setButtonCell(new ChapterListCell());

        pointsField.setTextFormatter(new TextFormatter<>(buildNumericFilter()));
        selectedChapterList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> updateSelectionButtons());
        localizationService.localeProperty().addListener((obs, oldLocale, newLocale) -> applyTranslations());
        applyTranslations();
    }

    public void configure(Stage dialogStage) {
        this.dialogStage = dialogStage;
        this.allChapters = ApplicationContext.getInstance().getChapterRepository().findAll();
        this.selectedChapters.setAll(allChapters);
        this.availableChapters.clear();
        this.currentLayoutSettings = PdfLayoutSettings.defaults(localizationService.get("generate.dialog.defaultTitle"));
        titleField.setText(localizationService.get("generate.dialog.defaultTitle"));
        layoutSummaryLabel.setText(currentLayoutSettings.summary());
        statusLabel.setText(localizationService.get("generate.dialog.status.ready"));
        updateSelectionButtons();
        applyTranslations();
    }

    @FXML
    private void handleAddChapter() {
        Chapter selectedChapter = availableChapterBox.getSelectionModel().getSelectedItem();
        if (selectedChapter == null) {
            setStatus(localizationService.get("generate.dialog.error.selectChapterToAdd"), false);
            return;
        }

        availableChapters.remove(selectedChapter);
        selectedChapters.add(selectedChapter);
        availableChapterBox.getSelectionModel().clearSelection();
        selectedChapterList.getSelectionModel().select(selectedChapter);
        setStatus(localizationService.get("generate.dialog.status.ready"), false);
        updateSelectionButtons();
    }

    @FXML
    private void handleRemoveChapter() {
        Chapter selectedChapter = selectedChapterList.getSelectionModel().getSelectedItem();
        if (selectedChapter == null) {
            setStatus(localizationService.get("generate.dialog.error.selectChapterToRemove"), false);
            return;
        }

        selectedChapters.remove(selectedChapter);
        availableChapters.add(selectedChapter);
        availableChapters.sort(Comparator.comparingInt(chapter -> allChapters.indexOf(chapter)));
        setStatus(localizationService.get("generate.dialog.status.ready"), false);
        updateSelectionButtons();
    }

    @FXML
    private void handleMoveUp() {
        moveSelectedChapter(-1);
    }

    @FXML
    private void handleMoveDown() {
        moveSelectedChapter(1);
    }

    @FXML
    private void handleOpenLayoutDialog() {
        currentLayoutSettings = pdfLayoutDialog.show(dialogStage, titleField.getText(), currentLayoutSettings);
        layoutSummaryLabel.setText(currentLayoutSettings.summary());
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    @FXML
    private void handleGenerate() {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        int targetPoints;
        try {
            targetPoints = Integer.parseInt(pointsField.getText());
        } catch (Exception exception) {
            setStatus(localizationService.get("generate.dialog.error.invalidPoints"), true);
            return;
        }

        PdfLayoutSettings layoutSettings = (currentLayoutSettings == null
                ? PdfLayoutSettings.defaults(title)
                : currentLayoutSettings).sanitize(title);
        currentLayoutSettings = layoutSettings;

        setBusy(true);
        setStatus(localizationService.get("generate.dialog.status.checking"), false);

        GenerateExamValues generateExamValues = new GenerateExamValues(title, targetPoints, new ArrayList<>(selectedChapters));
        Task<GeneratedExam> generationTask = new Task<>() {
            @Override
            protected GeneratedExam call() {
                return examGenerationService.generateExam(generateExamValues);
            }
        };

        generationTask.setOnSucceeded(event -> {
            GeneratedExam generatedExam = generationTask.getValue();
            File selectedFile = showSaveDialog(generatedExam.title());
            if (selectedFile == null) {
                setBusy(false);
                setStatus(localizationService.get("generate.dialog.save.cancelled"), false);
                return;
            }
            setStatus(localizationService.get("generate.dialog.status.saving"), false);
            saveGeneratedExam(generatedExam, layoutSettings, selectedFile.toPath(), includeSolutionsCheckBox.isSelected());
        });

        generationTask.setOnFailed(event -> {
            setBusy(false);
            setStatus(mapGenerationError(generationTask.getException()), true);
        });

        Thread generationThread = new Thread(generationTask, "exam-generation-task");
        generationThread.setDaemon(true);
        generationThread.start();
    }

    private void saveGeneratedExam(
            GeneratedExam generatedExam,
            PdfLayoutSettings layoutSettings,
            Path outputPath,
            boolean includeSolutions
    ) {
        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                pdfExamWriter.write(outputPath, generatedExam, layoutSettings, includeSolutions);
                return null;
            }
        };

        saveTask.setOnSucceeded(event -> {
            setBusy(false);
            dialogStage.close();
            showAlert(Alert.AlertType.INFORMATION,
                    localizationService.get("generate.dialog.title"),
                    localizationService.get("generate.dialog.success", outputPath.toAbsolutePath()));
        });

        saveTask.setOnFailed(event -> {
            setBusy(false);
            Throwable error = saveTask.getException();
            String message = error == null || error.getMessage() == null || error.getMessage().isBlank()
                    ? localizationService.get("generate.dialog.save.failed.generic")
                    : localizationService.get("generate.dialog.save.failed", error.getMessage());
            setStatus(message, true);
        });

        Thread saveThread = new Thread(saveTask, "exam-save-task");
        saveThread.setDaemon(true);
        saveThread.start();
    }

    private File showSaveDialog(String examTitle) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(localizationService.get("generate.dialog.save.title"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fileChooser.setInitialFileName(sanitizeFileName(examTitle) + ".pdf");
        return fileChooser.showSaveDialog(dialogStage);
    }

    private void moveSelectedChapter(int direction) {
        int selectedIndex = selectedChapterList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            return;
        }
        int targetIndex = selectedIndex + direction;
        if (targetIndex < 0 || targetIndex >= selectedChapters.size()) {
            return;
        }
        Chapter selectedChapter = selectedChapters.remove(selectedIndex);
        selectedChapters.add(targetIndex, selectedChapter);
        selectedChapterList.getSelectionModel().select(targetIndex);
        updateSelectionButtons();
    }

    private void updateSelectionButtons() {
        int selectedIndex = selectedChapterList.getSelectionModel().getSelectedIndex();
        int selectedCount = selectedChapterList.getItems().size();
        boolean hasSelection = selectedIndex >= 0;
        removeButton.setDisable(!hasSelection);
        moveUpButton.setDisable(!hasSelection || selectedIndex == 0);
        moveDownButton.setDisable(!hasSelection || selectedIndex == selectedCount - 1);
    }

    private void setBusy(boolean busy) {
        progressIndicator.setVisible(busy);
        progressIndicator.setManaged(busy);
        generateButton.setDisable(busy);
        cancelButton.setDisable(busy);
        layoutButton.setDisable(busy);
        includeSolutionsCheckBox.setDisable(busy);
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("generate-dialog-status-success", "generate-dialog-status-error");
        statusLabel.getStyleClass().add(error ? "generate-dialog-status-error" : "generate-dialog-status-success");
    }

    private String mapGenerationError(Throwable error) {
        if (error instanceof ExamGenerationException generationException) {
            return switch (generationException.getReason()) {
                case INVALID_TITLE -> localizationService.get("generate.dialog.error.titleRequired");
                case INVALID_POINTS -> localizationService.get("generate.dialog.error.invalidPoints");
                case EMPTY_SELECTION -> localizationService.get("generate.dialog.error.noSelectedChapters");
                case NO_GENERATABLE_SUBTASKS -> localizationService.get("generate.dialog.error.noGeneratableSubtasks");
                case POINTS_NOT_REACHABLE -> localizationService.get(
                        "generate.dialog.error.pointsNotReachable",
                        generationException.getRequestedPoints(),
                        generationException.getClosestReachablePoints(),
                        generationException.getMaxReachablePoints()
                );
            };
        }
        return localizationService.get("generate.dialog.error.generic");
    }

    private String sanitizeFileName(String value) {
        String sanitized = value == null ? "exam" : value.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return sanitized.isBlank() ? "exam" : sanitized;
    }

    private UnaryOperator<TextFormatter.Change> buildNumericFilter() {
        return change -> change.getControlNewText().matches("\\d*") ? change : null;
    }

    private void applyTranslations() {
        if (dialogStage != null) {
            dialogStage.setTitle(localizationService.get("generate.dialog.title"));
        }
        chapterLabel.setText(localizationService.get("generate.dialog.chapterSelection"));
        availableChapterBox.setPromptText(localizationService.get("generate.dialog.addChapter.prompt"));
        addButton.setText(localizationService.get("generate.dialog.addChapter"));
        removeButton.setText(localizationService.get("generate.dialog.removeChapter"));
        moveUpButton.setText(localizationService.get("generate.dialog.moveUp"));
        moveDownButton.setText(localizationService.get("generate.dialog.moveDown"));
        titleLabel.setText(localizationService.get("generate.dialog.examTitle"));
        titleField.setPromptText(localizationService.get("generate.dialog.examTitle.prompt"));
        pointsLabel.setText(localizationService.get("generate.dialog.points"));
        pointsField.setPromptText(localizationService.get("generate.dialog.points.prompt"));
        layoutLabel.setText(localizationService.get("generate.dialog.layout"));
        layoutButton.setText(localizationService.get("generate.dialog.layoutButton"));
        includeSolutionsCheckBox.setText(localizationService.get("generate.dialog.includeSolutions"));
        cancelButton.setText(localizationService.get("generate.dialog.cancel"));
        generateButton.setText(localizationService.get("generate.dialog.generate"));
        if (titleField.getText() == null || titleField.getText().isBlank()) {
            titleField.setText(localizationService.get("generate.dialog.defaultTitle"));
        }
        if (statusLabel.getText() == null || statusLabel.getText().isBlank()) {
            statusLabel.setText(localizationService.get("generate.dialog.status.ready"));
        }
        if (currentLayoutSettings != null) {
            layoutSummaryLabel.setText(currentLayoutSettings.summary());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType, content, ButtonType.OK);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.showAndWait();
        });
    }

    private static final class ChapterListCell extends ListCell<Chapter> {
        @Override
        protected void updateItem(Chapter item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                String title = item.getTitle() == null || item.getTitle().isBlank()
                        ? "Chapter " + item.getId()
                        : item.getTitle().trim();
                setText(title);
            }
        }
    }
}


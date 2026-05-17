package controller;

import config.ApplicationContext;
import controller.exam.ExamGenerationDialog;
import controller.editor.EditorHostController;
import exceptions.XmlStorageException;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import models.Chapter;
import models.ChildObject;
import models.Subtask;
import models.Variant;
import repository.XMLStorageConnector;
import service.impl.LocalizationService;
import service.impl.SettingsService;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Root controller that connects the sidebar, editor host and top-level actions.
 */
public class MainController {
    private static final double MIN_SIDEBAR_WIDTH = 220.0;
    private static final double MAX_SIDEBAR_WIDTH = 480.0;

    @FXML
    private Label titleLabel;
    @FXML
    private Label empathyStatusLabel;
    @FXML
    private Button sidebarToggleButton;
    @FXML
    private Region sidebarToggleIcon;
    @FXML
    private Button optionsButton;
    @FXML
    private Button importButton;
    @FXML
    private Button exportButton;
    @FXML
    private Button generateButton;
    @FXML
    private HBox sidebarHost;
    @FXML
    private StackPane sidebarPane;
    @FXML
    private Region sidebarResizeHandle;
    @FXML
    private Node sidebar;

    @FXML
    private SidebarController sidebarController;
    @FXML
    private EditorHostController editorHostController;

    private final LocalizationService localizationService = LocalizationService.getInstance();
    private final SettingsService settingsService = SettingsService.getInstance();
    private final ExamGenerationDialog examGenerationDialog = new ExamGenerationDialog();
    private final XMLStorageConnector xmlStorageConnector = ApplicationContext.getInstance().getXmlStorageConnector();
    private ChildObject currentSelection;

    @FXML
    private void initialize(){
        if(sidebarController != null && editorHostController != null){
            // Keep navigation and editor refreshes synchronized through lightweight callbacks.
            sidebarController.setSelectionListener(this::handleSelection);
            sidebarController.setCreateChapterHandler(this::handleCreateChapter);
            editorHostController.setDataChangedHandler(sidebarController::setChapters);
            editorHostController.setNavigationHandler(sidebarController::refreshAndRevealSelection);
        }

        initializeSidebarLayout();
        applyTranslations();
        localizationService.localeProperty().addListener((obs, oldLocal, newLocal) -> applyTranslations());
    }

    private void initializeSidebarLayout() {
        setSidebarWidth(settingsService.getSidebarWidth());
        setSidebarVisible(settingsService.isSidebarVisible());

        if (sidebarResizeHandle != null) {
            sidebarResizeHandle.setCursor(Cursor.H_RESIZE);
            sidebarResizeHandle.setOnMouseDragged(event -> {
                if (sidebarHost == null) {
                    return;
                }
                double leftEdge = sidebarHost.localToScene(0, 0).getX();
                double requestedWidth = event.getSceneX() - leftEdge;
                setSidebarWidth(requestedWidth);
                settingsService.setSidebarWidth(requestedWidth);
                event.consume();
            });
        }
    }

    @FXML
    private void handleToggleSidebar() {
        boolean visible = sidebarPane == null || !sidebarPane.isVisible();
        setSidebarVisible(visible);
        settingsService.setSidebarVisible(visible);
    }

    private void setSidebarVisible(boolean visible) {
        setNodeVisible(sidebarPane, visible);
        setNodeVisible(sidebarResizeHandle, visible);
        if (sidebarToggleButton != null) {
            sidebarToggleButton.setTooltip(new Tooltip(localizationService.get(
                    visible ? "sidebar.toggle.hide" : "sidebar.toggle.show"
            )));
        }
        if (sidebarToggleIcon != null) {
            sidebarToggleIcon.getStyleClass().removeAll("sidebar-toggle-icon-hidden", "sidebar-toggle-icon-visible");
            sidebarToggleIcon.getStyleClass().add(visible ? "sidebar-toggle-icon-visible" : "sidebar-toggle-icon-hidden");
        }
    }

    private void setNodeVisible(Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    private void setSidebarWidth(double width) {
        double clampedWidth = Math.max(MIN_SIDEBAR_WIDTH, Math.min(MAX_SIDEBAR_WIDTH, width));
        setFixedWidth(sidebarPane, clampedWidth);
        setFixedWidth(sidebar, clampedWidth);
    }

    private void setFixedWidth(Region region, double width) {
        if (region != null) {
            region.setMinWidth(width);
            region.setPrefWidth(width);
            region.setMaxWidth(width);
        }
    }

    private void setFixedWidth(Node node, double width) {
        if (node instanceof Region region) {
            setFixedWidth(region, width);
        }
    }

    @FXML
    private void handleOpenOptions() {
        Map<String, Locale> localeLookup = new LinkedHashMap<>();
        for (Locale locale : localizationService.getSupportedLocales()) {
            String label = localizationService.get("language." + locale.getLanguage());
            localeLookup.put(label, locale);
        }

        String currentLabel = localizationService.get("language." + localizationService.getLocale().getLanguage());

        ChoiceDialog<String> dialog = new ChoiceDialog<>(currentLabel, localeLookup.keySet());
        dialog.setTitle(localizationService.get("options.language.dialog.title"));
        dialog.setHeaderText(localizationService.get("options.language.dialog.header"));
        dialog.setContentText(localizationService.get("options.language.dialog.content"));

        dialog.showAndWait().ifPresent(selected -> {
            Locale newLocale = localeLookup.get(selected);
            localizationService.setLocale(newLocale);
            showInfo(localizationService.get("options.language.dialog.title"),
                    localizationService.get("options.language.changed"));
        }); }

    @FXML
    private void handleImportXml() {
        FileChooser fileChooser = createXmlFileChooser(localizationService.get("import.fileChooser.title"));
        File selectedFile = fileChooser.showOpenDialog(getOwnerWindow());
        if (selectedFile == null || !confirmImport(selectedFile)) {
            return;
        }

        try {
            xmlStorageConnector.importDocument(selectedFile.toPath());
            if (sidebarController != null) {
                sidebarController.reloadFromStorage();
            }
            if (editorHostController != null) {
                editorHostController.displayObject(null);
            }
            showInfo(
                    localizationService.get("import.dialog.title"),
                    localizationService.get("import.dialog.success", selectedFile.getAbsolutePath())
            );
        } catch (XmlStorageException | IllegalStateException exception) {
            showError(
                    localizationService.get("import.dialog.title"),
                    localizationService.get("import.dialog.failed", messageOrFallback(exception))
            );
        }
    }

    @FXML
    private void handleExportXml() {
        FileChooser fileChooser = createXmlFileChooser(localizationService.get("export.fileChooser.title"));
        fileChooser.setInitialFileName("exam-generator-data.xml");
        File selectedFile = fileChooser.showSaveDialog(getOwnerWindow());
        if (selectedFile == null) {
            return;
        }
        selectedFile = ensureXmlExtension(selectedFile);

        try {
            xmlStorageConnector.exportDocument(selectedFile.toPath());
            showInfo(
                    localizationService.get("export.dialog.title"),
                    localizationService.get("export.dialog.success", selectedFile.getAbsolutePath())
            );
        } catch (XmlStorageException | IllegalStateException exception) {
            showError(
                    localizationService.get("export.dialog.title"),
                    localizationService.get("export.dialog.failed", messageOrFallback(exception))
            );
        }
    }

    @FXML
    private void handleGenerateExam() {
        examGenerationDialog.show(generateButton.getScene().getWindow());
    }

    @FXML
    private void handleSelection(ChildObject selection){
        currentSelection = selection;
        updateEmpathyStatus();
        editorHostController.displayObject(selection);
    }

    private void handleCreateChapter() {
        currentSelection = null;
        updateEmpathyStatus();
        editorHostController.displayCreateChapter();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private boolean confirmImport(File selectedFile) {
        ButtonType importType = new ButtonType(
                localizationService.get("import.confirm.action"),
                ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelType = new ButtonType(
                localizationService.get("editor.delete.confirm.cancel"),
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(localizationService.get("import.confirm.title"));
        alert.setHeaderText(localizationService.get("import.confirm.header"));
        alert.setContentText(localizationService.get("import.confirm.content", selectedFile.getAbsolutePath()));
        alert.getButtonTypes().setAll(importType, cancelType);
        return alert.showAndWait()
                .filter(importType::equals)
                .isPresent();
    }

    private FileChooser createXmlFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(localizationService.get("xml.fileFilter"), "*.xml")
        );
        return fileChooser;
    }

    private File ensureXmlExtension(File selectedFile) {
        if (selectedFile.getName().toLowerCase(Locale.ROOT).endsWith(".xml")) {
            return selectedFile;
        }
        File parent = selectedFile.getParentFile();
        return parent == null
                ? new File(selectedFile.getName() + ".xml")
                : new File(parent, selectedFile.getName() + ".xml");
    }

    private Window getOwnerWindow() {
        if (titleLabel != null && titleLabel.getScene() != null) {
            return titleLabel.getScene().getWindow();
        }
        return null;
    }

    private String messageOrFallback(Exception exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? localizationService.get("editor.error.unknown")
                : message;
    }

    private void applyTranslations(){
        if(titleLabel != null){
            titleLabel.setText(localizationService.get("header.title"));
        }
        updateEmpathyStatus();
        if (optionsButton != null) {
            optionsButton.setText(localizationService.get("buttons.options"));
        }
        if (importButton != null) {
            importButton.setText(localizationService.get("buttons.importXml"));
        }
        if (exportButton != null) {
            exportButton.setText(localizationService.get("buttons.exportXml"));
        }
        if (generateButton != null) {
            generateButton.setText(localizationService.get("buttons.generateExam"));
        }
        if (sidebarPane != null) {
            setSidebarVisible(sidebarPane.isVisible());
        }
    }

    private void updateEmpathyStatus() {
        if (empathyStatusLabel == null) {
            return;
        }

        // Aufgabe 22 - UI/UX-Rule "Empathy": react to the user's current focus with reassuring context.
        if (currentSelection == null) {
            empathyStatusLabel.setText(localizationService.get("main.empathy.default"));
            return;
        }

        String title = currentSelection.getTitle() == null || currentSelection.getTitle().isBlank()
                ? String.valueOf(currentSelection.getId())
                : currentSelection.getTitle();
        if (currentSelection instanceof Chapter) {
            empathyStatusLabel.setText(localizationService.get("main.empathy.chapter", title));
        } else if (currentSelection instanceof Subtask) {
            empathyStatusLabel.setText(localizationService.get("main.empathy.subtask", title));
        } else if (currentSelection instanceof Variant) {
            empathyStatusLabel.setText(localizationService.get("main.empathy.variant", title));
        } else {
            empathyStatusLabel.setText(localizationService.get("main.empathy.generic", title));
        }
    }
}

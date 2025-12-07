package controller;

import controller.editor.EditorHostController;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import objects.ChildObject;
import service.LocalizationService;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class MainController {
    @FXML
    private Label titleLabel;
    @FXML
    private Button optionsButton;
    @FXML
    private Button importButton;
    @FXML
    private Button exportButton;
    @FXML
    private Button generateButton;

    @FXML
    private SidebarController sidebarController;
    @FXML
    private EditorHostController editorHostController;

    private final LocalizationService localizationService = LocalizationService.getInstance();

    @FXML
    private void initialize(){
        if(sidebarController != null && editorHostController != null){
            sidebarController.setSelectionListener(this::handleSelection);
        }

        applyTranslations();
        localizationService.localeProperty().addListener((obs, oldLocal, newLocal) -> applyTranslations());
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
        showInfo(localizationService.get("import.dialog.title"), localizationService.get("import.dialog.message"));
    }

    @FXML
    private void handleExportXml() {
        showInfo(localizationService.get("export.dialog.title"), localizationService.get("export.dialog.message"));
    }

    @FXML
    private void handleGenerateExam() {
        showInfo(localizationService.get("generate.dialog.title"), localizationService.get("generate.dialog.message"));
    }

    @FXML
    private void handleSelection(ChildObject selection){
        editorHostController.displayObject(selection);
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void applyTranslations(){
        if(titleLabel != null){
            titleLabel.setText(localizationService.get("header.title"));
        }
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
    }
}
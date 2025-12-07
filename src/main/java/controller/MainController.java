package controller;

import controller.editor.EditorHostController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import objects.ChildObject;

public class MainController {
    @FXML
    private Label label;
    @FXML
    private SidebarController sidebarController;
    @FXML
    private EditorHostController editorHostController;

    @FXML
    private void initialize(){
        if(sidebarController != null && editorHostController != null){
            sidebarController.setSelectionListener(this::handleSelection);
        }
    }


    @FXML
    private void handleOpenOptions() {
        showInfo("Optionen", "Einstellungsdialog folgt. Bis dahin kannst du hier Platzhalteraktionen auslösen.");
    }

    @FXML
    private void handleImportXml() {
        showInfo("XML importieren", "XML-Import wird vorbereitet. Wähle hier künftig eine Datei aus, um Daten zu laden.");
    }

    @FXML
    private void handleExportXml() {
        showInfo("XML exportieren", "XML-Export wird vorbereitet. Die generierten Daten können bald gespeichert werden.");
    }

    @FXML
    private void handleGenerateExam() {
        showInfo("Klausur generieren", "Die Klausur-Generierung wird in Kürze implementiert.");
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
}
package controller.exam;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import service.exam.dto.PdfLayoutSettings;
import service.impl.LocalizationService;

import java.io.IOException;

public class PdfLayoutDialog {

    private final LocalizationService localizationService = LocalizationService.getInstance();

    public PdfLayoutSettings show(Window owner, String examTitle, PdfLayoutSettings currentSettings) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/PdfLayoutDialog.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load PDF layout dialog.", exception);
        }

        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setTitle(localizationService.get("layout.dialog.title"));
        dialogStage.setMinWidth(620);
        dialogStage.setMinHeight(480);

        PdfLayoutDialogController controller = loader.getController();
        controller.configure(dialogStage, examTitle, currentSettings);

        Scene scene = new Scene(root);
        scene.getStylesheets().addAll(owner.getScene().getStylesheets());
        dialogStage.setScene(scene);
        dialogStage.showAndWait();

        return controller.getResult();
    }
}

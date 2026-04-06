package controller.exam;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import service.impl.LocalizationService;

import java.io.IOException;

public class ExamGenerationDialog {

    private final LocalizationService localizationService = LocalizationService.getInstance();

    public void show(Window owner) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/ExamGenerationDialog.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load exam generation dialog.", exception);
        }

        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setTitle(localizationService.get("generate.dialog.title"));
        dialogStage.setMinWidth(720);
        dialogStage.setMinHeight(620);

        ExamGenerationDialogController controller = loader.getController();
        controller.configure(dialogStage);

        Scene scene = new Scene(root);
        scene.getStylesheets().addAll(owner.getScene().getStylesheets());
        dialogStage.setScene(scene);
        dialogStage.show();
    }
}

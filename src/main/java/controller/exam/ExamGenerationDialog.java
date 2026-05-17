package controller.exam;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import service.impl.LocalizationService;

import java.io.IOException;

/**
 * Opens the modal dialog used to configure and generate an exam PDF.
 */
public class ExamGenerationDialog {
    private static final double DEFAULT_WIDTH = 720.0;
    private static final double DEFAULT_HEIGHT = 560.0;
    private static final double MIN_WIDTH = 640.0;
    private static final double MIN_HEIGHT = 460.0;
    private static final double SCREEN_PADDING = 80.0;

    private final LocalizationService localizationService = LocalizationService.getInstance();

    /**
     * Loads and displays the exam generation dialog.
     *
     * @param owner owner window used for modality and stylesheet inheritance
     */
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

        ExamGenerationDialogController controller = loader.getController();
        controller.configure(dialogStage);

        Rectangle2D visualBounds = visualBoundsFor(owner);
        double width = clamp(DEFAULT_WIDTH, MIN_WIDTH, visualBounds.getWidth() - SCREEN_PADDING);
        double height = clamp(DEFAULT_HEIGHT, MIN_HEIGHT, visualBounds.getHeight() - SCREEN_PADDING);

        dialogStage.setMinWidth(Math.min(MIN_WIDTH, width));
        dialogStage.setMinHeight(Math.min(MIN_HEIGHT, height));

        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().addAll(owner.getScene().getStylesheets());
        dialogStage.setScene(scene);
        dialogStage.show();
        keepInsideScreen(dialogStage, owner, visualBounds);
    }

    private Rectangle2D visualBoundsFor(Window owner) {
        if (owner == null) {
            return Screen.getPrimary().getVisualBounds();
        }
        return Screen.getScreensForRectangle(owner.getX(), owner.getY(), owner.getWidth(), owner.getHeight())
                .stream()
                .findFirst()
                .orElse(Screen.getPrimary())
                .getVisualBounds();
    }

    private double clamp(double preferred, double min, double max) {
        return Math.max(Math.min(min, max), Math.min(preferred, max));
    }

    private void keepInsideScreen(Stage dialogStage, Window owner, Rectangle2D visualBounds) {
        double targetX = owner == null
                ? visualBounds.getMinX() + (visualBounds.getWidth() - dialogStage.getWidth()) / 2.0
                : owner.getX() + (owner.getWidth() - dialogStage.getWidth()) / 2.0;
        double targetY = owner == null
                ? visualBounds.getMinY() + (visualBounds.getHeight() - dialogStage.getHeight()) / 2.0
                : owner.getY() + (owner.getHeight() - dialogStage.getHeight()) / 2.0;

        double maxX = visualBounds.getMaxX() - dialogStage.getWidth() - 20.0;
        double maxY = visualBounds.getMaxY() - dialogStage.getHeight() - 20.0;
        dialogStage.setX(clamp(targetX, visualBounds.getMinX() + 20.0, maxX));
        dialogStage.setY(clamp(targetY, visualBounds.getMinY() + 20.0, maxY));
    }
}

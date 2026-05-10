import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.impl.LocalizationService;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * JavaFX entry point for the SWE Exam Generator application.
 */
public class App extends Application {

    private static final String FXML_PATH = "/fxml/MainView.fxml";
    private static final List<String> CSS_PATHS = Arrays.asList("/style/index.css", "/style/chapterEditorPage.css", "/style/sidebar.css");

    private final LocalizationService localizationService = LocalizationService.getInstance();

    /**
     * Initializes the primary JavaFX stage and loads the main view.
     *
     * @param stage primary stage provided by JavaFX
     * @throws Exception if the initial view cannot be loaded
     */
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = loadRoot();
        Scene scene = new Scene(root);

        applyCss(scene);

        stage.setTitle(localizationService.get("app.title"));
        localizationService.localeProperty().addListener((obs, oldLocale, newLocale) ->
                stage.setTitle(localizationService.get("app.title"))
        );
        stage.setScene(scene);
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * Loads the main FXML and returns its root node.
     */
    private Parent loadRoot() {
        URL fxml = getClass().getResource(FXML_PATH);
        Objects.requireNonNull(fxml, "FXML not found on classpath at " + FXML_PATH);
        try {
            return new FXMLLoader(fxml).load();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load FXML: " + FXML_PATH, e);
        }
    }

    /**
     * Applies the configured CSS files to the target scene.
     */
    private void applyCss(Scene targetScene) {
        targetScene.getStylesheets().clear();

        for (String path : CSS_PATHS) {
            URL css = getClass().getResource(path);
            Objects.requireNonNull(css, "CSS not found on classpath at " + path);
            targetScene.getStylesheets().add(css.toExternalForm());
        }

        // Force CSS application to the current root after stylesheets are attached.
        if (targetScene.getRoot() != null) {
            targetScene.getRoot().applyCss();
        }
    }

    /**
     * Starts the JavaFX runtime.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}

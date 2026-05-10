import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import service.impl.LocalizationService;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * JavaFX entry point for the SWE Exam Generator application.
 */
public class App extends Application {

    private static final String FXML_PATH = "/fxml/MainView.fxml";
    private static final List<String> CSS_PATHS = Arrays.asList("/style/index.css", "/style/chapterEditorPage.css", "/style/sidebar.css");

    private Stage primaryStage;
    private Scene scene;
    private final LocalizationService localizationService = LocalizationService.getInstance();

    /**
     * Initializes the primary JavaFX stage and loads the main view.
     *
     * @param stage primary stage provided by JavaFX
     * @throws Exception if the initial view cannot be loaded
     */
    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;

        Parent root = loadRoot();
        this.scene = new Scene(root);

        applyCss(scene);

        stage.setTitle(localizationService.get("app.title"));
        localizationService.localeProperty().addListener((obs, oldLocale, newLocale) ->
                primaryStage.setTitle(localizationService.get("app.title"))
                );
        stage.setScene(scene);
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.centerOnScreen();
        stage.show();

        // F5 triggers a lightweight UI reload during development.
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F5) {
                reloadUI();
            }
        });

        // Optional file watcher for development when resources are copied on save.
        // new DevReloader(this::reloadUI).watchResourcesAsync();
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
     * Reloads the configured CSS files for the target scene.
     */
    private void applyCss(Scene targetScene) {
        targetScene.getStylesheets().clear();

        for (String path : CSS_PATHS) {
            URL css = getClass().getResource(path);
            Objects.requireNonNull(css, "CSS not found on classpath at " + path);
            targetScene.getStylesheets().add(css.toExternalForm());
        }

        // Force CSS re-application to the current root after stylesheet reload.
        if (targetScene.getRoot() != null) {
            targetScene.getRoot().applyCss();
        }
    }

    /**
     * Reloads FXML and CSS without restarting the application.
     */
    public void reloadUI() {
        try {
            Parent newRoot = loadRoot();
            scene.setRoot(newRoot);
            applyCss(scene);
            System.out.println("[Reload] FXML & CSS neu geladen.");
        } catch (Exception ex) {
            ex.printStackTrace();
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

    /**
     * Lightweight development watcher for FXML and CSS resources.
     */
    static final class DevReloader {
        private final Runnable onChange;

        DevReloader(Runnable onChange) {
            this.onChange = onChange;
        }

        void watchResourcesAsync() {
            // Candidate output locations for IDE and build-tool resource copies.
            Path[] candidates = new Path[]{
                    Paths.get("out/production"),
                    Paths.get("build/resources/main")
            };

            for (Path base : candidates) {
                if (Files.exists(base)) {
                    Path fxmlDir = findExisting(base, "fxml");
                    Path styleDir = findExisting(base, "style");
                    startWatcherThread(fxmlDir, styleDir);
                    return;
                }
            }
            System.out.println("[Reload] Watcher nicht aktiv (Zielordner nicht gefunden).");
        }

        private Path findExisting(Path base, String child) {
            Path p = base.resolve(child);
            return Files.exists(p) ? p : null;
        }

        private void startWatcherThread(Path... dirs) {
            new Thread(() -> {
                try (WatchService ws = FileSystems.getDefault().newWatchService()) {
                    for (Path dir : dirs) {
                        if (dir != null) {
                            dir.register(ws,
                                    StandardWatchEventKinds.ENTRY_CREATE,
                                    StandardWatchEventKinds.ENTRY_MODIFY,
                                    StandardWatchEventKinds.ENTRY_DELETE);
                            System.out.println("[Reload] Watching: " + dir.toAbsolutePath());
                        }
                    }
                    while (true) {
                        WatchKey key = ws.take();
                        boolean relevant = key.pollEvents().stream().anyMatch(e -> {
                            String name = e.context().toString().toLowerCase();
                            return name.endsWith(".fxml") || name.endsWith(".css");
                        });
                        key.reset();
                        if (relevant) {
                            Platform.runLater(onChange);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, "DevReloaderWatcher").start();
        }
    }
}

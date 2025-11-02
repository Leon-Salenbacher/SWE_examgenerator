import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class App extends Application {

    private static final String FXML_PATH = "/fxml/MainView.fxml";
    private static final List<String> CSS_PATHS = Arrays.asList("/style/index.css", "/style/chapterEditorPage.css");

    private Stage primaryStage;
    private Scene scene;

    @Override
    public void start(Stage stage) throws Exception {
        this.primaryStage = stage;

        Parent root = loadRoot();
        this.scene = new Scene(root);

        applyCss(scene);

        stage.setTitle("Exam Generator");
        stage.setScene(scene);
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.centerOnScreen();
        stage.show();

        // F5 = reload
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F5) {
                reloadUI();
            }
        });

        // Optional: File-Watcher für Dev (funktioniert, wenn IntelliJ Ressourcen bei Save automatisch kopiert)
        // new DevReloader(this::reloadUI).watchResourcesAsync();
    }

    /**
     * Lädt das FXML frisch und liefert die Wurzel zurück.
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
     * Lädt CSS neu (cleart die Stylesheets und hängt sie erneut an).
     */
    private void applyCss(Scene targetScene) {
        targetScene.getStylesheets().clear(); // clear once

        for (String path : CSS_PATHS) {
            URL css = getClass().getResource(path);
            Objects.requireNonNull(css, "CSS not found on classpath at " + path);
            targetScene.getStylesheets().add(css.toExternalForm());
        }

        // Optional: force re-apply of CSS to current root
        if (targetScene.getRoot() != null) {
            targetScene.getRoot().applyCss();
        }
    }

    /**
     * Public, damit du sie auch aus Controllern oder Dev-Buttons triggern kannst.
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

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * ---- Optionaler Dev-Watcher (leichtgewichtig) ----
     */
    static final class DevReloader {
        private final Runnable onChange;

        DevReloader(Runnable onChange) {
            this.onChange = onChange;
        }

        void watchResourcesAsync() {
            // Passen: Pfad zum ausgegebenen Ressourcen-Ordner (IntelliJ kopiert hierher)
            // z.B. out/production/<module>/fxml  und  out/production/<module>/style
            // Oder bei Gradle: build/resources/main
            Path[] candidates = new Path[]{
                    Paths.get("out/production"), // IntelliJ (anpassen!)
                    Paths.get("build/resources/main") // Gradle (anpassen!)
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

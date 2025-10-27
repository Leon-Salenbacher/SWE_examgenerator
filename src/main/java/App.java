import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        URL fxml = getClass().getResource("/fxml/MainView.fxml");
        Objects.requireNonNull(fxml, "MainView.fxml not found on classpath at /fxml/MainView.fxml");
        Scene scene = new Scene(new FXMLLoader(fxml).load());

        URL css = getClass().getResource("/style/index.css");
        Objects.requireNonNull(css, "index.css not found on classpath at /style/index.css");
        scene.getStylesheets().add(css.toExternalForm());

        stage.setTitle("Exam Generator");
        stage.setScene(scene);
        stage.setWidth(1200);
        stage.setHeight(800);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
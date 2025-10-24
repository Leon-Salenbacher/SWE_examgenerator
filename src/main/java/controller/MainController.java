package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {
    @FXML
    private Label label;

    @FXML
    private void onClick() {
        label.setText("Button clicked!");
    }
}
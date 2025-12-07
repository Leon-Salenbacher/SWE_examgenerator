package controller.editor;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import objects.ChildObject;

import java.util.function.Consumer;

public class EditorChildRowController {

    @FXML
    private Label titleLabel;

    @FXML
    private Button openButton;

    private ChildObject data;
    private Consumer<ChildObject> selectionHandler;

    public void configure(ChildObject data, Consumer<ChildObject> selectionHandler){
        this.data = data;
        this.selectionHandler = selectionHandler;
        this.titleLabel.setText(data != null ? data.getTitle() :  "");
    }

    @FXML private void handleOpen(){
        if(selectionHandler != null && data != null){
            selectionHandler.accept(data);
        }
    }
}

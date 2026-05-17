package controller.editor;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import models.ChildObject;

import java.util.function.Consumer;

/**
 * Controller for one child row displayed inside the parent editor.
 */
public class EditorChildRowController {

    @FXML
    private Label titleLabel;

    private ChildObject data;
    private Consumer<ChildObject> selectionHandler;

    /**
     * Binds a child object to the row and registers the open callback.
     *
     * @param data child object represented by this row
     * @param selectionHandler callback invoked when the row is opened
     */
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

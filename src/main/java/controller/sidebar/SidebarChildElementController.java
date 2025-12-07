package controller.sidebar;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import objects.ChildObject;

public class SidebarChildElementController implements SidebarElementController{
    @FXML
    protected Label title;
    @FXML
    protected VBox root;

    protected SidebarSelectionCoordinator selectionCoordinator;
    protected ChildObject data;

    public void setTitle(String titleText){
        title.setText(titleText);
    }

    public void setSelectionCoordinator(SidebarSelectionCoordinator sidebarSelectionCoordinator){
        this.selectionCoordinator = sidebarSelectionCoordinator;
    }

    public void setData(ChildObject data){
        this.data = data;
    }

    @FXML
    protected void handleSelect(MouseEvent event){
        selectSelf();
        if(event != null){
            event.consume();
        }
    }

    protected void selectSelf(){
        if(selectionCoordinator != null){
            selectionCoordinator.select(root, data);
        }

        ObservableList<String> styleClasses = root.getStyleClass();
        if(!styleClasses.contains("selected")){
            styleClasses.add("selected");
        }
    }

}

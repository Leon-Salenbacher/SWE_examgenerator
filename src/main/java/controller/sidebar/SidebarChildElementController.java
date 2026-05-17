package controller.sidebar;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import models.ChildObject;

/**
 * Controller for one selectable leaf row in the sidebar.
 */
public class SidebarChildElementController implements SidebarElementController{
    @FXML
    protected Label title;
    @FXML
    protected VBox root;

    protected SidebarSelectionCoordinator selectionCoordinator;
    protected ChildObject data;

    /**
     * Sets the visible row title.
     *
     * @param titleText title text
     */
    public void setTitle(String titleText){
        title.setText(titleText);
    }

    /**
     * Sets the coordinator that owns sidebar selection state.
     *
     * @param sidebarSelectionCoordinator selection coordinator
     */
    public void setSelectionCoordinator(SidebarSelectionCoordinator sidebarSelectionCoordinator){
        this.selectionCoordinator = sidebarSelectionCoordinator;
    }

    /**
     * Associates this row with its domain object.
     *
     * @param data represented domain object
     */
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

    /**
     * Selects this row programmatically.
     */
    public void selectNode() {
        selectSelf();
    }

    /**
     * Checks whether this row represents the same persistent object.
     *
     * @param target object to compare
     * @return {@code true} when type and id match
     */
    public boolean matchesData(ChildObject target) {
        if (data == null || target == null) {
            return false;
        }
        return data.getClass().equals(target.getClass()) && data.getId() == target.getId();
    }

    /**
     * @return domain object represented by this row
     */
    public ChildObject getData() {
        return data;
    }

}

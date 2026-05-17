package controller.sidebar;


import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import models.ChildObject;

import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * Sidebar row controller for expandable parent objects.
 */
public class SidebarParentElementController extends SidebarChildElementController implements  SidebarElementController{
    @FXML
    protected VBox childContainer;

    @FXML
    protected ImageView chevronImg;

    private boolean open = false;
    private boolean hasChild = false;

    @FXML
    protected void initialize(){
        loadChevron();
        setOpen(false, false);
    }

    protected void loadChevron(){
        URL chevronResource = getClass().getResource("/icons/chevron-down.png");
        if (chevronResource == null) {
            throw new IllegalStateException("Unable to load sidebar chevron icon.");
        }
        chevronImg.setImage(new Image(chevronResource.toExternalForm()));
    }

    @FXML
    protected void toggleOpen(MouseEvent event){
        boolean wasSelected = root.getStyleClass().contains("selected");
        selectSelf();
        if (wasSelected) {
            setOpen(!open, true);
        } else {
            setOpen(true, true);
        }

        if(event != null){
            event.consume();
        }
    }

    @FXML
    @Override
    protected void handleSelect(MouseEvent event) {
        boolean wasSelected = root.getStyleClass().contains("selected");
        selectSelf();
        if (!wasSelected) {
            setOpen(true, true);
        }

        if (event != null) {
            event.consume();
        }
    }

    /**
     * Updates the visible title and resets the expanded state.
     *
     * @param titleText text shown for this parent row
     */
    @Override
    public void setTitle(String titleText){
        title.setText(titleText);
        this.open = false;
    }

    protected void setOpen(boolean value, boolean animate){
        this.open = value;

        // Visibility and managed state must move together so collapsed rows do not reserve space.
        childContainer.setVisible(open);
        childContainer.setManaged(open);

        if(open){
            if(!root.getStyleClass().contains("open")){
                root.getStyleClass().add("open");
            }
        }else{
            root.getStyleClass().remove("open");
        }

        // Rotate the chevron to match the persisted open state.
        double toAngle = open ? 0 : -90;
        if(animate){
            RotateTransition rt = new RotateTransition(Duration.millis(120), chevronImg);
            rt.setToAngle(toAngle);
            rt.play();
        }else{
            chevronImg.setRotate(toAngle);
        }
    }
    /**
     * Loads nested children into the expandable container.
     *
     * @param childElements child objects to render
     */
    public void loadChildElements(List<? extends ChildObject> childElements){
        if(childElements == null || childElements.isEmpty()){
            return;
        }
        this.hasChild = true;
        for(ChildObject child : childElements){
            Node childNode = SidebarElementController.createElement(child, selectionCoordinator);
            childContainer.getChildren().add(childNode);
        }
    }

    protected void onOpen(){
        setOpen(true, true);
    }

    /**
     * @return whether this sidebar parent is expanded
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Applies an expanded/collapsed state without animation.
     *
     * @param value desired open state
     */
    public void applyOpenState(boolean value) {
        setOpen(value, false);
    }

    /**
     * @return immutable view of child row nodes
     */
    public List<Node> getChildNodes() {
        return Collections.unmodifiableList(childContainer.getChildren());
    }

    /**
     * Expands the path to a target object and returns the matching row controller.
     *
     * @param target object to reveal
     * @return matching child controller or {@code null}
     */
    public SidebarChildElementController revealPathTo(ChildObject target) {
        if (matchesData(target)) {
            return this;
        }

        for (Node childNode : childContainer.getChildren()) {
            Object userData = childNode.getUserData();
            if (userData instanceof SidebarParentElementController parentController) {
                SidebarChildElementController match = parentController.revealPathTo(target);
                if (match != null) {
                    setOpen(true, false);
                    return match;
                }
            } else if (userData instanceof SidebarChildElementController childController && childController.matchesData(target)) {
                setOpen(true, false);
                return childController;
            }
        }

        return null;
    }


}

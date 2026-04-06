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

import java.util.Collections;
import java.util.List;

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
        try{
            chevronImg.setImage(new Image(getClass().getResource("/icons/chevron-down.png").toExternalForm()));
        }catch(NullPointerException e){
            e.printStackTrace();
        }
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

    @Override
    public void setTitle(String titleText){
        title.setText(titleText);
        this.open = false;
    }

    protected void setOpen(boolean value, boolean animate){
        this.open = value;

        //inhalt on-off
        childContainer.setVisible(open);
        childContainer.setManaged(open);

        if(open){
            if(!root.getStyleClass().contains("open")){
                root.getStyleClass().add("open");
            }
        }else{
            root.getStyleClass().remove("open");
        }

        //rotate Chevron
        double toAngle = open ? 0 : -90;
        if(animate){
            RotateTransition rt = new RotateTransition(Duration.millis(120), chevronImg);
            rt.setToAngle(toAngle);
            rt.play();
        }else{
            chevronImg.setRotate(toAngle);
        }
    }




    public void loadChildElements(List<? extends ChildObject> childElements){
        //load child elements
        if(childElements == null || childElements.isEmpty()){
            return;
        }
        this.hasChild = true;
        // if child object is also parent object, load as parent object
        for(ChildObject child : childElements){
            Node childNode = SidebarElementController.createElement(child, selectionCoordinator);
            childContainer.getChildren().add(childNode);
        }

        //when given elements are also parentElements,
    }

    protected void onOpen(){
        setOpen(true, true);
    }

    public boolean isOpen() {
        return open;
    }

    public void applyOpenState(boolean value) {
        setOpen(value, false);
    }

    public List<Node> getChildNodes() {
        return Collections.unmodifiableList(childContainer.getChildren());
    }

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

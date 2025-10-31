package controller.sidebar;


import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import objects.Chapter;
import objects.ChildObject;
import objects.DataObject;
import objects.ParentObject;

import java.io.IOException;
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
    }

    protected void loadChevron(){
        try{
            chevronImg.setImage(new Image(getClass().getResource("icons/chevron-down.png").toExternalForm()));
        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }

    @FXML
    protected void toggleOpen(){
        setOpen(!open, true);
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
            Node childNode = SidebarElementController.createElement(child);
            childContainer.getChildren().add(childNode);
        }

        //when given elements are also parentElements,
    }

    protected void onOpen(){
        setOpen(true, true);
    }
}

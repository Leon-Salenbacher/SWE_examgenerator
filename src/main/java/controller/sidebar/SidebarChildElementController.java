package controller.sidebar;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public abstract class SidebarChildElementController {
    @FXML
    protected Label title;


    public void setTitle(){
        title.setText("Test");
    }


}

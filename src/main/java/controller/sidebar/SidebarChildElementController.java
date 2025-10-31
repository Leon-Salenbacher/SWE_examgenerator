package controller.sidebar;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SidebarChildElementController {
    @FXML
    protected Label title;


    public void setTitle(String titleText){
        title.setText(titleText);
    }
}

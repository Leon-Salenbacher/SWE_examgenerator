package controller.sidebar;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import objects.ChildObject;
import objects.ParentObject;

import java.io.IOException;

public interface SidebarElementController {
    public static String PARENT_ELEMENT_FXML_PATH = "/fxml/components/Sidebar_ParentElement.fxml";
    public static String CHILD_ELEMENT_FXML_PATH = "/fxml/components/Sidebar_ChildElement.fxml";

    public static <T extends ChildObject> Node createElement(ParentObject<T> element){
        try {
            FXMLLoader loader = new FXMLLoader(SidebarParentElementController.class.getResource(PARENT_ELEMENT_FXML_PATH));
            Node node = loader.load();

            SidebarParentElementController controller = loader.getController();
            controller.setTitle(element.getTitle());
            controller.loadChildElements(element.getChildElements());
            return node;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load sidebar parent element", e);

        }
    }

    public static  Node createElement(ChildObject element) {
        if(element instanceof ParentObject<?>){
            ParentObject<? extends ChildObject> parent = (ParentObject<? extends  ChildObject>) element;
            return createElement(parent);
        }

        try{
            FXMLLoader loader = new FXMLLoader(SidebarChildElementController.class.getResource(CHILD_ELEMENT_FXML_PATH));
            Node node = loader.load();

            SidebarChildElementController controller = loader.getController();
            controller.setTitle(element.getTitle());
            return node;
        }catch(IOException e){
            throw new IllegalStateException("Unable to load sidebar child element", e);
        }
    }
}

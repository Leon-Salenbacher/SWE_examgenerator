package controller.sidebar;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import objects.ChildObject;
import objects.DataObject;
import objects.ParentObject;

import javax.xml.crypto.Data;
import java.io.IOException;

public interface SidebarElementController {
    public static <T extends ChildObject> Node createElement(ParentObject<T> element){
        try {
            FXMLLoader loader = new FXMLLoader(SidebarParentElementController.class.getResource("/fxml/components/Sidebar_ParentElement.fxml"));
            Node node = loader.load();

            SidebarParentElementController controller = loader.getController();
            controller.setTitle(element.getTitle());
            // ggf. controller.loadChildElements(chapter.getSubtasks());
            return node;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    };
}

package controller.editor;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import models.ChildObject;
import models.ParentObject;
import service.impl.LocalizationService;

import java.io.IOException;
import java.util.function.Consumer;

public class EditorHostController {
    @FXML
    private ScrollPane editorScroll;

    @FXML
    private StackPane contentHost;

    private Node parentEditorRoot;
    private Node childEditorRoot;
    private Label placeholderLabel;

    private ParentEditorController parentEditorController;
    private ChildEditorController childEditorController;
    private Runnable dataChangedHandler;
    private Consumer<ChildObject> navigationHandler;
    private EditorFeedbackRequest pendingFeedback;

    private final LocalizationService localizationService = LocalizationService.getInstance();

    @FXML
    private void initialize(){
        bindContentToViewport();
        showPlaceholder();
        localizationService
                .localeProperty()
                .addListener((obs, oldLocale, newLocale) -> applyTranslations());
        applyTranslations();
    }

    private void bindContentToViewport() {
        contentHost.minWidthProperty().bind(Bindings.createDoubleBinding(
                () -> viewportBounds().getWidth(),
                editorScroll.viewportBoundsProperty()
        ));
        contentHost.minHeightProperty().bind(Bindings.createDoubleBinding(
                () -> viewportBounds().getHeight(),
                editorScroll.viewportBoundsProperty()
        ));
    }

    private Bounds viewportBounds() {
        Bounds bounds = editorScroll.getViewportBounds();
        return bounds == null ? new BoundingBox(0, 0, 0, 0) : bounds;
    }

    public void displayObject(ChildObject data){
        if(data == null){
            showPlaceholder();
            return;
        }

        if(data instanceof ParentObject<?>){
            showParentEditor((ParentObject<? extends ChildObject>) data);
        }else{
            showChildEditor(data);
        }

        applyPendingFeedback(data);
    }

    private void showParentEditor(ParentObject<? extends ChildObject> data){
        ensureParentEditor();
        parentEditorController.displayParent(data);
        setContent(parentEditorRoot);
    }

    private void showChildEditor(ChildObject data){
        ensureChildEditor();
        childEditorController.displayChild(data);
        setContent(childEditorRoot);
    }

    private void setContent(Node node){
        contentHost.getChildren().setAll(node);
    }

    private void showPlaceholder(){
        if (placeholderLabel == null) {
            placeholderLabel = new Label();
            placeholderLabel.getStyleClass().add("section-label");
        }
        placeholderLabel.setText(localizationService.get("editor.selectPrompt"));
        contentHost.getChildren().setAll(placeholderLabel);
    }

    private void applyTranslations() {
        if (contentHost.getChildren().contains(placeholderLabel)) {
            showPlaceholder();
        }
    }

    public void setDataChangedHandler(Runnable dataChangedHandler) {
        this.dataChangedHandler = dataChangedHandler;
        if (parentEditorController != null) {
            parentEditorController.setDataChangedHandler(dataChangedHandler);
        }
        if (childEditorController != null) {
            childEditorController.setDataChangedHandler(dataChangedHandler);
        }
    }

    public void setNavigationHandler(Consumer<ChildObject> navigationHandler) {
        this.navigationHandler = navigationHandler;
        if (parentEditorController != null) {
            parentEditorController.setNavigationHandler(navigationHandler);
        }
    }

    public void displayObjectWithFeedback(EditorFeedbackRequest feedbackRequest) {
        if (feedbackRequest == null) {
            return;
        }
        pendingFeedback = feedbackRequest;
        displayObject(feedbackRequest.data());
    }

    private void ensureParentEditor(){
        if(parentEditorController != null){
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/elements/ParentEditor.fxml"));
        try {
            parentEditorRoot = loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load parent editor", e);
        }

        parentEditorController = loader.getController();
        parentEditorController.setSelectionHandler(this::displayObject);
        parentEditorController.setDisplayHandler(this::displayObject);
        parentEditorController.setFeedbackHandler(this::displayObjectWithFeedback);
        parentEditorController.setNavigationHandler(navigationHandler);
        parentEditorController.setDataChangedHandler(dataChangedHandler);
    }

    private void ensureChildEditor() {
        if (childEditorController != null) {
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/elements/ChildEditor.fxml"));
        try {
            childEditorRoot = loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load child editor", e);
        }

        childEditorController = loader.getController();
        childEditorController.setDataChangedHandler(dataChangedHandler);
        childEditorController.setDisplayHandler(this::displayObject);
        childEditorController.setFeedbackHandler(this::displayObjectWithFeedback);
        childEditorController.setNavigationHandler(navigationHandler);
    }

    private void applyPendingFeedback(ChildObject data) {
        if (pendingFeedback == null || data == null) {
            return;
        }

        if (!sameObject(pendingFeedback.data(), data)) {
            return;
        }

        if (data instanceof ParentObject<?>) {
            parentEditorController.showTransientFeedback(pendingFeedback.message(), pendingFeedback.success());
        } else {
            childEditorController.showTransientFeedback(pendingFeedback.message(), pendingFeedback.success());
        }

        pendingFeedback = null;
    }

    private boolean sameObject(ChildObject left, ChildObject right) {
        if (left == null || right == null) {
            return false;
        }
        return left.getClass().equals(right.getClass()) && left.getId() == right.getId();
    }
}

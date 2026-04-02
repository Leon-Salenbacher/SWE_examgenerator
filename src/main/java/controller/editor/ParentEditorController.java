package controller.editor;

import config.ApplicationContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import objects.Chapter;
import objects.ChildObject;
import objects.ParentObject;
import objects.Subtask;
import objects.Variant;
import service.impl.LocalizationService;
import service.impl.elements.ChapterServiceImpl;
import service.impl.elements.SubtaskServiceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ParentEditorController {
    @FXML
    private Label headerLabel;

    @FXML
    private TextField titleField;

    @FXML
    private VBox pointsBox;

    @FXML
    private TextField pointsField;

    @FXML
    private Label childSectionLabel;

    @FXML
    private VBox labelsBox;

    @FXML
    private Label labelsLabel;

    @FXML
    private TextField labelsField;

    @FXML
    private Label titleLabel;
    @FXML
    private Label pointsLabel;
    @FXML
    private Button deleteButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button createButton;

    @FXML
    private Button addChild;

    @FXML
    private VBox childList;

    private ParentObject<? extends ChildObject> currentParent;
    private Consumer<ChildObject> selectionHandler;
    private Runnable dataChangedHandler;
    private boolean createMode;
    private final LocalizationService localizationService = LocalizationService.getInstance();
    private final ChapterServiceImpl chapterService = ApplicationContext.getInstance().getChapterService();
    private final SubtaskServiceImpl subtaskService = ApplicationContext.getInstance().getSubtaskService();

    @FXML
    private void initialize(){
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (createMode) {
                return;
            }
            if(currentParent instanceof Chapter){
                ((Chapter) currentParent).setTitle(newValue);
                headerLabel.setText(defaultText(newValue, localizationService.get("parentEditor.header.chapter")));
            }else if(currentParent instanceof Subtask){
                ((Subtask) currentParent).setTitle(newValue);
                headerLabel.setText(defaultText(newValue, localizationService.get("parentEditor.header.subtask")));
            }
        });

        pointsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (createMode) {
                return;
            }
            if(!(currentParent instanceof Subtask)){
                return;
            }

            try{
                int parsed = newValue == null || newValue.isBlank() ? 0 : Integer.parseInt(newValue);
                ((Subtask) currentParent).setPoints(parsed);
            }catch(NumberFormatException ignored){
                //keep previos value, when input is not a valid integer
                pointsField.setText(oldValue);
            }
        });

        labelsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (createMode) {
                return;
            }
            if (!(currentParent instanceof Subtask)) {
                return;
            }
            ((Subtask) currentParent).setLabels(parseLabels(newValue));
        });

        displayPlaceholder();
        localizationService.localeProperty().addListener((obs, oldLocale, newLocale) -> applyTranslations());
        applyTranslations();
    }

    public void displayParent(ParentObject<? extends ChildObject> parent){
        this.currentParent = parent;
        if(parent == null){
            displayPlaceholder();
            return;
        }

        if(parent instanceof Chapter){
            displayChapter((Chapter) parent);
        }else if(parent instanceof Subtask){
            displaySubtask((Subtask) parent);
        }else{
            displayGeneric(parent);
        }
    }

    public void setSelectionHandler(Consumer<ChildObject> selectionHandler){
        this.selectionHandler = selectionHandler;
    }

    public void setDataChangedHandler(Runnable dataChangedHandler) {
        this.dataChangedHandler = dataChangedHandler;
    }

    private void renderChildren(List<? extends ChildObject> children){
        childList.getChildren().clear();

        if(children == null || children.isEmpty()){
            childList.getChildren().add(createEmptyRow());
            return;
        }


        for(ChildObject child : children){
            childList.getChildren().add(createChildRow(child));
        }
    }

    private Node createChildRow(ChildObject child){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/Editor_ChildRow.fxml"));
        try{
            Node node = loader.load();
            EditorChildRowController controller = loader.getController();
            controller.configure(child, selectionHandler);
            return node;
        }catch (IOException e){
            throw new IllegalStateException("Unabel to load editor child row", e);
        }
    }

    private Node createEmptyRow(){
        Label placeholder = new Label(localizationService.get("editor.noEntries"));
        placeholder.getStyleClass().add("subtask-item");
        return placeholder;
    }

    private void displayPlaceholder(){
        createMode = false;
        headerLabel.setText(localizationService.get("editor.placeholder"));
        titleField.clear();
        pointsField.clear();
        pointsBox.setVisible(false);
        pointsBox.setManaged(false);
        toggleLabels(false);
        labelsField.clear();
        childList.getChildren().setAll(createEmptyRow());
        currentParent = null;
        updateActionButtons();
    }

    private String defaultText(String value, String fallback){
        return value == null ? fallback : value;
    }

    private void displayChapter(Chapter chapter){
        createMode = false;
        headerLabel.setText(defaultText(chapter.getTitle(), localizationService.get("parentEditor.header.chapter")));
        titleField.setText(defaultText(chapter.getTitle(), ""));
        togglePoints(false);
        toggleLabels(false);
        childSectionLabel.setText(localizationService.get("parentEditor.childSection.subtasks"));
        renderChildren(chapter.getChildElements());
        updateActionButtons();
    }

    private void displaySubtask(Subtask subtask){
        createMode = false;
        headerLabel.setText(defaultText(subtask.getTitle(), localizationService.get("parentEditor.header.subtask")));
        titleField.setText(defaultText(subtask.getTitle(), ""));
        togglePoints(true);
        toggleLabels(true);
        pointsField.setText(String.valueOf(subtask.getPoints()));
        labelsField.setText(String.join(", ", defaultLabels(subtask.getLabels())));
        childSectionLabel.setText(localizationService.get("parentEditor.childSection.variants"));
        renderChildren(subtask.getChildElements());
        updateActionButtons();
    }

    private void displayGeneric(ParentObject<? extends ChildObject> parent){
        createMode = false;
        headerLabel.setText(defaultText(parent.getTitle(), localizationService.get("parentEditor.header.generic")));
        titleField.setText(defaultText(parent.getTitle(), ""));
        togglePoints(false);
        toggleLabels(false);
        childSectionLabel.setText(localizationService.get("parentEditor.childSection.generic"));
        renderChildren(parent.getChildElements());
        updateActionButtons();
    }

    private void togglePoints(boolean visible){
        pointsBox.setVisible(visible);
        pointsBox.setManaged(visible);
    }

    private void toggleLabels(boolean visible){
        labelsBox.setVisible(visible);
        labelsBox.setManaged(visible);
    }

    private List<String> parseLabels(String input){
        if(input == null || input.isBlank()){
            return List.of();
        }

        return List.of(input.split(","))
                .stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private List<String> defaultLabels(List<String> labels){
        return labels == null ? List.of() : labels;
    }

    private void applyTranslations() {
        titleLabel.setText(localizationService.get("parentEditor.title"));
        titleField.setPromptText(localizationService.get("parentEditor.title.prompt"));
        pointsLabel.setText(localizationService.get("parentEditor.points"));
        pointsField.setPromptText(localizationService.get("parentEditor.points.prompt"));
        labelsLabel.setText(localizationService.get("parentEditor.labels"));
        labelsField.setPromptText(localizationService.get("parentEditor.labels.prompt"));
        deleteButton.setText(localizationService.get("editor.delete"));
        saveButton.setText(localizationService.get("editor.save"));
        addChild.setText("Create child");
        createButton.setText("Create");

        if (currentParent == null) {
            displayPlaceholder();
        } else if (currentParent instanceof Chapter) {
            displayChapter((Chapter) currentParent);
        } else if (currentParent instanceof Subtask) {
            displaySubtask((Subtask) currentParent);
        } else {
            displayGeneric(currentParent);
        }
    }

    private void updateActionButtons() {
        boolean hasParent = currentParent != null;
        addChild.setVisible(hasParent);
        addChild.setManaged(hasParent);

        createButton.setVisible(createMode);
        createButton.setManaged(createMode);
        saveButton.setVisible(!createMode);
        saveButton.setManaged(!createMode);
        deleteButton.setVisible(!createMode);
        deleteButton.setManaged(!createMode);
    }

    private void notifyDataChanged() {
        if (dataChangedHandler != null) {
            dataChangedHandler.run();
        }
    }

    private int nextSubtaskId() {
        return subtaskService.getAll().stream()
                .mapToInt(Subtask::getId)
                .max()
                .orElse(0) + 1;
    }

    private int nextVariantId() {
        return ApplicationContext.getInstance().getVariantService().getAll().stream()
                .mapToInt(Variant::getId)
                .max()
                .orElse(0) + 1;
    }

    private int parsePointsInput() {
        String value = pointsField.getText();
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    public void toggleAddNewChild(){
        if (currentParent == null) {
            return;
        }

        createMode = true;
        titleField.clear();
        pointsField.clear();
        labelsField.clear();

        if (currentParent instanceof Chapter) {
            headerLabel.setText("Create new subtask");
            togglePoints(true);
            toggleLabels(true);
        } else if (currentParent instanceof Subtask) {
            headerLabel.setText("Create new variant");
            togglePoints(false);
            toggleLabels(false);
        } else {
            headerLabel.setText(localizationService.get("parentEditor.header.generic"));
            togglePoints(false);
            toggleLabels(false);
        }

        updateActionButtons();
    }

    @FXML
    private void handleSave(){
        if(currentParent == null){
            return;
        }

        if(currentParent instanceof Chapter chapter){
            ChapterServiceImpl.ChapterCommand command = new ChapterServiceImpl.ChapterCommand() {
                @Override
                public String title() {
                    return titleField.getText();
                }

                @Override
                public Integer parentId() {
                    return null;
                }
            };
            currentParent = chapterService.update(chapter.getId(), command);
            displayParent(currentParent);
            notifyDataChanged();
            return;
        }

        if(currentParent instanceof Subtask subtask){
            SubtaskServiceImpl.SubtaskCommand command = new SubtaskServiceImpl.SubtaskCommand() {
                @Override
                public String title() {
                    return titleField.getText();
                }

                @Override
                public int points() {
                    String value = pointsField.getText();
                    return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
                }

                @Override
                public List<String> labels() {
                    return parseLabels(labelsField.getText());
                }

                @Override
                public Integer parentId() {
                    return subtask.getChapterId();
                }
            };

            currentParent = subtaskService.update(subtask.getId(), command);
            displayParent(currentParent);
            notifyDataChanged();
        }
    }

    @FXML
    private void handleCreate() {
        if (currentParent == null || !createMode) {
            return;
        }

        if (currentParent instanceof Chapter chapter) {
            Subtask createdSubtask = new Subtask();
            createdSubtask.setId(nextSubtaskId());
            createdSubtask.setChapterId(chapter.getId());
            createdSubtask.setTitle(titleField.getText());
            createdSubtask.setPoints(parsePointsInput());
            createdSubtask.setLabels(new ArrayList<>(parseLabels(labelsField.getText())));

            Chapter updatedChapter = chapterService.getById(chapter.getId());
            List<Subtask> children = new ArrayList<>(updatedChapter.getChildElements());
            children.add(createdSubtask);
            updatedChapter.setChildElements(children);
            ApplicationContext.getInstance().getChapterRepository().update(updatedChapter);

            notifyDataChanged();
            if (selectionHandler != null) {
                selectionHandler.accept(createdSubtask);
            } else {
                displayParent(updatedChapter);
            }
            return;
        }

        if (currentParent instanceof Subtask subtask) {
            Variant createdVariant = new Variant();
            createdVariant.setId(nextVariantId());
            createdVariant.setTitle(titleField.getText());
            createdVariant.setQuestion(titleField.getText());
            createdVariant.setSolution("");

            Subtask updatedSubtask = subtaskService.getById(subtask.getId());
            List<Variant> children = new ArrayList<>(updatedSubtask.getChildElements());
            children.add(createdVariant);
            updatedSubtask.setChildElements(children);
            ApplicationContext.getInstance().getSubtaskRepository().update(updatedSubtask);

            notifyDataChanged();
            if (selectionHandler != null) {
                selectionHandler.accept(createdVariant);
            } else {
                displayParent(updatedSubtask);
            }
        }
    }
}

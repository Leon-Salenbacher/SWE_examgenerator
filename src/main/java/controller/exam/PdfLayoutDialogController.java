package controller.exam;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.exam.dto.PdfLayoutSettings;
import service.impl.LocalizationService;

public class PdfLayoutDialogController {

    @FXML
    private CheckBox coverPageCheckBox;
    @FXML
    private VBox coverFieldsBox;
    @FXML
    private Label coverTitleLabel;
    @FXML
    private TextField coverTitleField;
    @FXML
    private Label coverSubtitleLabel;
    @FXML
    private TextField coverSubtitleField;
    @FXML
    private Label headerLabel;
    @FXML
    private TextField headerField;
    @FXML
    private Label footerLabel;
    @FXML
    private TextField footerField;
    @FXML
    private CheckBox pageNumbersCheckBox;
    @FXML
    private Label answerBoxHeightPerPointLabel;
    @FXML
    private TextField answerBoxHeightPerPointField;
    @FXML
    private Label helperLabel;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    private final LocalizationService localizationService = LocalizationService.getInstance();
    private Stage dialogStage;
    private String examTitle;
    private PdfLayoutSettings result;

    @FXML
    private void initialize() {
        applyTranslations();
        coverPageCheckBox.selectedProperty().addListener((obs, oldValue, newValue) -> updateCoverState());
        localizationService.localeProperty().addListener((obs, oldLocale, newLocale) -> applyTranslations());
    }

    public void configure(Stage dialogStage, String examTitle, PdfLayoutSettings currentSettings) {
        this.dialogStage = dialogStage;
        this.examTitle = examTitle;

        PdfLayoutSettings baseSettings = (currentSettings == null
                ? PdfLayoutSettings.defaults(examTitle)
                : currentSettings).sanitize(examTitle);
        this.result = baseSettings;

        coverPageCheckBox.setSelected(baseSettings.coverPageEnabled());
        coverTitleField.setText(baseSettings.coverTitle());
        coverSubtitleField.setText(baseSettings.coverSubtitle());
        headerField.setText(baseSettings.headerText());
        footerField.setText(baseSettings.footerText());
        pageNumbersCheckBox.setSelected(baseSettings.pageNumbersEnabled());
        answerBoxHeightPerPointField.setText(Integer.toString(baseSettings.answerBoxHeightPerPoint()));
        updateCoverState();
    }

    public PdfLayoutSettings getResult() {
        return result;
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    @FXML
    private void handleSave() {
        result = new PdfLayoutSettings(
                coverPageCheckBox.isSelected(),
                coverTitleField.getText(),
                coverSubtitleField.getText(),
                headerField.getText(),
                footerField.getText(),
                pageNumbersCheckBox.isSelected(),
                readAnswerBoxHeightPerPoint()
        ).sanitize(examTitle);
        dialogStage.close();
    }

    private int readAnswerBoxHeightPerPoint() {
        String value = answerBoxHeightPerPointField.getText();
        try {
            return PdfLayoutSettings.sanitizeAnswerBoxHeightPerPoint(Integer.parseInt(value.trim()));
        } catch (NullPointerException | NumberFormatException exception) {
            return result == null
                    ? PdfLayoutSettings.DEFAULT_ANSWER_BOX_HEIGHT_PER_POINT
                    : result.answerBoxHeightPerPoint();
        }
    }

    private void updateCoverState() {
        boolean enabled = coverPageCheckBox.isSelected();
        coverFieldsBox.setDisable(!enabled);
    }

    private void applyTranslations() {
        coverPageCheckBox.setText(localizationService.get("layout.dialog.coverPage"));
        coverTitleLabel.setText(localizationService.get("layout.dialog.coverTitle"));
        coverTitleField.setPromptText(localizationService.get("layout.dialog.coverTitle.prompt"));
        coverSubtitleLabel.setText(localizationService.get("layout.dialog.coverSubtitle"));
        coverSubtitleField.setPromptText(localizationService.get("layout.dialog.coverSubtitle.prompt"));
        headerLabel.setText(localizationService.get("layout.dialog.header"));
        headerField.setPromptText(localizationService.get("layout.dialog.header.prompt"));
        footerLabel.setText(localizationService.get("layout.dialog.footer"));
        footerField.setPromptText(localizationService.get("layout.dialog.footer.prompt"));
        pageNumbersCheckBox.setText(localizationService.get("layout.dialog.pageNumbers"));
        answerBoxHeightPerPointLabel.setText(localizationService.get("layout.dialog.answerBoxHeightPerPoint"));
        answerBoxHeightPerPointField.setPromptText(localizationService.get("layout.dialog.answerBoxHeightPerPoint.prompt"));
        helperLabel.setText(localizationService.get("layout.dialog.helper"));
        cancelButton.setText(localizationService.get("layout.dialog.cancel"));
        saveButton.setText(localizationService.get("layout.dialog.save"));
        if (dialogStage != null) {
            dialogStage.setTitle(localizationService.get("layout.dialog.title"));
        }
    }
}

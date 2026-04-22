package controller.editor;

import models.ChildObject;

public record EditorFeedbackRequest(ChildObject data, String message, boolean success) {
}

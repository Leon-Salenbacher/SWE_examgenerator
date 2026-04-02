package controller.editor;

import objects.ChildObject;

public record EditorFeedbackRequest(ChildObject data, String message, boolean success) {
}

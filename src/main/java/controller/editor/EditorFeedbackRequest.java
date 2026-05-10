package controller.editor;

import models.ChildObject;

/**
 * Request to display transient editor feedback after navigation or refresh.
 *
 * @param data target object that should receive the feedback
 * @param message feedback text to display
 * @param success whether the feedback represents a successful action
 */
public record EditorFeedbackRequest(ChildObject data, String message, boolean success) {
}

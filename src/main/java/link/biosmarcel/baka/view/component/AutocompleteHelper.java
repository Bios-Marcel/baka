package link.biosmarcel.baka.view.component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tooltip;
import javafx.stage.PopupWindow;

public final class AutocompleteHelper {
    private AutocompleteHelper() {
    }

    public static void installErrorToolTip(final AutocompleteInput input,
                                           final StringProperty error,
                                           final BooleanProperty fatal) {
        Tooltip errorTooltip = new Tooltip("");
        errorTooltip.textProperty().bind(error);
        errorTooltip.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_TOP_RIGHT);
        input.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                errorTooltip.hide();
                return;
            }

            if (!errorTooltip.getText().isEmpty()) {
                final var bounds = input.getNode().localToScreen(input.getNode().getLayoutBounds());
                errorTooltip.show(input.getNode(), bounds.getMinX(), bounds.getMinY());
            }
        });

        error.addListener((_, _, newText) -> {
            if (newText.isEmpty()) {
                errorTooltip.hide();
            } else {
                final var bounds = input.getNode().localToScreen(input.getNode().getLayoutBounds());
                errorTooltip.show(input.getNode(), bounds.getMinX(), bounds.getMinY());
            }
        });

        fatal.addListener((_, _, newValue) -> {
            if (newValue) {
                input.indicateError();
            } else {
                input.clearError();
            }
        });
    }
}

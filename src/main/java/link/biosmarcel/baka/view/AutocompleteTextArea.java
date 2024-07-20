package link.biosmarcel.baka.view;

import javafx.geometry.Point2D;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.skin.TextAreaSkin;

import java.util.List;
import java.util.function.Function;

public class AutocompleteTextArea extends AutocompleteInput {
    public AutocompleteTextArea(Function<String, List<String>> autocompleteGenerator) {
        super(autocompleteGenerator);
    }

    @Override
    Point2D computePopupLocation() {
        // Required, as the bounds will be outdated otherwise.
        input.layout();

        final var textFieldBounds = input.getBoundsInParent();
        final var bounds = ((TextAreaSkin) input.getSkin()).getCaretBounds();
        return new Point2D(textFieldBounds.getMinX() + bounds.getMinX(), textFieldBounds.getMinY() + bounds.getMaxY());
    }

    @Override
    TextInputControl createInput() {
        return new TextArea();
    }
}

package link.biosmarcel.baka.view;

import javafx.geometry.Point2D;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.skin.TextFieldSkin;

import java.util.List;
import java.util.function.Function;

public class AutocompleteField extends AutocompleteInput {
    public AutocompleteField(Function<String, List<String>> autocompleteGenerator) {
        super(autocompleteGenerator);
    }

    @Override
    Point2D computePopupLocation() {
        final var textFieldBounds = input.getBoundsInParent();
        final var caretBounds = ((TextFieldSkin) input.getSkin()).getCharacterBounds(input.getCaretPosition());
        return new Point2D(textFieldBounds.getMinX() + caretBounds.getMinX(), textFieldBounds.getMaxY());
    }

    @Override
    TextInputControl createInput() {
        return new TextField();
    }
}

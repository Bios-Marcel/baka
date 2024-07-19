package link.biosmarcel.baka.view;

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
    void positionPopup() {
        final var textFieldBounds = input.getBoundsInParent();
        // Required, as the bounds will be outdated otherwise.
        input.layout();
        final var bounds = ((TextAreaSkin) input.getSkin()).getCaretBounds();
        completionList.setTranslateX(textFieldBounds.getMinX() + bounds.getMinX());
        completionList.setTranslateY(textFieldBounds.getMinY() + bounds.getMaxY());
    }

    @Override
    TextInputControl createInput() {
        return new TextArea();
    }
}

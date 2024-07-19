package link.biosmarcel.baka.view;

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
    void positionPopup() {
        final var textFieldBounds = input.getBoundsInParent();
        completionList.setTranslateY(textFieldBounds.getMaxY());

        final var caretBounds = ((TextFieldSkin) input.getSkin()).getCharacterBounds(input.getCaretPosition());
        completionList.setTranslateX(textFieldBounds.getMinX() + caretBounds.getMinX());
    }

    @Override
    TextInputControl createInput() {
        return new TextField();
    }
}

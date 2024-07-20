package link.biosmarcel.baka.view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public abstract class AutocompleteInput {
    private final Pane pane;
    public final TextInputControl input;
    protected final ListView<String> completionList;

    public AutocompleteInput(Function<String, List<String>> autocompleteGenerator) {
        pane = new Pane();
        input = createInput();
        completionList = new ListView<>();

        // This is what is the Z-Index on the web. It allows us to render our popup above everything else.
        pane.getChildren().add(input);
        pane.getChildren().add(completionList);
        pane.maxHeightProperty().bind(input.heightProperty());
        pane.maxWidthProperty().bind(input.widthProperty());

        completionList.setFixedCellSize(35.0);
        completionList.setMaxHeight(8 * completionList.getFixedCellSize());
        completionList.setBackground(Background.fill(Color.WHITE));
        completionList.setFocusTraversable(false);
        completionList.setVisible(false);
        completionList.setCellFactory(_ -> {
            final ListCell<@Nullable String> cell = new ListCell<>() {
                @Override
                protected void updateItem(final @Nullable String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (!empty && item != null) {
                        setText(item);
                    } else {
                        setText("");
                    }
                }
            };
            cell.setOnMouseClicked(_ -> complete());
            return cell;
        });

        input.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            // FIXME Ctrl+Home/End
            if (event.getCode() == KeyCode.UP) {
                event.consume();
                if (completionList.getSelectionModel().getSelectedIndex() != 0) {
                    completionList.getSelectionModel().select(completionList.getSelectionModel().getSelectedIndex() + -1);
                }
            } else if (event.getCode() == KeyCode.DOWN) {
                event.consume();
                completionList.getSelectionModel().select(completionList.getSelectionModel().getSelectedIndex() - -1);
            } else if (event.getCode() == KeyCode.ENTER) {
                if (event.isShiftDown()) {
                    input.insertText(input.getCaretPosition(), "\n");
                } else if (completionList.isVisible()) {
                    event.consume();
                    complete();
                }
            }
        });

        input.focusedProperty().addListener((_, _, _) -> {
            if (!canShowPopup()) {
                hidePopup();
                return;
            }

            updatePopupItems(autocompleteGenerator.apply(input.getText().substring(0, input.getCaretPosition())));
            refreshPopup();
        });

        input.caretPositionProperty().addListener((_, _, newValue) -> {
            if (!canShowPopup()) {
                return;
            }

            updatePopupItems(autocompleteGenerator.apply(input.getText().substring(0, newValue.intValue())));
            refreshPopup();
        });
    }

    private boolean canShowPopup() {
        return input.isFocused() && !input.isDisabled();
    }

    private static final char[] autocompleteAfterChars = new char[]{')', '(', ' ', '\n'};

    private void complete() {
        final String selectedItem = completionList.getSelectionModel().getSelectedItem();
        // selection is always nullable
        //noinspection ConstantValue
        if (selectedItem != null) {
            final var textBeforeCaret = input.getText().substring(0, input.getCaretPosition());

            // FIXME Properly preserve newlines?
            // FIXME Autocomplete over selection?

            int autocompleteTo = -1;
            for (final char c : autocompleteAfterChars) {
                autocompleteTo = Integer.max(textBeforeCaret.lastIndexOf(c), autocompleteTo);
            }
            final var preCompletionText = textBeforeCaret.substring(0, autocompleteTo + 1);

            // Make sure that we have a space after either open or closed parenthesis.
            String textToInsert = selectedItem + " ";
            if (autocompleteTo != -1 && !Character.isWhitespace(textBeforeCaret.charAt(autocompleteTo))) {
                textToInsert = " " + textToInsert;
            }

            input.setText(preCompletionText + textToInsert + input.getText().substring(input.getCaretPosition()));
            // We add a space at the end, so we can start writing / completing the next token type right away.
            input.positionCaret(preCompletionText.length() + selectedItem.length() + 1);
        }
    }

    private void updatePopupItems(final Collection<String> newItems) {
        completionList.getItems().removeIf(item -> !newItems.contains(item));
        for (final String newItem : newItems) {
            if (!completionList.getItems().contains(newItem)) {
                completionList.getItems().add(newItem);
            }
        }
        completionList.getItems().sort(String::compareTo);
    }

    public void hidePopup() {
        completionList.setVisible(false);
    }

    public void refreshPopup() {
        if (completionList.getItems().isEmpty()) {
            hidePopup();
            return;
        }

        // +2 to prevent an unnecessary scrollbar
        if (completionList.getSelectionModel().getSelectedIndex() == -1) {
            completionList.getSelectionModel().select(0);
        }

        completionList.setPrefHeight(completionList.getItems().size() * completionList.getFixedCellSize() + 2);
        positionPopup();
        toFront(completionList);

        completionList.setVisible(true);
    }

    public StringProperty textProperty() {
        return input.textProperty();
    }

    public BooleanProperty disableProperty() {
        return input.disableProperty();
    }

    private void toFront(Node node) {
        node.setViewOrder(-1);
        if (node.getParent() != null) {
            toFront(node.getParent());
        }
    }

    public Node getNode() {
        return pane;
    }

    abstract void positionPopup();

    abstract TextInputControl createInput();
}

package link.biosmarcel.baka.view;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import link.biosmarcel.baka.view.component.AutocompleteField;
import link.biosmarcel.baka.view.model.ClassificationFX;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.function.UnaryOperator;

public class ClassificationCell extends ListCell<@Nullable ClassificationFX> {
    private static final DecimalFormat CURRENCY_FORMAT;

    static {
        CURRENCY_FORMAT = (DecimalFormat) DecimalFormat.getNumberInstance();
        CURRENCY_FORMAT.setParseBigDecimal(true);
    }

    private static final StringConverter<BigDecimal> CONVERTER = new StringConverter<>() {
        @Override
        public @Nullable BigDecimal fromString(@Nullable String value) {
            // If the specified value is null or zero-length, return null
            if (value == null) {
                return BigDecimal.ZERO;
            }

            value = value.strip();
            if (value.isEmpty()) {
                return BigDecimal.ZERO;
            }

            try {
                return (BigDecimal) CURRENCY_FORMAT.parse(value);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString(final @Nullable BigDecimal value) {
            // If the specified value is null, return a zero-length String
            if (value == null) {
                return "";
            }

            return CURRENCY_FORMAT.format(value);
        }
    };

    private static final UnaryOperator<TextFormatter.@Nullable Change> FILTER = change -> {
        if (change == null || !change.isContentChange()) {
            return change;
        }

        if (change.getText().chars().allMatch(value -> {
            return Character.isDigit(value)
                    || CURRENCY_FORMAT.getDecimalFormatSymbols().getDecimalSeparator() == value
                    || CURRENCY_FORMAT.getDecimalFormatSymbols().getGroupingSeparator() == value
                    || '-' == value;
        })) {
            return change;
        }

        return null;
    };

    private final Node renderer;
    private final AutocompleteField tagField;
    private final TextField amountField;
    private @Nullable ClassificationFX lastItem;

    public ClassificationCell(
            final TagCompletion tagCompletion
    ) {
        amountField = new TextField();

        final TextFormatter<BigDecimal> formatter = new TextFormatter<>(CONVERTER, BigDecimal.ZERO, FILTER);
        amountField.setTextFormatter(formatter);
        amountField.textProperty().addListener((_, _, newValue) -> {
            if (newValue.isEmpty()) {
                return;
            }

            try {
                amountField.getTextFormatter().getValueConverter().fromString(newValue);
                amountField.getStyleClass().remove("text-field-error");
            } catch (final Exception exception) {
                if (!amountField.getStyleClass().contains("text-field-error")) {
                    amountField.getStyleClass().add("text-field-error");
                }
            }
        });
        amountField.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                amountField.commitValue();
            }
        });

        tagField = new AutocompleteField(new char[]{' '}, tagCompletion::match);
        tagField.setInsertSpaceAfterCompletion(false);
        tagField.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                tagField.textProperty().set(tagField.textProperty().get().strip());
            }
        });

        final var amountLabel = new Label("Amount:");
        final var tagLabel = new Label("Tag:");
        renderer = new HBox(5.0, tagLabel, tagField.getNode(), amountLabel, amountField);
        amountLabel.setMaxHeight(Double.MAX_VALUE);
        tagLabel.setMaxHeight(Double.MAX_VALUE);
        setText(null);
    }

    @Override
    protected void updateItem(final @Nullable ClassificationFX item, final boolean empty) {
        super.updateItem(item, empty);

        if (lastItem != null) {
            ((ObjectProperty<BigDecimal>) amountField.getTextFormatter().valueProperty()).unbindBidirectional(lastItem.amount);
            tagField.textProperty().unbindBidirectional(lastItem.tag);
            lastItem = null;
        }

        if (empty || item == null) {
            setGraphic(null);
            return;
        }

        ((ObjectProperty<BigDecimal>) amountField.getTextFormatter().valueProperty()).bindBidirectional(item.amount);
        tagField.textProperty().bindBidirectional(item.tag);

        setGraphic(renderer);

        lastItem = item;
    }
}


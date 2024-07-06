package link.biosmarcel.baka.view;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import link.biosmarcel.baka.ApplicationState;
import link.biosmarcel.baka.data.Classification;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;

public class PaymentDetails extends VBox {
    public final ObjectProperty<@Nullable PaymentFX> activePayment = new SimpleObjectProperty<>();
    public final BooleanProperty disableComponents = new SimpleBooleanProperty(true);
    private final BooleanBinding disableDelete; // Do not inline, it will get garbage collected!
    private final ApplicationState state;

    public PaymentDetails(final ApplicationState state) {
        this.state = state;

        final TableView<Classification> classificationsTable = new TableView<>();
        classificationsTable.setEditable(true);

        final TableColumn<Classification, BigDecimal> amountColumn = new TableColumn<>("Amount");
        final Callback<TableColumn<Classification, @Nullable BigDecimal>, TableCell<Classification, @Nullable BigDecimal>>
                amountColumnCellFactory = _ ->
                new TextFieldTableCell<>(new StringConverter<BigDecimal>() {
                    @Override
                    public @Nullable String toString(final @Nullable BigDecimal value) {
                        if (value == null) {
                            return null;
                        }
                        return value.toString();
                    }

                    @Override
                    public @Nullable BigDecimal fromString(final @Nullable String string) {
                        if (string == null || string.isBlank()) {
                            return null;
                        }
                        return new BigDecimal(string);
                    }
                });
        amountColumn.setCellFactory(amountColumnCellFactory);
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        amountColumn.setOnEditCommit(event -> {
            event.getRowValue().amount = event.getNewValue();
            state.storer.store(event.getRowValue());
            state.storer.commit();
        });

        final TableColumn<Classification, String> tagColumn = new TableColumn<>("Tag");
        final Callback<TableColumn<Classification, @Nullable String>, TableCell<Classification, @Nullable String>>
                simpleStringColumnFactory = _ ->
                new TextFieldTableCell<>(new StringConverter<String>() {
                    @Override
                    public @Nullable String toString(final @Nullable String string) {
                        return string;
                    }

                    @Override
                    public @Nullable String fromString(final @Nullable String string) {
                        return string;
                    }
                });
        tagColumn.setCellFactory(simpleStringColumnFactory);
        tagColumn.setCellValueFactory(new PropertyValueFactory<>("tag"));
        tagColumn.setOnEditCommit(event -> {
            event.getRowValue().tag = event.getNewValue();
            final var payment = Objects.requireNonNull(activePayment.get());
            state.storer.store(event.getRowValue());

            // HACK Small trick to make sure we rerender the table cell. Not quite the best way, but it'll do for now.
            final var old = new ArrayList<>(payment.classifications);
            payment.classifications.clear();
            payment.classifications.setAll(old);

            state.storer.commit();
        });

        classificationsTable.getColumns().addAll(
                amountColumn,
                tagColumn
        );

        classificationsTable.disableProperty().bind(disableComponents);

        activePayment.addListener((_, _, newValue) -> {
            if (newValue == null) {
                disableComponents.set(true);
                classificationsTable.setItems(FXCollections.emptyObservableList());
            } else {
                disableComponents.set(false);
                classificationsTable.setItems(newValue.classifications);
            }
        });


        final var createButton = new Button("New");
        createButton.disableProperty().bind(disableComponents);
        createButton.setOnAction(_ -> {
            final var payment = Objects.requireNonNull(activePayment.get());

            final Classification newClassification = new Classification();
            // We default to the full amount, assuming one payment is USUALLY one thing.
            newClassification.amount = payment.amount.get().abs();

            payment.payment.classifications.add(newClassification);
            payment.classifications.add(newClassification);

            state.storer.store(payment.payment.classifications);
            state.storer.commit();
        });

        final var deleteButton = new Button("Delete");
        final ReadOnlyObjectProperty<@Nullable Classification> selectedClassifiction =
                classificationsTable.getSelectionModel().selectedItemProperty();
        disableDelete = Bindings.createBooleanBinding(
                () -> disableComponents.get() || selectedClassifiction.get() == null,
                disableComponents, selectedClassifiction);
        deleteButton.disableProperty().bind(disableDelete);
        final ReadOnlyObjectProperty<@Nullable Classification> selectedClassification =
                classificationsTable.getSelectionModel().selectedItemProperty();
        deleteButton.setOnAction(_ -> {
            final var selected = Objects.requireNonNull(selectedClassification.get());
            final var payment = Objects.requireNonNull(activePayment.get());

            payment.classifications.remove(selected);
            payment.payment.classifications.remove(selected);
            state.storer.store(payment.payment);
            state.storer.commit();
        });

        final var buttons = new HBox(
                createButton,
                deleteButton
        );
        buttons.setSpacing(5.0);

        setSpacing(5.0);
        getChildren().addAll(
                buttons,
                classificationsTable
        );
    }
}

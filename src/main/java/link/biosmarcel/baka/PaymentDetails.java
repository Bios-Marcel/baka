package link.biosmarcel.baka;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;

public class PaymentDetails extends VBox {
    public final ObjectProperty<PaymentFX> activePayment = new SimpleObjectProperty<>();
    public final BooleanProperty disableComponents = new SimpleBooleanProperty(true);
    private final State state;

    public PaymentDetails(final State state) {
        this.state = state;

        final TableView<Classification> classificationsTable = new TableView<>();

        final TableColumn<Classification, BigDecimal> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        final TableColumn<Classification, String> tagColumn = new TableColumn<>("Tag");
        tagColumn.setCellValueFactory(new PropertyValueFactory<>("tag"));

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
            final Classification newClassification = new Classification();
            newClassification.amount = new BigDecimal("5.0");
            newClassification.tag = "test";

            activePayment.get().payment.classifications.add(newClassification);
            activePayment.get().classifications.add(newClassification);

            state.storer.store(activePayment.get().payment.classifications);
            state.storer.commit();
        });

        final var deleteButton = new Button("Delete");
        classificationsTable.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            deleteButton.setDisable(newValue == null);
        });
        deleteButton.setOnAction(_ -> {
            final var selected = classificationsTable.getSelectionModel().selectedItemProperty().get();
            activePayment.get().classifications.remove(selected);
            activePayment.get().payment.classifications.remove(selected);
            state.storer.store(activePayment.get().payment);
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

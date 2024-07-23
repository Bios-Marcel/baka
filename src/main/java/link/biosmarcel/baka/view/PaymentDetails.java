package link.biosmarcel.baka.view;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Cell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import link.biosmarcel.baka.ApplicationState;
import link.biosmarcel.baka.data.Classification;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class PaymentDetails extends VBox {
    public final ObjectProperty<@Nullable PaymentFX> activePayment = new SimpleObjectProperty<>();
    public final BooleanProperty disableComponents = new SimpleBooleanProperty(true);
    private final ApplicationState state;

    public PaymentDetails(final ApplicationState state) {
        this.state = state;

        final var classificationsList = new ListView<ClassificationFX>();
        classificationsList.disableProperty().bind(disableComponents);
        classificationsList.setCellFactory(_ -> new ClassificationCell());

        activePayment.addListener((_, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.classifications.forEach(ClassificationFX::apply);
            }

            if (newValue == null) {
                classificationsList.setItems(FXCollections.emptyObservableList());
                disableComponents.set(true);
            } else {
                disableComponents.set(false);
                classificationsList.setItems(newValue.classifications);
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
            final ClassificationFX newClassificationFX = new ClassificationFX(newClassification);
            payment.classifications.add(newClassificationFX);

            classificationsList.getSelectionModel().select(newClassificationFX);
            classificationsList.layout();
            classificationsList
                    .lookupAll(".cell")
                    .stream()
                    .map(node -> (ClassificationCell) node)
                    .filter(Cell::isSelected)
                    .findFirst()
                    .ifPresent(ClassificationCell::requestFocus);
        });

        final var deleteButton = new Button("Delete");
        final ReadOnlyObjectProperty<@Nullable ClassificationFX> selectedClassification =
                classificationsList.getSelectionModel().selectedItemProperty();
        final var disableDelete = Bindings.createBooleanBinding(
                () -> disableComponents.get() || selectedClassification.get() == null,
                disableComponents, selectedClassification);
        deleteButton.disableProperty().bind(disableDelete);
        deleteButton.setOnAction(_ -> {
            final var selected = Objects.requireNonNull(selectedClassification.get());
            final var payment = Objects.requireNonNull(activePayment.get());

            payment.classifications.remove(selected);
            payment.payment.classifications.remove(selected.classification);
        });

        final var buttons = new HBox(
                createButton,
                deleteButton
        );
        buttons.setSpacing(5.0);

        classificationsList.setMaxWidth(Double.MAX_VALUE);
        setSpacing(5.0);
        getChildren().addAll(
                buttons,
                classificationsList
        );
    }

    public void save() {
        final var selected = activePayment.get();
        if (selected != null) {
            selected.classifications.forEach(ClassificationFX::apply);
        }
    }
}

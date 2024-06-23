package link.biosmarcel.baka;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import link.biosmarcel.baka.bankimport.RevolutCSV;
import link.biosmarcel.baka.bankimport.SparkasseCSV;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PaymentsView extends Tab {
    private final State state;

    private final TableView<PaymentFX> table;

    public PaymentsView(State state) {
        super("Payments");

        this.state = state;

        this.table = new TableView<>();

        final TableColumn<PaymentFX, BigDecimal> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cell -> cell.getValue().amount);

        final TableColumn<PaymentFX, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cell -> cell.getValue().name);

        final TableColumn<PaymentFX, String> referenceColumn = new TableColumn<>("Reference");
        referenceColumn.setCellValueFactory(cell -> cell.getValue().reference);

        final TableColumn<PaymentFX, LocalDate> bookingDateColumn = new TableColumn<>("Booking Date");
        bookingDateColumn.setCellValueFactory(cell -> cell.getValue().bookingDate);

        final TableColumn<PaymentFX, LocalDate> effectiveDateColumn = new TableColumn<>("Effective Date");
        effectiveDateColumn.setCellValueFactory(cell -> cell.getValue().effectiveDate);
        effectiveDateColumn.setSortType(TableColumn.SortType.DESCENDING);

        table.getColumns().addAll(
                amountColumn,
                nameColumn,
                referenceColumn,
                bookingDateColumn,
                effectiveDateColumn
        );
        table.getSortOrder().add(effectiveDateColumn);
        table.getItems().setAll(convertPayments(state.data.payments));
        table.sort();

        final var menuItemSparkasseCSVCamtV8 = new MenuItem("SparkasseCAMTv8.csv");
        final var menuItemRevolutCSV = new MenuItem("Revolut.csv");

        final var importButton = new MenuButton("Import");
        importButton.getItems().addAll(
                menuItemSparkasseCSVCamtV8,
                menuItemRevolutCSV
        );

        menuItemRevolutCSV.setOnAction(_ -> {
            final var file = new FileChooser().showOpenDialog(getTabPane().getScene().getWindow());
            if (file == null) {
                return;
            }

            createPayments(RevolutCSV.parse(file));
        });
        menuItemSparkasseCSVCamtV8.setOnAction(_ -> {
            final var file = new FileChooser().showOpenDialog(getTabPane().getScene().getWindow());
            if (file == null) {
                return;
            }

            createPayments(SparkasseCSV.parse(file));
        });

        PaymentDetails details = new PaymentDetails(state);
        details.activePayment.bind(table.getSelectionModel().selectedItemProperty());

        final var layout = new VBox(
                importButton,
                table,
                details
        );
        layout.setSpacing(10.0);

        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setFillWidth(true);
        VBox.setVgrow(table, Priority.ALWAYS);

        setContent(layout);
    }

    private void createPayments(Collection<Payment> newPayments) {
        // We simply append, preventing to restore everything (hopefully). We also won't depend on the sorting by
        // accident later on.
        state.data.payments.addAll(newPayments);
        state.storer.store(state.data);
        state.storer.commit();
        table.getItems().addAll(convertPayments(newPayments));
        // Even with active sorting, the table won't sort automatically.
        table.sort();
    }

    private static List<PaymentFX> convertPayments(Collection<Payment> payments) {
        final List<PaymentFX> newPaymentsFX = new ArrayList<>(payments.size());
        for (final var payment : payments) {
            newPaymentsFX.add(new PaymentFX(payment));
        }
        return newPaymentsFX;
    }
}


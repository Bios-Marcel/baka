package link.biosmarcel.baka.view;

import javafx.geometry.Insets;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import link.biosmarcel.baka.ApplicationState;
import link.biosmarcel.baka.bankimport.*;
import link.biosmarcel.baka.data.Account;
import link.biosmarcel.baka.data.Payment;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

public class PaymentsView extends BakaTab {
    private final TableView<PaymentFX> table;
    private final PaymentDetails details;
    private final MenuButton importButton;

    public PaymentsView(ApplicationState state) {
        super("Payments", state);

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

        final TableColumn<PaymentFX, Account> accountColumn = new TableColumn<>("Account");
        accountColumn.setCellValueFactory(cell -> cell.getValue().account);
        accountColumn.setCellFactory(_ -> new TableCellRenderer<>(account -> account.name));

        table.getColumns().addAll(
                amountColumn,
                nameColumn,
                referenceColumn,
                bookingDateColumn,
                effectiveDateColumn,
                accountColumn
        );
        table.getSortOrder().add(effectiveDateColumn);

        importButton = new MenuButton("Import");

        details = new PaymentDetails(state);

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


    private void importHandler(final Account account, final BiFunction<Account, File, List<Payment>> importer) {
        final var file = new FileChooser().showOpenDialog(getTabPane().getScene().getWindow());
        if (file == null) {
            return;
        }

        createPayments(importer.apply(account, file));
    }

    private void createPayments(final Collection<Payment> newPayments) {
        // We simply append, preventing to restore everything (hopefully). We also won't depend on the sorting by
        // accident later on.
        final var possibleDuplicates = state.data.importPayments(newPayments);

        // FIXME If we encounter duplicates, we show a dialog that allows marking relevant ones as non-duplicate, assigning
        //  them a random identifier.
        if (!possibleDuplicates.isEmpty()) {
            System.out.println("Possible dupes: " + possibleDuplicates.size());
            // FIXME Do a reimport where the user can chose which to reimport.
        }

        state.storer.store(state.data);
        state.storer.commit();
        table.getItems().addAll(convertPayments(newPayments));
        // Even with active sorting, the table won't sort automatically.
        table.sort();
    }

    private static List<PaymentFX> convertPayments(final Collection<Payment> payments) {
        final List<PaymentFX> newPaymentsFX = new ArrayList<>(payments.size());
        for (final var payment : payments) {
            newPaymentsFX.add(new PaymentFX(payment));
        }
        return newPaymentsFX;
    }

    @Override
    protected void onTabActivated() {
        table.getItems().setAll(convertPayments(state.data.payments));
        table.sort();

        details.activePayment.bind(table.getSelectionModel().selectedItemProperty());

        for (final var account : state.data.accounts) {
            if (account.name == null || account.name.isBlank()) {
                continue;
            }
            if (account.importFormat == null) {
                continue;
            }

            final var menuItem = new MenuItem(account.name);
            menuItem.setOnAction(_ -> importHandler(account, account.importFormat.func));
            importButton.getItems().add(menuItem);
        }
    }

    @Override
    protected void onTabDeactivated() {
        details.activePayment.unbind();

        table.getItems().clear();
        importButton.getItems().clear();
    }
}


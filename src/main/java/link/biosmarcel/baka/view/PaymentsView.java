package link.biosmarcel.baka.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import link.biosmarcel.baka.ApplicationState;
import link.biosmarcel.baka.data.Account;
import link.biosmarcel.baka.data.Classification;
import link.biosmarcel.baka.data.Payment;
import link.biosmarcel.baka.filter.IncompleteQueryException;

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
    private final ObservableList<PaymentFX> data = FXCollections.observableArrayList();
    private final FilteredList<PaymentFX> filteredData = new FilteredList<>(data);

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

        final TableColumn<PaymentFX, String> tagsColumn = new TableColumn<>("Tags");
        tagsColumn.setCellValueFactory(cell -> cell.getValue().classificationRenderValue);

        final TableColumn<PaymentFX, Account> accountColumn = new TableColumn<>("Account");
        accountColumn.setCellValueFactory(cell -> cell.getValue().account);
        accountColumn.setCellFactory(_ -> new TableCellRenderer<>(account -> account.name));

        table.getColumns().addAll(
                amountColumn,
                nameColumn,
                bookingDateColumn,
                effectiveDateColumn,
                tagsColumn,
                referenceColumn,
                accountColumn
        );
        table.getSortOrder().add(effectiveDateColumn);
        SortedList<PaymentFX> sortableData = new SortedList<>(filteredData);
        sortableData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortableData);

        importButton = new MenuButton("Import");
        final var classifyButton = new Button("Apply Classification Rules");
        classifyButton.setOnAction(_ -> classifyAllUnclassified());

        details = new PaymentDetails(state);

        final var filterField = new AutocompleteField(new AutocompleteGenerator(new PaymentFilter())::generate);
        final var filter = new PaymentFilter();
        filterField.textProperty().addListener((_, _, newText) -> {
            try {
                filter.setQuery(newText);
                filteredData.setPredicate(paymentFX -> filter.test(paymentFX.payment));
            } catch (final IncompleteQueryException exception) {
                if (exception.empty) {
                    filteredData.setPredicate(null);
                }
                // If the query is not empty, but incomplete, it isn't really an issue.
            }
        });

        final var topBarCenterSpacer = new Region();
        final var layout = new VBox(
                new HBox(2.5, importButton, classifyButton, topBarCenterSpacer, filterField.getNode()),
                table,
                details
        );
        layout.setSpacing(10.0);
        HBox.setHgrow(topBarCenterSpacer, Priority.ALWAYS);

        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setFillWidth(true);
        VBox.setVgrow(table, Priority.ALWAYS);

        setContent(layout);
    }

    private void classifyAllUnclassified() {
        for (final var payment : state.data.payments) {
            if (payment.classifications.isEmpty() && payment.amount.doubleValue() < 0.0) {
                System.out.println(payment.name + " / " + payment.reference);
                for (final var rule : state.data.classificationRules) {
                    if (rule.test(payment)) {
                        final var classification = new Classification();
                        classification.tag = rule.tag;
                        classification.amount = payment.amount;
                        payment.classifications.add(classification);
                        state.storer.store(payment.classifications);
                        state.storer.store(payment);
                        break;
                    }
                }
            }
        }

        data.setAll(convertPayments(state.data.payments));
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
        // We call setALl, as we aren't aware of what was filtered.
        data.setAll(convertPayments(state.data.payments));
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
        data.setAll(convertPayments(state.data.payments));
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

        data.clear();
        importButton.getItems().clear();
    }
}


package link.biosmarcel.baka.view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import link.biosmarcel.baka.bankimport.Importer;
import link.biosmarcel.baka.data.Account;
import link.biosmarcel.baka.data.Classification;
import link.biosmarcel.baka.data.ClassificationRule;
import link.biosmarcel.baka.data.Payment;
import link.biosmarcel.baka.filter.FilterAutocompleteGenerator;
import link.biosmarcel.baka.filter.IncompleteQueryException;
import link.biosmarcel.baka.view.component.AutocompleteField;
import link.biosmarcel.baka.view.component.AutocompleteHelper;
import link.biosmarcel.baka.view.component.BakaTab;
import link.biosmarcel.baka.view.component.TableCellRenderer;
import link.biosmarcel.baka.view.model.PaymentFX;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

public class PaymentsView extends BakaTab {
    private final TableView<PaymentFX> table;
    private final PaymentDetails details;
    private final MenuButton importButton;
    private final ObservableList<PaymentFX> data = FXCollections.observableArrayList();
    private final FilteredList<PaymentFX> filteredData = new FilteredList<>(data);
    private final TagCompletion tagCompletion;

    public PaymentsView(ApplicationState state) {
        super("Payments", state);

        this.tagCompletion = new TagCompletion(state);
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
        classifyButton.setOnAction(_ -> classifyAll());

        // Debug Feature for now, as I am not sure whether I want to easily allow this and this isn't optimised and
        // has a bad user experience.
        final var deleteSelected = new Button("Delete Visible");
        deleteSelected.visibleProperty().bind(state.debugMode);
        deleteSelected.managedProperty().bind(state.debugMode);
        deleteSelected.setOnAction(__ -> {
            filteredData.forEach(payment -> state.data.payments.remove(payment.payment));
            state.storer.store(state.data);
            state.storer.commit();

            data.setAll(convertPayments(state.data.payments));
            table.sort();
        });

        details = new PaymentDetails(tagCompletion);

        final var filterField = new AutocompleteField(
                new char[]{')', '(', ' ', '\n'},
                new FilterAutocompleteGenerator(new PaymentFilter())::generate
        );
        filterField.setPrefColumnCount(30);
        final var filter = new PaymentFilter();

        StringProperty filterError = new SimpleStringProperty();
        BooleanProperty fatalError = new SimpleBooleanProperty();
        AutocompleteHelper.installErrorToolTip(filterField, filterError, fatalError);

        filterField.textProperty().addListener((_, _, newText) -> {
            try {
                filterError.set("");
                fatalError.set(false);
                filter.setQuery(newText);
                filteredData.setPredicate(paymentFX -> filter.test(paymentFX.payment));
            } catch (final IncompleteQueryException exception) {
                if (exception.empty) {
                    filteredData.setPredicate(null);
                } else {
                    filterError.set("Query is incomplete.");
                    fatalError.set(false);
                }
                // If the query is not empty, but incomplete, it isn't really an issue.
            } catch (final RuntimeException exception) {
                filterError.set(exception.getMessage());
                fatalError.set(true);
            }
        });

        final var topBarCenterSpacer = new Region();
        final var layout = new VBox(
                new HBox(2.5, importButton, classifyButton, deleteSelected, topBarCenterSpacer, filterField.getNode()),
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

    private void classifyAll() {
        final var compiledRules = state.data.classificationRules.stream().map(ClassificationRule::compile).toList();
        for (final var payment : state.data.payments) {
            // All fields updatable by classification are already set, nothing to do for now.
            if(!payment.classifications.isEmpty() && payment.ignoreSpending) {
                continue;
            }

            for (final var rule : compiledRules) {
                boolean canTag = rule.tag != null && !rule.tag.isBlank() && payment.classifications.isEmpty();
                // Rule would be a no-op anyway.
                if(!canTag && !rule.ignoreSpending) {
                    continue;
                }

                if (!rule.test(payment)) {
                    continue;
                }

                // We only add the tag if no tags have been added yet.
                if(canTag) {
                    final var classification = new Classification();
                    classification.tag = rule.tag;
                    classification.amount = payment.amount;
                    payment.classifications.add(classification);
                    state.storer.store(payment.classifications);
                }

                if(rule.ignoreSpending) {
                    payment.ignoreSpending = true;
                    state.storer.store(payment);
                }

                // Payment is fully classified, no need to keep testing.
                if (payment.ignoreSpending && !payment.classifications.isEmpty()) {
                    break;
                }
            }
        }

        data.setAll(convertPayments(state.data.payments));
    }

    private void importHandler(final Account account, final BiFunction<Account, File, List<Payment>> importer) {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import for account '%s'".formatted(account.name));

        final var initialDir = state.data.convenienceState.importDirectories.get(account);
        if (initialDir != null) {
            fileChooser.setInitialDirectory(new File(initialDir));
        } else if (state.data.convenienceState.lastImportPath != null) {
            fileChooser.setInitialDirectory(new File(state.data.convenienceState.lastImportPath));
        }
        final var files = fileChooser.showOpenMultipleDialog(getTabPane().getScene().getWindow());
        if (files == null || files.isEmpty()) {
            return;
        }

        // Since it's assumed we can only pick files from one directory, we use the next best parent dir we can find.
        final String dir = files.getFirst().getParent();
        state.data.convenienceState.lastImportPath = dir;
        state.storer.store(state.data.convenienceState);
        state.data.convenienceState.importDirectories.put(account, dir);
        state.storer.store(state.data.convenienceState.importDirectories);
        state.storer.commit();

        // We merge the files and do a single import.
        final var newPayments = files.stream()
                .flatMap(file -> importer.apply(account, file).stream())
                // Since the files usually have consistent order, we sort, as adding together two files contents may
                // produces an incorrect order.
                .sorted(Comparator.comparing(payment -> payment.bookingDate))
                .toList();
        createPayments(newPayments);
    }

    private void createPayments(final Collection<Payment> newPayments) {
        // We simply append, preventing to restore everything (hopefully). We also won't depend on the sorting by
        // accident later on.
        final var importSummary = Importer.importPayments(state.data, newPayments);

        // FIXME Allow user to manually handle what to do with the "potential duplicates".

        state.storer.store(state.data);
        state.storer.commit();
        // We call setALl, as we aren't aware of what was filtered.
        data.setAll(convertPayments(state.data.payments));
        // Even with active sorting, the table won't sort automatically.
        table.sort();

        final var summaryText = "Successful: %d\nDuplicates: %d\nPossible Duplicates: %d\nSkipped: %d\n".formatted(
                importSummary.successful.size(),
                importSummary.duplicates.size(),
                importSummary.possibleDuplicates.size(),
                importSummary.skipped.size()
        );
        final var importSummaryAlert = new Alert(Alert.AlertType.INFORMATION);
        importSummaryAlert.setTitle("Import done");
        importSummaryAlert.setHeaderText("Import Summary");
        importSummaryAlert.setContentText(summaryText);
        importSummaryAlert.initOwner(table.getScene().getWindow());
        importSummaryAlert.showAndWait();
    }

    private static List<PaymentFX> convertPayments(final Collection<Payment> payments) {
        final List<PaymentFX> newPaymentsFX = new ArrayList<>(payments.size());
        for (final var payment : payments) {
            newPaymentsFX.add(new PaymentFX(payment));
        }
        return newPaymentsFX;
    }

    @Override
    public void onTabActivated() {
        data.setAll(convertPayments(state.data.payments));
        table.sort();
        tagCompletion.update();

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
    public void onTabDeactivated() {
        details.activePayment.unbind();

        data.clear();
        importButton.getItems().clear();
    }

    @Override
    public void save() {
        details.save();
        state.storer.store(state.data);
        state.storer.commit();
    }
}


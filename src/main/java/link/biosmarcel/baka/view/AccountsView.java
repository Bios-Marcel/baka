package link.biosmarcel.baka.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import link.biosmarcel.baka.ApplicationState;
import link.biosmarcel.baka.data.Account;
import link.biosmarcel.baka.data.ImportFormat;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This view allows you to specify your own accounts. This is useful for cancelling out payments, pre-configuring
 * imports and more.
 */
public class AccountsView extends BakaTab {
    private final TableView<AccountFX> accountTable;

    public AccountsView(final ApplicationState state) {
        super("Accounts", state);

        this.accountTable = new TableView<>();

        final Callback<TableColumn<AccountFX, @Nullable String>, TableCell<AccountFX, @Nullable String>> simpleStringColumnFactory = __ ->
                new TextFieldTableCell<>(new StringConverter<String>() {
                    @Override
                    public @Nullable String toString(final @Nullable String name) {
                        return name;
                    }

                    @Override
                    public @Nullable String fromString(final @Nullable String string) {
                        return string;
                    }
                });
        final Callback<TableColumn<AccountFX, @Nullable ImportFormat>, TableCell<AccountFX, @Nullable ImportFormat>> importFormatColumnFactory = __ ->
                new ComboBoxTableCell<>(new StringConverter<ImportFormat>() {
                    @Override
                    public @Nullable String toString(final @Nullable ImportFormat object) {
                        if (object != null) {
                            return object.toString();
                        }
                        return "";
                    }

                    @Override
                    public @Nullable ImportFormat fromString(final @Nullable String string) {
                        if (string != null && string != "") {
                            return ImportFormat.valueOf(string);
                        }
                        return null;
                    }
                }, ImportFormat.values());

        final TableColumn<AccountFX, @Nullable String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellFactory(simpleStringColumnFactory);
        nameColumn.setCellValueFactory(cell -> cell.getValue().name);
        nameColumn.setOnEditCommit(event -> {
            event.getRowValue().name.set(event.getNewValue());
            event.getRowValue().account.name = event.getNewValue();
            state.storer.store(event.getRowValue().account);
            state.storer.commit();
        });

        final TableColumn<AccountFX, @Nullable String> ibanColumn = new TableColumn<>("IBAN");
        ibanColumn.setCellFactory(simpleStringColumnFactory);
        ibanColumn.setCellValueFactory(cell -> cell.getValue().iban);
        ibanColumn.setOnEditCommit(event -> {
            event.getRowValue().iban.set(event.getNewValue());
            event.getRowValue().account.iban = event.getNewValue();
            state.storer.store(event.getRowValue().account);
            state.storer.commit();
        });

        final TableColumn<AccountFX, @Nullable ImportFormat> importFormatColumn = new TableColumn<>("Import Format");
        importFormatColumn.setCellFactory(importFormatColumnFactory);
        importFormatColumn.setCellValueFactory(cell -> cell.getValue().importFormat);
        importFormatColumn.setOnEditCommit(event -> {
            event.getRowValue().importFormat.set(event.getNewValue());
            event.getRowValue().account.importFormat = event.getNewValue();
            state.storer.store(event.getRowValue().account);
            state.storer.commit();
        });

        this.accountTable.getColumns().addAll(
                nameColumn,
                ibanColumn,
                importFormatColumn
        );
        this.accountTable.setEditable(true);

        final var addButton = new Button("New");
        final var deleteButton = new Button("Delete");

        final var content = new VBox(
                new HBox(addButton, deleteButton),
                accountTable
        );

        VBox.setVgrow(accountTable, Priority.ALWAYS);
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setSpacing(5.0);

        setContent(content);

        addButton.setOnAction(__ -> {
            final var account = new Account();
            accountTable.getItems().add(new AccountFX(account));

            state.data.accounts.add(account);
            state.storer.store(state.data.accounts);
            state.storer.commit();
        });

        deleteButton.setOnAction(__ -> {
            final var selectedAccount = accountTable.getSelectionModel().selectedItemProperty().get();
            state.data.accounts.remove(selectedAccount.account);
            accountTable.getItems().remove(selectedAccount);
            state.storer.store(state.data.accounts);
            state.storer.commit();
        });
        deleteButton.disableProperty().bind(accountTable.getSelectionModel().selectedItemProperty().isNull());
    }

    private List<AccountFX> convertAccounts(final Collection<Account> accounts) {
        final List<AccountFX> viewAccounts = new ArrayList<>(accounts.size());
        for (final var account : accounts) {
            viewAccounts.add(new AccountFX(account));
        }
        return viewAccounts;
    }

    @Override
    protected void onTabActivated() {
        accountTable.getItems().setAll(convertAccounts(state.data.accounts));
    }

    @Override
    protected void onTabDeactivated() {
        accountTable.getItems().clear();
    }
}

package link.biosmarcel.baka.view;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import link.biosmarcel.baka.data.ImportFormat;
import org.jspecify.annotations.Nullable;

public class AccountDetails extends GridPane {
    private @Nullable ReadOnlyObjectProperty<@Nullable AccountFX> account;
    private final ChangeListener<@Nullable AccountFX> accountListener;

    AccountDetails() {
        add(new Label("Name"), 0, 0);
        add(new Label("IBAN"), 0, 1);
        add(new Label("Import Format"), 0, 2);

        final var nameTextField = new TextField();
        final var ibanTextField = new TextField();
        final var importFormatChooser = new ComboBox<>(FXCollections.observableArrayList(ImportFormat.values()));

        add(nameTextField, 1, 0);
        add(ibanTextField, 1, 1);
        add(importFormatChooser, 1, 2);

        setVgap(5.0);
        setHgap(10.0);

        accountListener = (_, _, newValue) -> {
            if (newValue != null) {
                nameTextField.setDisable(false);
                nameTextField.setText(newValue.name.get());
                ibanTextField.setDisable(false);
                ibanTextField.setText(newValue.iban.get());
                importFormatChooser.setDisable(false);
                importFormatChooser.getSelectionModel().select(newValue.importFormat.get());
            } else {
                nameTextField.setDisable(true);
                nameTextField.setText("");
                ibanTextField.setDisable(true);
                ibanTextField.setText("");
                importFormatChooser.setDisable(true);
                importFormatChooser.getSelectionModel().select(null);
            }
        };
    }

    void bind(final @Nullable ReadOnlyObjectProperty<AccountFX> account) {
        unbind();

        this.account = account;
        if (this.account != null) {
            this.account.addListener(accountListener);
        }
    }

    void unbind() {
        if (account != null) {
            account.removeListener(accountListener);
            account = null;
        }
    }
}

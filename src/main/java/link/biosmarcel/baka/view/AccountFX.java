package link.biosmarcel.baka.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import link.biosmarcel.baka.data.Account;
import link.biosmarcel.baka.data.ImportFormat;
import org.jspecify.annotations.Nullable;

public class AccountFX {
    public final Account account;

    public final StringProperty name = new SimpleStringProperty("");
    public final StringProperty iban = new SimpleStringProperty("");
    public final ObjectProperty<@Nullable ImportFormat> importFormat = new SimpleObjectProperty<>(null);

    public AccountFX(final Account account) {
        this.account = account;

        this.name.set(account.name);
        this.iban.set(account.iban);
        this.importFormat.set(account.importFormat);
    }
}

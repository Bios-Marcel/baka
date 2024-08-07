package link.biosmarcel.baka.view.model;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import link.biosmarcel.baka.data.Account;
import link.biosmarcel.baka.data.Classification;
import link.biosmarcel.baka.data.Payment;
import link.biosmarcel.baka.view.PaymentsView;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Data Model for the {@link PaymentsView}-Tab.
 */
public class PaymentFX {
    public final Payment payment;

    public final ObjectProperty<Account> account = new SimpleObjectProperty<>();

    public final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>();
    public final StringProperty reference = new SimpleStringProperty();
    public final StringProperty name = new SimpleStringProperty();

    public final ObjectProperty<LocalDate> bookingDate = new SimpleObjectProperty<>();
    public final ObjectProperty<@Nullable LocalDate> effectiveDate = new SimpleObjectProperty<>();

    public final ObservableList<ClassificationFX> classifications = FXCollections.observableArrayList();
    public final StringBinding classificationRenderValue = Bindings.createStringBinding(() -> {
        return classifications
                .stream()
                .map(classification -> classification.tag.get())
                .reduce((string, string2) -> string + "; " + string2)
                .orElse("");
    }, classifications);

    public PaymentFX(final Payment payment) {
        this.payment = payment;

        account.set(payment.account);
        amount.set(payment.amount);
        reference.set(payment.reference);
        name.set(payment.name);
        bookingDate.set(payment.bookingDate.toLocalDate());
        if (payment.effectiveDate != null) {
            effectiveDate.set(payment.effectiveDate.toLocalDate());
        }
        classifications.addAll(convertClassifications(payment.classifications));
    }

    private static List<ClassificationFX> convertClassifications(Collection<Classification> classifications) {
        final var converted = new ArrayList<ClassificationFX>(classifications.size());
        for (final var classification : classifications) {
            converted.add(new ClassificationFX(classification));
        }
        return converted;
    }
}

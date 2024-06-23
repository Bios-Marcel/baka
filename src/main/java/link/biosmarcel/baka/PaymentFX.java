package link.biosmarcel.baka;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Model for the {@link PaymentsView}-Tab.
 */
public class PaymentFX {
    public final Payment payment;

    public final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>();
    public final StringProperty reference = new SimpleStringProperty();
    public final StringProperty name = new SimpleStringProperty();

    public final ObjectProperty<LocalDate> bookingDate = new SimpleObjectProperty<>();
    public final ObjectProperty<LocalDate> effectiveDate = new SimpleObjectProperty<>();

    public final ObservableList<Classification> classifications = FXCollections.observableArrayList();

    public PaymentFX(final Payment payment) {
        this.payment = payment;

        amount.set(payment.amount);
        reference.set(payment.reference);
        name.set(payment.name);
        bookingDate.set(payment.bookingDate.toLocalDate());
        effectiveDate.set(payment.effectiveDate.toLocalDate());
        classifications.addAll(payment.classifications);
    }
}

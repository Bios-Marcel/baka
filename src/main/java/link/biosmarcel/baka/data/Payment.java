package link.biosmarcel.baka.data;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Payment {
    public Account account;

    public BigDecimal amount;
    public String reference;
    public String name;
    /**
     * IBAN of the sender / recipient.
     */
    public @Nullable String participant;

    /**
     * Can be used to uniquely identify a payment. Useful if you import payments that seem like duplicates otherwise.
     * This identifier however isn't always available.
     */
    public @Nullable String identifier;

    public LocalDateTime bookingDate;
    /**
     * Potentially the same as bookingDate
     */
    public LocalDateTime effectiveDate;

    /**
     * Some payments might not be relevant for any scenario. These can be ignored during calculations.
     * For example, if you pay 100€ for damages and get back the same amount from your insurance, this is not a
     * spending you need to know about.
     */
    public @Nullable Payment cancelledOutBy;

    /**
     * For negative (outgoing) payments, this will make sure that the payment does not count as a spending.
     */
    public boolean ignoreSpending = false;

    public List<Classification> classifications = new ArrayList<>();

    public Payment(
            final Account account,
            final BigDecimal amount,
            final String reference,
            final String name,
            final LocalDateTime bookingDate,
            final @Nullable LocalDateTime effectiveDate
    ) {
        this.account = account;
        this.amount = amount;
        this.reference = reference;
        this.name = name;
        this.bookingDate = bookingDate;
        this.effectiveDate = Objects.requireNonNullElse(effectiveDate, bookingDate);
    }

    /**
     * This method is deliberately not called `equals`, since it can't for sure say whether Payments are the same.
     * For example, if you send the same amount to the same person twice on the same day, your bank data export
     * might not include any data sufficient for uniquely identifying these two payments. So, only if
     */
    public EqualityAssumption assumeEquality(Payment other) {
        if (!account.equals(other.account)) {
            return EqualityAssumption.NOT_EQUAL;
        }

        if (identifier != null && !identifier.isEmpty()) {
            // If one side is empty, we can assume that these aren't equal. The only situation where this might not
            // hold true, is when the bank has recently started adding more information to their export.
            if (other.identifier == null || other.identifier.isEmpty()) {
                return EqualityAssumption.NOT_EQUAL;
            }

            if (identifier.equals(other.identifier)) {
                return EqualityAssumption.EQUAL;
            }
        }

        if (amount.compareTo(other.amount) != 0) {
            return EqualityAssumption.NOT_EQUAL;
        }

        if (!Objects.equals(reference, other.reference)) {
            return EqualityAssumption.NOT_EQUAL;
        }

        if (!Objects.equals(participant, other.participant)) {
            return EqualityAssumption.NOT_EQUAL;
        }

        if (!Objects.equals(name, other.name)) {
            return EqualityAssumption.NOT_EQUAL;
        }

        if (!Objects.equals(bookingDate, other.bookingDate)) {
            return EqualityAssumption.NOT_EQUAL;
        }

        if (!Objects.equals(effectiveDate, other.effectiveDate)) {
            return EqualityAssumption.NOT_EQUAL;
        }

        return EqualityAssumption.POSSIBLY_EQUAL;
    }

    // For now, we want reference equality!
    //    /**
    //     * @deprecated prefer {@link #assumeEquality(Payment)}
    //     * <p>
    //     * {@inheritDoc}
    //     */
    //    @Override
    //    @Deprecated
    //    public boolean equals(Object o) {
    //        if (this == o) return true;
    //        if (o == null || getClass() != o.getClass()) return false;
    //        return assumeEquality((Payment) o) == EqualityAssumption.EQUAL;
    //    }
    //
    //    /**
    //     * @deprecated prefer {@link #assumeEquality(Payment)}
    //     * <p>
    //     * {@inheritDoc}
    //     */
    //    @Override
    //    public int hashCode() {
    //        return Objects.hash(identifier, amount, reference, bookingDate, effectiveDate);
    //    }
}

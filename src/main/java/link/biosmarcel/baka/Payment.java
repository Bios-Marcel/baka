package link.biosmarcel.baka;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Payment {
    public BigDecimal amount;
    public String reference;
    public String name;
    /**
     * IBAN of the sender / recipient.
     */
    public @Nullable String account;

    /**
     * Can be used to uniquely identify a payment. Useful if you import payments that seem like duplicates otherwise.
     * This identifier however isn't always available.
     */
    public @Nullable String identifier;

    public LocalDateTime bookingDate;
    public LocalDateTime effectiveDate;

    public List<Classification> classifications = new ArrayList<>();

    /**
     * This method is deliberately not called `equals`, since it can't for sure say whether Payments are the same.
     * For example, if you send the same amount to the same person twice on the same day, your bank data export
     * might not include any data sufficient for uniquely identifying these two payments. So, only if
     */
    public EqualityAssumption assumeEquality(Payment other) {
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

        if (!Objects.equals(account, other.account)) {
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
}

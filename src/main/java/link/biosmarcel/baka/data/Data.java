package link.biosmarcel.baka.data;

import java.math.BigDecimal;
import java.util.*;

/**
 * Root-Object for our storage. Getters and setters are not required here. Collection values are automatically
 * initialised once data is found. By default, such fields will be {@code null}.
 */
public class Data {
    /**
     * A list of ALL payments. Sorted by {@link Payment#effectiveDate} descending.
     */
    public List<Payment> payments = new ArrayList<>();

    public List<Account> accounts = new ArrayList<>();

    /**
     * Import payments will add new payments to the data, if the data doesn't already exist.
     *
     * @return a Map from existing payments to possible new duplicates, allowing a potential reimport of data we failed
     * to import. The existing data should receive identifiers, so that this process can be repeated for the future.
     */
    public Map<Payment, Payment> importPayments(final Collection<Payment> newPayments) {
        final var filteredPayments = newPayments
                .stream()
                // Banks sometimes add info payments at the end of the months. We don't want these for now.
                .filter(payment -> payment.amount.compareTo(BigDecimal.ZERO) != 0)
                .filter(newPayment -> {
                    // We iterate backwards to save time, as the payments are sorted by date descending.
                    for (int i = payments.size() - 1; i >= 0; i--) {
                        final var existingPayment = payments.get(i);
                        final var equality = existingPayment.assumeEquality(newPayment);
                        if (equality == EqualityAssumption.EQUAL) {
                            return false;
                        }

                        if (newPayment.bookingDate.isBefore(existingPayment.bookingDate)) {
                            return false;
                        }

                        // Technically we could have equal dates here, meaning that some of the last entries might be
                        // duplicates. This should happen rarely, but could be more than a single item. However, those
                        // entries SHOULD always be on the same day.
                    }

                    return true;
                })
                .toList();

        final Map<Payment, Payment> possibleDuplicates = new HashMap<>();
        OUTER_LOOP:
        for (final var newPayment : newPayments) {
            for (int i = payments.size() - 1; i >= 0; i--) {
                final var existingPayment = payments.get(i);
                if (newPayment.bookingDate.isBefore(existingPayment.bookingDate)) {
                    continue OUTER_LOOP;
                }

                final var equality = existingPayment.assumeEquality(newPayment);
                if (equality == EqualityAssumption.POSSIBLY_EQUAL) {
                    possibleDuplicates.put(existingPayment, newPayment);
                }
            }
        }

        payments.addAll(filteredPayments);

        // Since we can't guarantee the imported  data to be in our desired order, we resort the whole thing.
        payments.sort((o1, o2) -> {
            final int bookingDateComparison = o1.bookingDate.compareTo(o2.bookingDate);
            if (bookingDateComparison != 0) {
                return bookingDateComparison;
            }

            if (o1.effectiveDate != null && o2.effectiveDate != null) {
                return o1.effectiveDate.compareTo(o2.effectiveDate);
            }

            return bookingDateComparison;
        });

        return possibleDuplicates;
    }
}

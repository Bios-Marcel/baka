package link.biosmarcel.baka.bankimport;

import link.biosmarcel.baka.data.Classifier;
import link.biosmarcel.baka.data.Data;
import link.biosmarcel.baka.data.Payment;

import java.math.BigDecimal;
import java.util.*;

public final class Importer {
    /**
     * A summary of an important, giving as much detailed information as possible as to what happen and also allowing
     * for post-import adjustments.
     */
    public static final class ImportSummary {
        /**
         * Payments that were neither duplicates, nor possible duplicates and were therefore imported without issues.
         */
        public final List<Payment> successful = new ArrayList<>();
        /**
         * Payments where we are 100% sure they are duplicates, due to the fact that they have some unique identifier
         * that is duplicated.
         */
        public final List<Payment> duplicates = new ArrayList<>();
        /**
         * Payments that look the same, but might not necessarily be duplicates. We map from exiting payments to new
         * payments.
         */
        public final Map<Payment, Payment> possibleDuplicates = new HashMap<>();

        /**
         * These payments were skipped due to the fact that they provide no real value. These can be informational
         * entries sometimes added by banks.
         */
        public final List<Payment> skipped = new ArrayList<>();
    }

    /**
     * Import payments will add new payments to the data, if the data doesn't already exist.
     *
     * @return a Map from existing payments to possible new duplicates, allowing a potential reimport of data we failed
     * to import. The existing data should receive identifiers, so that this process can be repeated for the future.
     */
    public static ImportSummary importPayments(
            final Data data,
            final Collection<Payment> newPayments) {
        final var classifier = new Classifier(data.classificationRules);
        final var summary = new ImportSummary();

        OUTER_LOOP:
        for (final Payment newPayment : newPayments) {
            // We classify everything for now, even duplicates and skipped payments.
            classifier.classify(newPayment);

            // Banks sometimes add info payments at the end of the months. We don't want these for now.
            if (newPayment.amount.compareTo(BigDecimal.ZERO) == 0) {
                summary.skipped.add(newPayment);
                continue;
            }

            // We iterate backwards to save time, as the payments are sorted by date descending.
            for (int i = data.payments.size() - 1; i >= 0; i--) {
                final var existingPayment = data.payments.get(i);
                final var equality = existingPayment.assumeEquality(newPayment);
                switch (equality) {
                    case EQUAL -> {
                        summary.duplicates.add(newPayment);
                        continue OUTER_LOOP;
                    }
                    case POSSIBLY_EQUAL -> {
                        summary.possibleDuplicates.put(existingPayment, newPayment);
                        continue OUTER_LOOP;
                    }
                }

                // Importing older payments makes no sense, unless its a different bank account, as people use multiple accounts simultaneously.
                if (newPayment.account.equals(existingPayment.account) && newPayment.bookingDate.isBefore(existingPayment.bookingDate)) {
                    // FIXME This is potentially not a good idea anymore! If files are imported out of order, payments will be skipped.
                    summary.duplicates.add(newPayment);
                    continue OUTER_LOOP;
                }

                // Technically we could have equal dates here, meaning that some of the last entries might be
                // duplicates. This should happen rarely, but could be more than a single item. However, those
                // entries SHOULD always be on the same day.
            }

            summary.successful.add(newPayment);
        }

        data.payments.addAll(summary.successful);

        // Since we can't guarantee the imported  data to be in our desired order, we resort the whole thing.
        data.payments.sort((o1, o2) -> {
            final int bookingDateComparison = o1.bookingDate.compareTo(o2.bookingDate);
            if (bookingDateComparison != 0) {
                return bookingDateComparison;
            }

            if (o1.effectiveDate != null && o2.effectiveDate != null) {
                return o1.effectiveDate.compareTo(o2.effectiveDate);
            }

            return bookingDateComparison;
        });

        return summary;
    }
}

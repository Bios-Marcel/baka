package link.biosmarcel.baka.bankimport;

import link.biosmarcel.baka.Payment;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RevolutCSV {
    private static final DateTimeFormatter REVOLUT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DecimalFormat REVOLUT_CURRENCY_FORMAT;

    static {
        REVOLUT_CURRENCY_FORMAT = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.ENGLISH);
        REVOLUT_CURRENCY_FORMAT.setParseBigDecimal(true);
    }

    public static List<Payment> parse(final File file) {
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.ISO_8859_1)) {
            final var format = CSVFormat.Builder
                    .create()
                    .setDelimiter(',')
                    // Haven't figured out the escape character yet.
                    .setQuoteMode(QuoteMode.MINIMAL)
                    .setIgnoreSurroundingSpaces(true)
                    .setIgnoreEmptyLines(true)
                    .build();
            final var records = format.parse(reader).iterator();
            // Skip header row; Format.setSkipHeaderRecord doesn't seem to work.
            records.next();

            final List<Payment> newPayments = new ArrayList<>();
            records.forEachRemaining(record -> {
                final var transactionState = record.get(8);
                // We skip these for now to keep the logic simpler.
                if (!"COMPLETED".equals(transactionState)) {
                    return;
                }

                final var reference = record.get(4);
                final BigDecimal amount;
                try {
                    amount = (BigDecimal) REVOLUT_CURRENCY_FORMAT.parse(record.get(5));
                } catch (final ParseException exception) {
                    throw new RuntimeException(exception);
                }

                final LocalDateTime bookingDate = LocalDateTime.parse(record.get(2), REVOLUT_DATE_FORMAT);
                final LocalDateTime effectiveDate = switch (record.get(3)) {
                    // EffectiveDate is optional, so to avoid confusion, we just set it to the same as bookingDate.
                    case null -> bookingDate;
                    case "" -> bookingDate;
                    default -> LocalDateTime.parse(record.get(2), REVOLUT_DATE_FORMAT);
                };
                final Payment payment = new Payment();
                // Revolut CSV doesn't supply this, we just got the reference, which is called "description"
                payment.name = "";
                payment.amount = amount;
                payment.reference = reference;
                payment.bookingDate = bookingDate;
                payment.effectiveDate = effectiveDate;
                newPayments.add(payment);
            });

            return newPayments;
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}

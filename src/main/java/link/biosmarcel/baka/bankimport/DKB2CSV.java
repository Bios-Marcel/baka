package link.biosmarcel.baka.bankimport;

import link.biosmarcel.baka.data.Account;
import link.biosmarcel.baka.data.Payment;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Format for new DKB UI.
 */
public class DKB2CSV {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yy");
    private static final DecimalFormat CURRENCY_FORMAT;

    static {
        CURRENCY_FORMAT = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.GERMAN);
        CURRENCY_FORMAT.setParseBigDecimal(true);
    }

    public static List<Payment> parse(final Account account, final File file) {
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.ISO_8859_1)) {
            final var format = CSVFormat.Builder
                    .create()
                    .setDelimiter(';')
                    .setQuote('"')
                    // Haven't figured out the escape character yet.
                    .setQuoteMode(QuoteMode.MINIMAL)
                    .setIgnoreSurroundingSpaces(true)
                    .setIgnoreEmptyLines(true)
                    .setAllowMissingColumnNames(true)
                    .build();
            final var records = format.parse(reader).iterator();
            // Skip header row; Format.setSkipHeaderRecord doesn't seem to work.
            // Also skip rows that just contain meta data. Empty rows don't count, so we skip 5.
            for (int i = 0; i < 5; i++) {
                records.next();
            }

            final List<Payment> newPayments = new ArrayList<>();
            records.forEachRemaining(record -> {
                final var reference = record.get(5);
                final BigDecimal amount;
                try {
                    amount = (BigDecimal) CURRENCY_FORMAT.parse(record.get(8));
                } catch (final ParseException exception) {
                    throw new RuntimeException(exception);
                }

                final var bookingDate = LocalDate.parse(record.get(0), DATE_FORMAT).atStartOfDay();
                final var effectiveDate = switch (record.get(1)) {
                    // EffectiveDate is optional, so to avoid confusion, we just set it to the same as bookingDate.
                    case null -> bookingDate;
                    case "" -> bookingDate;
                    default -> LocalDate.parse(record.get(1), DATE_FORMAT).atStartOfDay();
                };
                final String name = record.get(3);

                final Payment payment = new Payment(
                        account,
                        amount,
                        reference,
                        name,
                        bookingDate,
                        effectiveDate
                );

                payment.participant = Import.prepareIBAN(record.get(7));

                newPayments.add(payment);
            });

            return newPayments;
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}

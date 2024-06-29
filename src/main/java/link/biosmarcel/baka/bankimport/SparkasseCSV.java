package link.biosmarcel.baka.bankimport;

import link.biosmarcel.baka.Payment;
import org.apache.commons.csv.CSVFormat;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SparkasseCSV {
    private static final DateTimeFormatter SPARKASSE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yy");
    private static final DecimalFormat SPARKASSE_CURRENCY_FORMAT;

    static {
        SPARKASSE_CURRENCY_FORMAT = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.GERMAN);
        SPARKASSE_CURRENCY_FORMAT.setParseBigDecimal(true);
    }

    public static List<Payment> parse(final File file) {
        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.ISO_8859_1)) {
            final var format = CSVFormat.Builder
                    .create(CSVFormat.EXCEL)
                    .setDelimiter(';')
                    .setIgnoreSurroundingSpaces(true)
                    .setIgnoreEmptyLines(true)
                    .build();
            final var records = format.parse(reader).iterator();
            // Skip header row; Format.setSkipHeaderRecord doesn't seem to work.
            records.next();

            final List<Payment> newPayments = new ArrayList<>();
            records.forEachRemaining(record -> {
                final BigDecimal amount;
                try {
                    amount = (BigDecimal) SPARKASSE_CURRENCY_FORMAT.parse(record.get(14));
                } catch (final ParseException exception) {
                    throw new RuntimeException(exception);
                }
                final String reference = record.get(4);
                final LocalDateTime bookingDate = LocalDate.parse(record.get(1), SPARKASSE_DATE_FORMAT).atStartOfDay();
                final LocalDateTime effectiveDate = switch (record.get(2)) {
                    // EffectiveDate is optional, so to avoid confusion, we just set it to the same as bookingDate.
                    case null -> bookingDate;
                    case "" -> bookingDate;
                    default -> LocalDate.parse(record.get(2), SPARKASSE_DATE_FORMAT).atStartOfDay();
                };

                final Payment payment = new Payment();
                payment.name = record.get(11);
                payment.account = record.get(12);
                payment.identifier = record.get(7);
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

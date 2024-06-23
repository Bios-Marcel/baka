package link.biosmarcel.baka;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.commons.csv.CSVFormat;

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
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class PaymentsView extends Tab {
    private final State state;

    public PaymentsView(State state) {
        super("Payments");

        this.state = state;

        final var table = new TableView<PaymentFX>();

        final TableColumn<PaymentFX, BigDecimal> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cell -> cell.getValue().amount);

        final TableColumn<PaymentFX, String> referenceColumn = new TableColumn<>("Reference");
        referenceColumn.setCellValueFactory(cell -> cell.getValue().reference);

        final TableColumn<PaymentFX, LocalDate> bookingDateColumn = new TableColumn<>("Booking Date");
        bookingDateColumn.setCellValueFactory(cell -> cell.getValue().bookingDate);

        final TableColumn<PaymentFX, LocalDate> effectiveDateColumn = new TableColumn<>("Effective Date");
        effectiveDateColumn.setCellValueFactory(cell -> cell.getValue().effectiveDate);

        table.getColumns().addAll(
                amountColumn,
                referenceColumn,
                bookingDateColumn,
                effectiveDateColumn
        );
        table.getItems().setAll(convertPayments(state.data.payments));

        final var importButton = new Button("Import Bankdata");
        importButton.setOnAction(_ -> {
            FileChooser fileChooser = new FileChooser();
            final var file = fileChooser.showOpenDialog(getTabPane().getScene().getWindow());
            if (file == null) {
                return;
            }

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
                        amount = parseBigDecimal(record.get(14));
                    } catch (final ParseException exception) {
                        throw new RuntimeException(exception);
                    }
                    final String reference = record.get(4);
                    final LocalDate bookingDate = LocalDate.parse(record.get(1), SIMPLE_DATE_FORMAT);
                    final LocalDate effectiveDate = switch (record.get(2)) {
                        // EffectiveDate is optional, so to avoid confusion, we just set it to the same as bookingDate.
                        case null -> bookingDate;
                        case "" -> bookingDate;
                        default -> LocalDate.parse(record.get(2), SIMPLE_DATE_FORMAT);
                    };

                    final Payment payment = new Payment();
                    payment.amount = amount;
                    payment.reference = reference;
                    payment.bookingDate = bookingDate;
                    payment.effectiveDate = effectiveDate;
                    newPayments.add(payment);
                });

                state.data.payments.addAll(newPayments);
                state.storer.store(state.data);
                System.out.println(state.storer.commit());
                table.getItems().addAll(convertPayments(newPayments));
            } catch (final IOException exception) {
                throw new RuntimeException(exception);
            }

            state.storageManager.storeRoot();
        });

        PaymentDetails details = new PaymentDetails(state);
        details.activePayment.bind(table.getSelectionModel().selectedItemProperty());

        final var layout = new VBox(
                importButton,
                table,
                details
        );
        layout.setSpacing(10.0);

        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setFillWidth(true);
        VBox.setVgrow(table, Priority.ALWAYS);

        setContent(layout);
    }

    private static List<PaymentFX> convertPayments(Collection<Payment> payments) {
        final List<PaymentFX> newPaymentsFX = new ArrayList<>(payments.size());
        for (final var payment : payments) {
            newPaymentsFX.add(new PaymentFX(payment));
        }
        return newPaymentsFX;
    }

    static final DateTimeFormatter SIMPLE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yy");
    static final DecimalFormat LOCAL_CURRENCY_FORMAT;

    static {
        LOCAL_CURRENCY_FORMAT = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.GERMAN);
        LOCAL_CURRENCY_FORMAT.setParseBigDecimal(true);

    }

    private static BigDecimal parseBigDecimal(String value) throws ParseException {
        return (BigDecimal) LOCAL_CURRENCY_FORMAT.parse(value);
    }
}


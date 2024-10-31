package link.biosmarcel.baka.view;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import link.biosmarcel.baka.ApplicationState;
import link.biosmarcel.baka.bankimport.Importer;
import link.biosmarcel.baka.data.Payment;
import link.biosmarcel.baka.view.component.BakaTab;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class DebugView extends BakaTab {
    public DebugView(final ApplicationState state) {
        super("Debug", state);

        final var deleteAllPaymentsButton = new Button("Delete All Payments");
        deleteAllPaymentsButton.setOnAction(_ -> {
            state.data.payments.clear();
            state.storer.store(state.data);
            state.storer.commit();
        });

        final var createFakePaymentsButton = new Button("Create Fake Payments");
        createFakePaymentsButton.setOnAction(_ -> {
            final var alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(createFakePaymentsButton.getText());
            alert.initOwner(getTabPane().getScene().getWindow());

            final var startDate = new DatePicker();
            final var endDate = new DatePicker();

            alert.getDialogPane().setContent(new HBox(5.0, startDate, endDate));
            alert.showAndWait();

            // FIXME Dialog if no account present

            final var account = state.data.accounts.getFirst();
            final var rand = ThreadLocalRandom.current();
            final var payments = new ArrayList<Payment>((int) (endDate.getValue().toEpochDay() - startDate.getValue().toEpochDay()) + 1);
            for (var day = startDate.getValue(); !day.isAfter(endDate.getValue()); day = day.plusDays(1)) {
                final var dateTime = day.atStartOfDay();
                payments.add(new Payment(
                        account,
                        BigDecimal.valueOf(rand.nextLong(-50, 50)),
                        "Demo Reference",
                        "Demo Name",
                        dateTime,
                        dateTime
                ));
            }

            Importer.importPayments(state.data, payments);
            state.storer.store(state.data.payments);
            state.storer.commit();
        });

        final var layout = new VBox(5.0,
                deleteAllPaymentsButton,
                createFakePaymentsButton);
        layout.setPadding(new Insets(5.0));

        setContent(layout);
    }

    @Override
    public void onTabActivated() {

    }

    @Override
    public void onTabDeactivated() {

    }
}

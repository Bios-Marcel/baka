package link.biosmarcel.baka;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class EvaluationView extends Tab {
    private final State state;

    private final ObservableList<XYChart.Series<String, Number>> spendingsData;
    private final ObservableList<XYChart.Series<Number, Number>> balanceData;
    private final IntegerProperty balanceLowerBound;
    private final IntegerProperty balanceUpperBound;
    private final ObjectProperty<LocalDate> startDate;
    private final ObjectProperty<LocalDate> endDate;

    public EvaluationView(State state) {
        super("Evaluation");

        this.state = state;
        this.spendingsData = FXCollections.observableArrayList();
        this.balanceData = FXCollections.observableArrayList();
        this.balanceLowerBound = new SimpleIntegerProperty();
        this.balanceUpperBound = new SimpleIntegerProperty();
        this.startDate = new SimpleObjectProperty<>();
        this.endDate = new SimpleObjectProperty<>();

        selectedProperty().addListener((_, _, newValue) -> {
            // Once the tab opens, we load all data, not before.
            if (newValue) {
                onTabActivate();
            }
        });

        final var spendingChartAmountAxis = new NumberAxis();
        spendingChartAmountAxis.setForceZeroInRange(true);

        final PatchedStackedBarChart<String, Number> spendingsChart = new PatchedStackedBarChart<>(
                new CategoryAxis(),
                spendingChartAmountAxis,
                spendingsData
        );
        spendingsChart.setAnimated(false);

        final var balanceChartDayAxis = new NumberAxis();
        balanceChartDayAxis.setTickUnit(1.0);
        balanceChartDayAxis.setAutoRanging(false);
        balanceChartDayAxis.lowerBoundProperty().bind(balanceLowerBound);
        balanceChartDayAxis.upperBoundProperty().bind(balanceUpperBound);

        final var balanceChartAmountAxis = new NumberAxis();
        balanceChartAmountAxis.setForceZeroInRange(false);

        final LineChart<Number, Number> balanceChart = new LineChart<>(
                balanceChartDayAxis,
                balanceChartAmountAxis,
                balanceData
        );
        balanceChart.setAnimated(false);

        final DatePicker startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDate
                .now()
                .minusMonths(3)
                .with(TemporalAdjusters.firstDayOfMonth())
        );
        startDate.bind(startDatePicker.valueProperty());

        final DatePicker endDatePicker = new DatePicker();
        endDatePicker.setValue(LocalDate
                .now()
                .with(TemporalAdjusters.lastDayOfMonth()));
        endDate.bind(endDatePicker.valueProperty());

        startDate.addListener((_, _, _) -> updateCharts());
        endDate.addListener((_, _, _) -> updateCharts());

        final HBox topBar = new HBox(
                startDatePicker,
                endDatePicker
        );
        topBar.setSpacing(5.0);

        final VBox layout = new VBox(
                topBar,
                new Label("Spendings:"),
                spendingsChart,
                new Label("Balance:"),
                balanceChart
        );
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setSpacing(5.0);

        VBox.setVgrow(spendingsChart, Priority.ALWAYS);

        setContent(layout);
    }

    private void onTabActivate() {
        updateCharts();
    }

    private void updateCharts() {
        final String unclassified = "Unclassified";
        final Map<String, Map<Month, BigDecimal>> classificationToMonthToMoney = new HashMap<>();
        classificationToMonthToMoney.put(unclassified, new TreeMap<>());

        // FIXME Add initial balance.
        BigDecimal balance = new BigDecimal("0");
        final XYChart.Series<Number, Number> balanceSeries = new XYChart.Series<>();
        balanceData.setAll(balanceSeries);

        final Collection<Payment> sorted = state.data.payments
                .stream()
                .sorted(Comparator.comparing(a -> a.effectiveDate))
                .toList();
        for (final var payment : sorted) {
            if (payment.effectiveDate.isBefore(startDate.get())) {
                continue;
            }
            if (payment.effectiveDate.isAfter(endDate.get())) {
                continue;
            }

            final var month = payment.effectiveDate.getMonth();
            balance = balance.add(payment.amount);
            final var chartX =
                    // Z.B. March (3) +
                    month.getValue()
                            // 10
                            + (double) (payment.effectiveDate.getDayOfMonth())
                            // 30
                            / (double) (payment.effectiveDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth())
                    // = 3.33
                    ;
            balanceSeries.getData().add(new XYChart.Data<>(chartX, balance));

            // Separates balance from spendings.
            if (payment.amount.intValue() >= 0) {
                continue;
            }

            // FIXME Retrieve classification via smart code that is yet to be written.
            final var classification = classificationToMonthToMoney.get(unclassified);
            var value = classification.get(month);
            var amount = payment.amount;
            if (value != null) {
                value = amount.add(value);
            } else {
                value = amount;
            }
            classification.put(month, value);
        }

        balanceLowerBound.setValue(Math.floor(balanceSeries.getData().getFirst().getXValue().doubleValue()));
        balanceUpperBound.setValue(Math.ceil(balanceSeries.getData().getLast().getXValue().doubleValue()));

        final List<XYChart.Series<String, Number>> fragments = new ArrayList<>();
        for (final var category : classificationToMonthToMoney.entrySet()) {
            final XYChart.Series<String, Number> categorySeries = new XYChart.Series<>();
            categorySeries.setName(category.getKey());

            for (final var monthToAmount : category.getValue().entrySet()) {
                categorySeries.getData().add(new XYChart.Data<>(
                        renderMonth(monthToAmount.getKey()),
                        monthToAmount.getValue()
                ));
            }

            fragments.add(categorySeries);
        }

        spendingsData.setAll(fragments);
    }

    private static String renderMonth(final Month month) {
        return month.getDisplayName(TextStyle.FULL, Locale.getDefault());
    }
}

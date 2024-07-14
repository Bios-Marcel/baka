package link.biosmarcel.baka.view;

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
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import link.biosmarcel.baka.ApplicationState;
import link.biosmarcel.baka.data.Account;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class EvaluationView extends BakaTab {
    private final ObservableList<XYChart.Series<String, Number>> spendingsData;
    private final ObservableList<XYChart.Series<Number, Number>> balanceData;
    private final IntegerProperty balanceLowerBound;
    private final IntegerProperty balanceUpperBound;
    private final ObjectProperty<LocalDate> startDate;
    private final ObjectProperty<LocalDate> endDate;

    private final ObservableList<Account> accounts;
    private final ComboBox<Account> accountFilter;

    /**
     * This account will be abused to specify we want to show data across all accounts. This is due to the fact that null misbehaves.
     */
    private final Account ALL_ACCOUNTS = new Account();

    public EvaluationView(ApplicationState state) {
        super("Evaluation", state);

        this.spendingsData = FXCollections.observableArrayList();
        this.balanceData = FXCollections.observableArrayList();
        this.balanceLowerBound = new SimpleIntegerProperty();
        this.balanceUpperBound = new SimpleIntegerProperty();
        this.startDate = new SimpleObjectProperty<>();
        this.endDate = new SimpleObjectProperty<>();

        this.accounts = FXCollections.observableArrayList();

        final var spendingChartAmountAxis = new NumberAxis();
        spendingChartAmountAxis.setForceZeroInRange(true);

        final PatchedStackedBarChart<String, Number> spendingsChart = new PatchedStackedBarChart<>(
                new CategoryAxis(),
                spendingChartAmountAxis,
                spendingsData
        );
        spendingsChart.setAnimated(false);

        final var balanceChartDayAxis = new NumberAxis();
        balanceChartDayAxis.setTickUnit(0.2);
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
        // No need, as we only have one series.
        balanceChart.setLegendVisible(false);

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

        accountFilter = new ComboBox<>();
        accountFilter.setItems(accounts);
        accountFilter.setButtonCell(new AccountComboBoxCell());
        accountFilter.setCellFactory(_ -> new AccountComboBoxCell());

        final HBox topBar = new HBox(
                startDatePicker,
                endDatePicker,
                accountFilter
        );
        topBar.setSpacing(2.5);

        startDate.addListener((_, _, _) -> updateCharts());
        endDate.addListener((_, _, _) -> updateCharts());
        accountFilter.getSelectionModel().selectedItemProperty().addListener((_, _, _) -> updateCharts());

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

    private class AccountComboBoxCell extends ListCell<@Nullable Account> {
        @Override
        protected void updateItem(@Nullable Account item, boolean empty) {
            super.updateItem(item, empty);

            if (ALL_ACCOUNTS.equals(item)) {
                setText("All");
            } else if (item != null) {
                setText(item.name);
            } else {
                setText("");
            }
        }
    }

    private void updateCharts() {
        final String unclassified = "unclassified";
        final String ignore = "ignore";
        final Map<String, @Nullable Map<Month, BigDecimal>> classificationToMonthToMoney = new HashMap<>();

        // FIXME Add initial balance.
        BigDecimal balance = new BigDecimal("0");
        final XYChart.Series<Number, Number> balanceSeries = new XYChart.Series<>();
        balanceData.setAll(balanceSeries);

        var sortedStream = state.data.payments.stream();
        final var account = accountFilter.getSelectionModel().selectedItemProperty().get();
        if (!ALL_ACCOUNTS.equals(account)) {
            sortedStream = sortedStream.filter(payment -> account.equals(payment.account));
        }
        final var sorted = sortedStream
                .sorted(Comparator.comparing(a -> a.effectiveDate))
                .toList();

        for (final var payment : sorted) {
            if (payment.effectiveDate.isBefore(startDate.get().atStartOfDay())) {
                continue;
            }
            if (payment.effectiveDate.isAfter(endDate.get().atStartOfDay())) {
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

            // We only want the latest datapoint for each point in time, to prevent having a day with 20 datapoints.
            // This also gets rid of unnecessary fluctations.
            if (!balanceSeries.getData().isEmpty() && balanceSeries.getData().getLast().getXValue().equals(chartX)) {
                balanceSeries.getData().set(balanceSeries.getData().size() - 1, new XYChart.Data<>(chartX, balance));
            } else {
                balanceSeries.getData().add(new XYChart.Data<>(chartX, balance));
            }

            // Separates balance from spendings.
            if (payment.amount.intValue() >= 0) {
                continue;
            }

            var leftOver = payment.amount;
            for (final var classification : payment.classifications) {
                // Add to the negative amount
                var amount = classification.amount.abs();
                leftOver = leftOver.add(amount);
                if (ignore.equals(classification.tag)) {
                    continue;
                }

                var entry = classificationToMonthToMoney.computeIfAbsent(classification.tag, _ -> new TreeMap<>());
                var value = entry.get(month);
                if (value != null) {
                    value = value.subtract(amount);
                } else {
                    value = amount.negate();
                }
                entry.put(month, value);
            }

            if (leftOver.compareTo(BigDecimal.ZERO) != 0) {
                final var entry = Objects.requireNonNull(classificationToMonthToMoney.computeIfAbsent(unclassified, _ -> new TreeMap<>()));
                var value = entry.get(month);
                if (value != null) {
                    value = leftOver.add(value);
                } else {
                    value = leftOver;
                }
                entry.put(month, value);
            }
        }

        for (final var dataPoint : balanceSeries.getData()) {
            Tooltip tooltip = new Tooltip();
            tooltip.setShowDelay(Duration.ZERO);
            tooltip.setText(String.valueOf(dataPoint.getYValue()));
            Tooltip.install(dataPoint.getNode(), tooltip);
        }

        if (!balanceSeries.getData().isEmpty()) {
            balanceLowerBound.setValue(Math.floor(balanceSeries.getData().getFirst().getXValue().doubleValue()));
            balanceUpperBound.setValue(Math.ceil(balanceSeries.getData().getLast().getXValue().doubleValue()));
        }

        final List<XYChart.Series<String, Number>> fragments = new ArrayList<>();
        for (final var category : classificationToMonthToMoney.entrySet()) {
            final XYChart.Series<String, Number> categorySeries = new XYChart.Series<>();
            categorySeries.setName(category.getKey());

            for (final var monthToAmount : Objects.requireNonNull(category.getValue()).entrySet()) {
                categorySeries.getData().add(new XYChart.Data<>(
                        renderMonth(monthToAmount.getKey()),
                        // We render positive values for now on, as the chart renders a 1 pixel gap between the series otherwise.
                        monthToAmount.getValue().abs()
                ));
            }

            fragments.add(categorySeries);
        }

        spendingsData.setAll(fragments);

        // Tooltips need to be set after adding the data, as they won't be added otherwise.
        for (final var fragment : fragments) {
            for (final var dataPoint : fragment.getData()) {
                Tooltip tooltip = new Tooltip();
                tooltip.setShowDelay(Duration.ZERO);
                tooltip.setText(fragment.getName() + ": " + dataPoint.getYValue());
                Tooltip.install(dataPoint.getNode(), tooltip);
            }
        }
    }

    private static String renderMonth(final Month month) {
        return month.getDisplayName(TextStyle.FULL, Locale.getDefault());
    }

    @Override
    protected void onTabActivated() {
        final var oldSelection = accountFilter.getSelectionModel().getSelectedItem();

        accounts.clear();
        // null is the all item
        accounts.add(ALL_ACCOUNTS);
        accounts.addAll(state.data.accounts);

        final var oldSelectionIndex = accountFilter.getItems().indexOf(oldSelection);
        if (oldSelectionIndex != -1) {
            accountFilter.getSelectionModel().select(oldSelectionIndex);
        } else {
            // null is the all item
            accountFilter.getSelectionModel().select(ALL_ACCOUNTS);
        }

        updateCharts();
    }

    @Override
    protected void onTabDeactivated() {
    }
}

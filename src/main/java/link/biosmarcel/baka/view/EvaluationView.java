package link.biosmarcel.baka.view;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
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
import javafx.util.StringConverter;
import link.biosmarcel.baka.ApplicationState;
import link.biosmarcel.baka.data.Account;
import link.biosmarcel.baka.view.component.BakaTab;
import link.biosmarcel.baka.view.component.PatchedStackedBarChart;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    private final ChangeListener<LocalDate> dateChangeListener;
    private final ChangeListener<Account> accountChangeListener;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy MMM");


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

        // Since we are using a number axis, but want our major ticks to be year+month, we have to normalise the dates
        // into an Double value. Days are added after the comma, as the tick unit is 1.0 in order to only display the
        // months as major ticks and no minor ticks. This way we can render months across years without losing a lot
        // of precision.
        final var balanceChartDayAxis = new NumberAxis();
        balanceChartDayAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(final Number object) {
                final var doubleValue = object.doubleValue();
                return LocalDate.of((int) (startDate.get().getYear() + doubleValue / 12), (int) (doubleValue % 12 + 1), 1).format(dateFormatter);
            }

            @Override
            public Number fromString(final String string) {
                return 0;
            }
        });
        balanceChartDayAxis.setTickUnit(1.0);
        balanceChartDayAxis.setMinorTickVisible(false);
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

        dateChangeListener = (_, _, _) -> updateCharts();
        accountChangeListener = (_, _, _) -> updateCharts();

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
        // The category axis expects a string. There's no way to easily change the data time and change how it is
        // rendered. As far as I can see, we'd have to write a completely custom axis. So instead we map from
        // category to yyyy-MMM to amount.
        final Map<String, @Nullable Map<String, BigDecimal>> classificationToMonthToMoney = new HashMap<>();

        BigDecimal balance = new BigDecimal("0");
        final XYChart.Series<Number, Number> balanceSeries = new XYChart.Series<>();
        balanceData.clear();

        var sortedStream = state.data.payments.stream();
        final var account = accountFilter.getSelectionModel().selectedItemProperty().get();
        if (!ALL_ACCOUNTS.equals(account)) {
            sortedStream = sortedStream.filter(payment -> account.equals(payment.account));
        }
        final var sorted = sortedStream
                .sorted(Comparator.comparing(a -> a.effectiveDate))
                .toList();

        final var baseYear = startDate.get().getYear();

        final var startDateWithTime = startDate.get().atStartOfDay();
        final var endDateWithTime = endDate.get().atStartOfDay();
        for (final var payment : sorted) {
            if (payment.effectiveDate.isBefore(startDateWithTime)) {
                // If we only show a partial balance, we have to take into account old data, as we'll otherwise have
                // an incorrect starting point. Let's say you made 10k from 2025 to 2027, but then show data from 2026
                // to 2027. You want to start at 5k, instead of 0k.
                balance = balance.add(payment.amount);
                continue;
            }
            if (payment.effectiveDate.isAfter(endDateWithTime)) {
                continue;
            }

            balance = balance.add(payment.amount);
            final double chartX =
                    (payment.effectiveDate.getYear() - baseYear) * 12
                            + (payment.effectiveDate.getMonthValue() - 1)
                            + (1.0 / payment.effectiveDate.getMonth().length(payment.effectiveDate.toLocalDate().isLeapYear()) * payment.effectiveDate.getDayOfMonth());
            // We only want the latest datapoint for each point in time, to prevent having a day with 20 data points.
            // This also gets rid of unnecessary fluctuations.
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
            final String yyyyMM = payment.effectiveDate.format(dateFormatter);
            for (final var classification : payment.classifications) {
                // Add to the negative amount
                var amount = classification.amount.abs();
                leftOver = leftOver.add(amount);
                if (ignore.equals(classification.tag)) {
                    continue;
                }

                var entry = classificationToMonthToMoney.computeIfAbsent(classification.tag, _ -> new TreeMap<>());
                var value = entry.get(yyyyMM);
                if (value != null) {
                    value = value.subtract(amount);
                } else {
                    value = amount.negate();
                }
                entry.put(yyyyMM, value);
            }

            if (leftOver.compareTo(BigDecimal.ZERO) != 0) {
                final var entry = Objects.requireNonNull(classificationToMonthToMoney.computeIfAbsent(unclassified, _ -> new TreeMap<>()));
                var value = entry.get(yyyyMM);
                if (value != null) {
                    value = leftOver.add(value);
                } else {
                    value = leftOver;
                }
                entry.put(yyyyMM, value);
            }
        }

        if (!balanceSeries.getData().isEmpty()) {
            balanceLowerBound.setValue(Math.floor(balanceSeries.getData().getFirst().getXValue().doubleValue()));
            balanceUpperBound.setValue(Math.ceil(balanceSeries.getData().getLast().getXValue().doubleValue()));
        }
        balanceData.setAll(balanceSeries);

        // It's import for this to happen after the data is added, as we won't have any tooltips otherwise.
        for (final var dataPoint : balanceSeries.getData()) {
            Tooltip tooltip = new Tooltip();
            tooltip.setShowDelay(Duration.ZERO);
            tooltip.setText(String.valueOf(dataPoint.getYValue()));
            Tooltip.install(dataPoint.getNode(), tooltip);
        }

        final List<XYChart.Series<String, Number>> fragments = new ArrayList<>();
        final var endYear = endDate.get().getYear();
        final var endMonth = endDate.get().getMonth().getValue();
        for (final var category : classificationToMonthToMoney.entrySet()) {
            final XYChart.Series<String, Number> categorySeries = new XYChart.Series<>();
            categorySeries.setName(category.getKey());

            // Hack to ensure that the order is correct in case there are any month values missing for a month, in which
            // case we can get incorrectly sorted months.
            for (
                    var month = startDate.get();
                    month.getMonthValue() <= endMonth && month.getYear() <= endYear;
                    month = month.plusMonths(1)
            ) {
                categorySeries.getData().add(new XYChart.Data<>(
                        month.format(dateFormatter),
                        0
                ));
            }

            for (final var monthToAmount : Objects.requireNonNull(category.getValue()).entrySet()) {
                categorySeries.getData().add(new XYChart.Data<>(
                        monthToAmount.getKey(),
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

    @Override
    public void onTabActivated() {
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

        startDate.addListener(dateChangeListener);
        endDate.addListener(dateChangeListener);
        accountFilter.getSelectionModel().selectedItemProperty().addListener(accountChangeListener);
    }

    @Override
    public void onTabDeactivated() {
        startDate.removeListener(dateChangeListener);
        endDate.removeListener(dateChangeListener);
        accountFilter.getSelectionModel().selectedItemProperty().removeListener(accountChangeListener);
    }
}

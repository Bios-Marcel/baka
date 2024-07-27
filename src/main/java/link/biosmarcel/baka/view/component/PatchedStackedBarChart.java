package link.biosmarcel.baka.view.component;

import javafx.beans.NamedArg;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.StackedBarChart;

import java.util.List;

/**
 * Fixes {@link StackedBarChart} not being able to display negative values. This is a JavaFX bug that existed for at
 * least 4 years, but isn't even in the bugtracker.
 * <p>
 * Code stolen from <a href="https://stackoverflow.com/a/68949840/5841827">StackOverflow</a>
 */
public class PatchedStackedBarChart<X, Y> extends StackedBarChart<X, Y> {
    public PatchedStackedBarChart(@NamedArg("xAxis") Axis<X> xAxis,
                                  @NamedArg("yAxis") Axis<Y> yAxis,
                                  @NamedArg("data") ObservableList<Series<X, Y>> data
    ) {
        super(xAxis, yAxis, data);
    }

    /**
     * Override the method that breaks the graph, patch to add missing "negative" CSS class.
     */
    @Override
    protected void dataItemAdded(Series<X, Y> series, int itemIndex, Data<X, Y> item) {
        super.dataItemAdded(series, itemIndex, item);

        Number val = (Number) (item.getYValue() instanceof Number ? item.getYValue() : item.getXValue());
        if (val.doubleValue() < 0) {
            // add missing CSS class
            item.getNode().getStyleClass().add("negative");
        }
    }

    /**
     * Override the method that breaks the graph, patch so it doesn't override styles.
     */
    @Override
    protected void seriesChanged(ListChangeListener.Change<? extends Series> c) {
        for (int i = 0; i < getData().size(); i++) {
            List<Data<X, Y>> items = getData().get(i).getData();
            for (int j = 0; j < items.size(); j++) {
                Node bar = items.get(j).getNode();
                // change .setAll to .addAll to avoid overriding styles
                bar.getStyleClass().removeIf(s -> s.matches("chart-bar|(series|data)\\d+"));
                bar.getStyleClass().addAll("chart-bar", "series" + i, "data" + j);
            }
        }
    }
}

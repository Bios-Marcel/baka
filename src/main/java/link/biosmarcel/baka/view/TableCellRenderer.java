package link.biosmarcel.baka.view;

import javafx.scene.control.TableCell;

import java.util.function.Function;

public class TableCellRenderer<S, T> extends TableCell<S, T> {
    private final Function<T, String> render;

    public TableCellRenderer(Function<T, String> render) {
        this.render = render;

        this.setGraphic(null);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            this.setText(null);
        } else {
            this.setText(render.apply(item));
        }
    }
}

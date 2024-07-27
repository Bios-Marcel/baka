package link.biosmarcel.baka.view.component;

import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class PopupPane extends AnchorPane {
    private @Nullable Node popup;

    public PopupPane(final Node center) {
        getChildren().add(center);
        AnchorPane.setBottomAnchor(center, 0.0);
        AnchorPane.setLeftAnchor(center, 0.0);
        AnchorPane.setRightAnchor(center, 0.0);
        AnchorPane.setTopAnchor(center, 0.0);
    }

    public void hidePopup() {
        if (popup == null) {
            return;
        }

        getChildren().remove(popup);
        popup.setManaged(false);
        popup.setVisible(false);
        popup = null;
    }

    public void showPopup(final Node popup) {
        if (!Objects.equals(popup, this.popup)) {
            hidePopup();

            getChildren().add(popup);
            this.popup = popup;
        }

        popup.setViewOrder(-1);
        popup.setManaged(true);
        popup.setVisible(true);
    }
}

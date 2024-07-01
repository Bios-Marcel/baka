package link.biosmarcel.baka.view;

import javafx.scene.control.Tab;
import link.biosmarcel.baka.ApplicationState;

public abstract class BakaTab extends Tab {
    protected final ApplicationState state;

    public BakaTab(final String title, final ApplicationState state) {
        super(title);

        this.state = state;

        selectedProperty().addListener((_, _, newValue) -> {
            if (newValue) {
                onTabActivated();
            } else {
                onTabDeactivated();
            }
        });
    }

    /**
     * Called once the tab is entered. Should be used to load the UI with data and register any listeners.
     */
    protected abstract void onTabActivated();

    /**
     * Called once the tab is exited. Should be used to remove any data and active listeners.
     */
    protected abstract void onTabDeactivated();
}

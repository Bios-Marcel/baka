package link.biosmarcel.baka.view.component;

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
                save();
                onTabDeactivated();
            }
        });
    }

    /**
     * Called once the tab is entered. Should be used to load the UI with data and register any listeners.
     */
    public abstract void onTabActivated();

    /**
     * Called once the tab is exited. Should be used to remove any data and active listeners.
     */
    public abstract void onTabDeactivated();

    /**
     * Views have the option to perform a store + commit upon this invocation. When save is called should not be the
     * views concern, but the applications concern. You should optimally never have to call save.
     */
    public void save() {
        // Do nothing by default
    }
}

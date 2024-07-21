package link.biosmarcel.baka;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import link.biosmarcel.baka.data.Data;
import org.eclipse.serializer.persistence.types.Storer;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;

public class ApplicationState {
    public final EmbeddedStorageManager storageManager;
    public final Data data;
    public final Storer storer;
    public final BooleanProperty debugMode = new SimpleBooleanProperty();

    public ApplicationState(EmbeddedStorageManager storageManager, Storer storer, Data data) {
        this.storageManager = storageManager;
        this.storer = storer;
        this.data = data;
    }
}

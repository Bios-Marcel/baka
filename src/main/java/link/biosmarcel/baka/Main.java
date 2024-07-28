package link.biosmarcel.baka;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import link.biosmarcel.baka.data.Data;
import link.biosmarcel.baka.view.*;
import link.biosmarcel.baka.view.component.BakaTab;
import link.biosmarcel.baka.view.component.PopupPane;
import org.eclipse.store.storage.embedded.configuration.types.EmbeddedStorageConfiguration;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Main extends Application {
    private @Nullable String getParameter(final String key) {
        boolean returnNext = false;
        for (final var param : getParameters().getUnnamed()) {
            if (key.equalsIgnoreCase(param)) {
                returnNext = true;
                continue;
            }

            if (returnNext) {
                return param;
            }
        }

        return null;
    }

    @Override
    public void start(Stage stage) {
        final Path dataRootDir;
        final String dataDirOverride = getParameter("--data-dir");
        if (dataDirOverride != null) {
            dataRootDir = Paths.get(dataDirOverride).toAbsolutePath();
        } else {
            dataRootDir = Paths.get(System.getenv("APPDATA"), "baka").toAbsolutePath();
        }

        final Data data = new Data();
        final var storageManager = EmbeddedStorageConfiguration
                .Builder()
                .setStorageDirectory(Paths.get(dataRootDir.toString(), "storage").toString())
                .setBackupDirectory(Paths.get(dataRootDir.toString(), "backup").toString())
                .setChannelCount(1)
                .createEmbeddedStorageFoundation()
                //.onConnectionFoundation((connection) -> {
                //    // Log which fields are persisted.
                //    connection.setFieldEvaluatorPersistable((entityType, field) -> {
                //        final var result = Persistence.isPersistableField(entityType, field);
                //        System.out.println("'" + field.getName() + "' " + field.getType() + "=" + result);
                //        return result;
                //    });
                //})
                .setRoot(data)
                .start();

        final ApplicationState state = new ApplicationState(storageManager, storageManager.createEagerStorer(), data);
        final DebugView debugView = new DebugView(state);
        final TabPane tabs = new TabPane(
                new PaymentsView(state),
                new AccountsView(state),
                new EvaluationView(state),
                new ClassificationsView(state)
        );
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        state.debugMode.addListener((_, _, newValue) -> {
            if (newValue) {
                if (!tabs.getTabs().contains(debugView)) {
                    tabs.getTabs().add(debugView);
                }
            } else {
                tabs.getTabs().remove(debugView);
            }
        });

        final var popupPane = new PopupPane(tabs);
        final Scene scene = new Scene(popupPane, 800, 600);
        scene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("base.css")).toExternalForm());
        stage.setTitle("Baka");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("icon.png"))));
        stage.setScene(scene);

        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN), () -> {
            state.debugMode.set(!state.debugMode.getValue());
        });

        stage.setOnCloseRequest(_ -> {
            // Triggers onTabDeactivate
            ((BakaTab) tabs.getSelectionModel().getSelectedItem()).save();
            // We take a while to flush, but want to prevent the shutdown being perceived as slow.
            stage.hide();

            // Not shutting down might result in data loss
            storageManager.shutdown();
            Platform.exit();
        });

        stage.sizeToScene();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
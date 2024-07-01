package link.biosmarcel.baka;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import link.biosmarcel.baka.data.Data;
import link.biosmarcel.baka.view.AccountsView;
import link.biosmarcel.baka.view.EvaluationView;
import link.biosmarcel.baka.view.PaymentsView;
import org.eclipse.store.storage.embedded.configuration.types.EmbeddedStorageConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Main extends Application {
    private static Path getDataDir(final String... children) {
        final String[] pathElements = new String[children.length + 1];
        pathElements[0] = "baka";
        System.arraycopy(children, 0, pathElements, 1, children.length);
        return Paths.get(System.getenv("APPDATA"), pathElements);
    }

    @Override
    public void start(Stage stage) {
        final Data data = new Data();
        final var storageManager = EmbeddedStorageConfiguration
                .Builder()
                .setStorageDirectory(getDataDir("storage").toString())
                .setBackupDirectory(getDataDir("backup").toString())
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

        ApplicationState state = new ApplicationState(storageManager, storageManager.createEagerStorer(), data);

        TabPane tabs = new TabPane(
                new PaymentsView(state),
                new AccountsView(state),
                new EvaluationView(state)
        );
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Scene scene = new Scene(tabs, 800, 600);
        scene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("base.css")).toExternalForm());
        stage.setTitle("Baka");
        stage.setScene(scene);

        // FIXME This seems dumb?
        Platform.setImplicitExit(true);
        stage.setOnCloseRequest((ae) -> {
            Platform.exit();
            System.exit(0);
        });

        stage.sizeToScene();
        stage.show();
    }

    public static void main(String[] __) {
        launch();
    }
}
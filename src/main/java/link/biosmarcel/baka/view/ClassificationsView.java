package link.biosmarcel.baka.view;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import link.biosmarcel.baka.ApplicationState;
import link.biosmarcel.baka.data.ClassificationRule;
import org.jspecify.annotations.Nullable;

public class ClassificationsView extends BakaTab {
    private final ListView<ClassificationRule> listView;

    public ClassificationsView(ApplicationState state) {
        super("Classifications", state);

        listView = new ListView<>();

        // FIXME Add name, make adjustable
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ClassificationRule item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText("");
                } else {
                    setText(item.name);
                }
            }
        });
        ReadOnlyObjectProperty<@Nullable ClassificationRule> classificationRuleReadOnlyObjectProperty = listView.getSelectionModel().selectedItemProperty();

        final TextField nameField = new TextField();
        nameField.textProperty().addListener((_, _, newValue) -> {
            var selectedClassification = classificationRuleReadOnlyObjectProperty.getValue();
            if (selectedClassification != null) {
                selectedClassification.name = newValue;
            }
            state.storer.store(selectedClassification);
            state.storer.commit();
        });
        final TextField tagField = new TextField();
        tagField.textProperty().addListener((_, _, newValue) -> {
            var selectedClassification = classificationRuleReadOnlyObjectProperty.getValue();
            if (selectedClassification != null) {
                selectedClassification.tag = newValue;
            }
            state.storer.store(selectedClassification);
            state.storer.commit();
        });
        final TextArea queryField = new TextArea();
        queryField.textProperty().addListener((_, _, newValue) -> {
            var selectedClassification = classificationRuleReadOnlyObjectProperty.getValue();
            if (selectedClassification != null) {
                selectedClassification.query = newValue;
            }
            state.storer.store(selectedClassification);
            state.storer.commit();
        });

        classificationRuleReadOnlyObjectProperty.addListener((_, _, newValue) -> {
            if (newValue == null) {
                nameField.setDisable(true);
                tagField.setDisable(true);
                queryField.setDisable(true);
                return;
            }

            nameField.setDisable(false);
            tagField.setDisable(false);
            queryField.setDisable(false);

            nameField.setText(newValue.name);
            tagField.setText(newValue.tag);
            queryField.setText(newValue.query);
        });

        final var details = new GridPane(5.0, 10.0);
        details.add(new Label("Name"), 0, 0);
        details.add(nameField, 1, 0);
        details.add(new Label("Tag"), 0, 1);
        details.add(tagField, 1, 1);
        details.add(new Label("Query"), 0, 2);
        details.add(queryField, 1, 2);

        final var newButton = new Button("New");
        newButton.setOnAction(_ -> {
            final var newClassification = new ClassificationRule();
            state.data.classificationRules.add(newClassification);

            state.storer.store(state.data.classificationRules);
            if (state.data.classificationRules.size() == 1) {
                state.storer.store(state.data);
            }
            state.storer.commit();

            listView.getItems().add(newClassification);
            listView.getSelectionModel().select(newClassification);
        });
        final var deleteButton = new Button("Delete");
        deleteButton.setOnAction(_ -> {
            final var selectedClassification = classificationRuleReadOnlyObjectProperty.getValue();
            if (selectedClassification == null) {
                return;
            }

            state.data.classificationRules.remove(selectedClassification);
            state.storer.store(state.data.classificationRules);
            state.storer.commit();

            listView.getItems().remove(selectedClassification);
        });

        final var upButton = new Button("↑");
        upButton.setOnAction(_ -> {
            final var selectedClassification = classificationRuleReadOnlyObjectProperty.getValue();
            if (selectedClassification == null) {
                return;
            }

            final int index = state.data.classificationRules.indexOf(selectedClassification);
            if (index == 0) {
                return;
            }

            state.data.classificationRules.set(index, state.data.classificationRules.get(index - 1));
            state.data.classificationRules.set(index - 1, selectedClassification);
            state.storer.store(state.data.classificationRules);
            state.storer.commit();

            // FIXME delta change
            listView.getItems().setAll(state.data.classificationRules);
            listView.getSelectionModel().select(selectedClassification);
        });
        final var downButton = new Button("↓");
        downButton.setOnAction(_ -> {
            final var selectedClassification = classificationRuleReadOnlyObjectProperty.getValue();
            if (selectedClassification == null) {
                return;
            }

            final int index = state.data.classificationRules.indexOf(selectedClassification);
            if (index == state.data.classificationRules.size() - 1) {
                return;
            }

            state.data.classificationRules.set(index, state.data.classificationRules.get(index + 1));
            state.data.classificationRules.set(index + 1, selectedClassification);
            state.storer.store(state.data.classificationRules);
            state.storer.commit();

            // FIXME delta change
            listView.getItems().setAll(state.data.classificationRules);
            listView.getSelectionModel().select(selectedClassification);
        });
        final var listViewButtons = new HBox(
                newButton,
                deleteButton,
                upButton,
                downButton
        );
        listViewButtons.setSpacing(5.0);

        final var layout = new GridPane(5.0, 2.5);
        layout.add(listViewButtons, 0, 0);
        layout.add(listView, 0, 1);
        layout.add(details, 1, 1);
        layout.setPadding(new Insets(10, 10, 10, 10));
        setContent(layout);
    }

    @Override
    protected void onTabActivated() {
        System.out.println(state.data.classificationRules.size());
        listView.getItems().setAll(state.data.classificationRules);
    }

    @Override
    protected void onTabDeactivated() {

    }
}

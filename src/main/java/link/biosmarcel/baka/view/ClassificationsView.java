package link.biosmarcel.baka.view;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import link.biosmarcel.baka.ApplicationState;
import link.biosmarcel.baka.data.ClassificationRule;
import link.biosmarcel.baka.filter.IncompleteQueryException;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ClassificationsView extends BakaTab {
    private final ListView<ClassificationRuleFX> listView;
    private final TextField nameField;
    private final TextField tagField;
    private final AutocompleteTextArea queryField;
    private final ReadOnlyObjectProperty<@Nullable ClassificationRuleFX> selectedRuleProperty;


    public ClassificationsView(ApplicationState state) {
        super("Classifications", state);

        listView = new ListView<>();

        // FIXME Abstract this away for when we have multiple list views.
        listView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(@Nullable ClassificationRuleFX item, boolean empty) {
                super.updateItem(item, empty);
                textProperty().unbind();
                if (item == null) {
                    setText(null);
                } else {
                    textProperty().bind(item.name);
                }
            }
        });

        selectedRuleProperty = listView.getSelectionModel().selectedItemProperty();
        nameField = new TextField();
        tagField = new TextField();
        final var autocompleteFilter = new PaymentFilter();
        queryField = new AutocompleteTextArea((value) -> {
            try {
                autocompleteFilter.setQuery(value);
                return Collections.EMPTY_LIST;
            } catch (final IncompleteQueryException exception) {
                if (value.endsWith(exception.token) &&
                        (!exception.token.isEmpty() || value.isEmpty() || !value.stripTrailing().equalsIgnoreCase(value))
                ) {
                    return exception.options;
                }
                return Collections.EMPTY_LIST;
            } catch (final RuntimeException exception) {
                System.out.println(exception.getMessage());
                return Collections.EMPTY_LIST;
            }
        });

        final BooleanBinding disableInputs = Bindings.createBooleanBinding(() -> selectedRuleProperty.getValue() == null, selectedRuleProperty);
        selectedRuleProperty.addListener((_, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.apply();

                oldValue.name.unbindBidirectional(nameField.textProperty());
                oldValue.tag.unbindBidirectional(tagField.textProperty());
                oldValue.query.unbindBidirectional(queryField.textProperty());

                nameField.setText("");
                tagField.setText("");
                queryField.textProperty().set("");
            }

            if (newValue != null) {
                nameField.textProperty().bindBidirectional(newValue.name);
                tagField.textProperty().bindBidirectional(newValue.tag);
                queryField.textProperty().bindBidirectional(newValue.query);
            }
        });
        nameField.disableProperty().bind(disableInputs);
        tagField.disableProperty().bind(disableInputs);
        queryField.disableProperty().bind(disableInputs);

        final var details = new GridPane(5.0, 10.0);
        details.add(new Label("Name"), 0, 0);
        details.add(nameField, 1, 0);
        details.add(new Label("Tag"), 0, 1);
        details.add(tagField, 1, 1);
        details.add(new Label("Query"), 0, 2);
        details.add(queryField.getNode(), 1, 2);

        final var newButton = new Button("New");
        newButton.setOnAction(_ -> {
            final var newClassification = new ClassificationRule();
            state.data.classificationRules.add(newClassification);

            final var newRuleFX = new ClassificationRuleFX(newClassification);
            listView.getItems().add(newRuleFX);
            listView.getSelectionModel().select(newRuleFX);
        });
        final var deleteButton = new Button("Delete");
        deleteButton.setOnAction(_ -> {
            final var selectedClassification = selectedRuleProperty.getValue();
            if (selectedClassification == null) {
                return;
            }

            state.data.classificationRules.remove(selectedClassification.rule);

            listView.getSelectionModel().select(listView.getSelectionModel().getSelectedIndex() + 1);
            listView.getItems().remove(selectedClassification);
        });

        final var upButton = new Button("↑");
        upButton.setOnAction(_ -> {
            final var selectedClassification = selectedRuleProperty.getValue();
            if (selectedClassification == null) {
                return;
            }

            final int index = state.data.classificationRules.indexOf(selectedClassification.rule);
            if (index == 0) {
                return;
            }

            state.data.classificationRules.set(index, state.data.classificationRules.get(index - 1));
            state.data.classificationRules.set(index - 1, selectedClassification.rule);

            final int oldViewIndex = listView.getItems().indexOf(selectedClassification);
            listView.getItems().set(oldViewIndex, listView.getItems().get(oldViewIndex - 1));
            listView.getItems().set(index - 1, selectedClassification);
            listView.getSelectionModel().select(selectedClassification);
        });
        final var downButton = new Button("↓");
        downButton.setOnAction(_ -> {
            final var selectedClassification = selectedRuleProperty.getValue();
            if (selectedClassification == null) {
                return;
            }

            final int index = state.data.classificationRules.indexOf(selectedClassification.rule);
            if (index == state.data.classificationRules.size() - 1) {
                return;
            }

            state.data.classificationRules.set(index, state.data.classificationRules.get(index + 1));
            state.data.classificationRules.set(index + 1, selectedClassification.rule);

            final int oldViewIndex = listView.getItems().indexOf(selectedClassification);
            listView.getItems().set(oldViewIndex, listView.getItems().get(oldViewIndex + 1));
            listView.getItems().set(index + 1, selectedClassification);
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


    private List<ClassificationRuleFX> convertRules(final Collection<ClassificationRule> elements) {
        final List<ClassificationRuleFX> newFXElements = new ArrayList<>(elements.size());
        for (final var element : elements) {
            newFXElements.add(new ClassificationRuleFX(element));
        }
        return newFXElements;
    }

    @Override
    protected void onTabActivated() {
        listView.getItems().setAll(convertRules(state.data.classificationRules));

        if (!listView.getItems().isEmpty()) {
            listView.getSelectionModel().select(0);
        }

        // Won't work otherwise, due to the event being fired relatively early.
        Platform.runLater(listView::requestFocus);
    }

    @Override
    protected void onTabDeactivated() {
        listView.getItems().clear();
    }

    @Override
    public void save() {
        final var selected = selectedRuleProperty.get();
        if (selected != null) {
            selected.apply();
        }

        // Just storing the classifications will cause issues if it hasn't been persisted before.
        state.storer.store(state.data);
        state.storer.commit();
    }
}

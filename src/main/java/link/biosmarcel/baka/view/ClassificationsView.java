package link.biosmarcel.baka.view;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import link.biosmarcel.baka.ApplicationState;
import link.biosmarcel.baka.data.ClassificationRule;
import link.biosmarcel.baka.filter.FilterAutocompleteGenerator;
import link.biosmarcel.baka.filter.IncompleteQueryException;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class ClassificationsView extends BakaTab {
    private final ListView<ClassificationRuleFX> listView;
    private final TextField nameField;
    private final AutocompleteField tagField;
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
        tagField = new AutocompleteField(new char[0], string -> {
            final var lowered = string.toLowerCase().stripLeading();
            return availableTags.stream().filter(tag -> tag.startsWith(lowered) && !tag.equals(lowered)).toList();
        });
        tagField.setInsertSpaceAfterCompletion(false);
        // necessary to prevent the popup from clashing with queryField.
//        tagField.setViewOrder(-2);

        final PaymentFilter filter = new PaymentFilter();
        queryField = new AutocompleteTextArea(
                new char[]{')', '(', ' ', '\n'},
                new FilterAutocompleteGenerator(filter)::generate
        );

        StringProperty filterError = new SimpleStringProperty();
        BooleanProperty fatalError = new SimpleBooleanProperty();
        AutocompleteHelper.installErrorToolTip(queryField, filterError, fatalError);

        queryField.textProperty().addListener((_, _, newText) -> {
            try {
                filterError.set("");
                fatalError.set(false);
                filter.setQuery(newText);
            } catch (final IncompleteQueryException exception) {
                if (!exception.empty) {
                    filterError.set("Query is incomplete.");
                    fatalError.set(false);
                }
                // If the query is not empty, but incomplete, it isn't really an issue.
            } catch (final RuntimeException exception) {
                filterError.set(exception.getMessage());
                fatalError.set(true);
            }
        });

        final BooleanBinding disableInputs = Bindings.createBooleanBinding(() -> selectedRuleProperty.getValue() == null, selectedRuleProperty);
        selectedRuleProperty.addListener((_, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.apply();

                oldValue.name.unbindBidirectional(nameField.textProperty());
                oldValue.tag.unbindBidirectional(tagField.textProperty());
                oldValue.query.unbindBidirectional(queryField.textProperty());

                nameField.textProperty().set("");
                tagField.textProperty().set("");
                queryField.textProperty().set("");
            }

            if (newValue != null) {
                nameField.textProperty().bindBidirectional(newValue.name);
                tagField.textProperty().bindBidirectional(newValue.tag);
                queryField.textProperty().bindBidirectional(newValue.query);
            }

            addTagsFromRules();
        });
        nameField.disableProperty().bind(disableInputs);
        tagField.disableProperty().bind(disableInputs);
        queryField.disableProperty().bind(disableInputs);

        final var details = new GridPane(5.0, 10.0);
        details.add(new Label("Name"), 0, 0);
        details.add(nameField, 1, 0);
        details.add(new Label("Tag"), 0, 1);
        details.add(tagField.getNode(), 1, 1);
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

    private final Set<String> availableTags = new HashSet<>();

    private void addTagsFromRules() {
        for (final var rule : state.data.classificationRules) {
            if (!rule.tag.isBlank()) {
                // We temporarily strip here, as the final stripping happens on save.
                availableTags.add(rule.tag.strip().toLowerCase());
            }
        }
    }

    @Override
    protected void onTabActivated() {
        availableTags.clear();
        for (final var payment : state.data.payments) {
            for (final var classification : payment.classifications) {
                if (classification.tag != null && !classification.tag.isBlank()) {
                    availableTags.add(classification.tag.toLowerCase());
                }
            }
        }
        addTagsFromRules();

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

        // We currently directly bind the property, so any entry is accepted as is, hence we have to correct it
        // before saving for now. This is an implementation detail however and should not cause breakage later on.
        for (final var rule : state.data.classificationRules) {
            rule.tag = rule.tag.strip();
        }

        // Just storing the classifications will cause issues if it hasn't been persisted before.
        state.storer.store(state.data);
        state.storer.commit();
    }
}

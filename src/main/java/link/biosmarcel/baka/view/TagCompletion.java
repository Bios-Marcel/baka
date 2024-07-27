package link.biosmarcel.baka.view;

import link.biosmarcel.baka.ApplicationState;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TagCompletion {
    private final Set<String> availableTags = new HashSet<>();
    private final ApplicationState state;

    public TagCompletion(final ApplicationState state) {
        this.state = state;
    }

    private void addTagsFromRules() {
        for (final var rule : state.data.classificationRules) {
            if (!rule.tag.isBlank()) {
                // We temporarily strip here, as the final stripping happens on save.
                availableTags.add(rule.tag.strip().toLowerCase());
            }
        }
    }

    private void addTagsFromPayments() {
        for (final var payment : state.data.payments) {
            for (final var classification : payment.classifications) {
                if (!classification.tag.isBlank()) {
                    availableTags.add(classification.tag.toLowerCase());
                }
            }
        }
    }

    public void update() {
        availableTags.clear();
        addTagsFromPayments();
        addTagsFromRules();
    }

    public List<String> match(final String text) {
        final var lowered = text.toLowerCase().stripLeading();
        return availableTags.stream().filter(tag -> tag.startsWith(lowered) && !tag.equals(lowered)).toList();
    }
}

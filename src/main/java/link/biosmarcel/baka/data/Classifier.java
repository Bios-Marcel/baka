package link.biosmarcel.baka.data;

import link.biosmarcel.baka.view.CompiledClassificationRule;

import java.util.List;

public final class Classifier {
    private final List<CompiledClassificationRule> compiledRules;

    public Classifier(
            final List<ClassificationRule> rules
    ) {
        this.compiledRules = rules.stream().map(ClassificationRule::compile).toList();
    }

    public boolean classify(final Payment payment) {
        // If we have no classifications, or only automated ones, we can override them.
        // Otherwise, we might risk overwriting userdata, which we want to avoid for now.
        for(final var classification : payment.classifications) {
            if(!classification.automated) {
                return false;
            }
        }

        for (final var rule : compiledRules) {
            if (!rule.test(payment)) {
                continue;
            }

            final var classification = new Classification();
            classification.tag = rule.tag;
            classification.amount = payment.amount;
            classification.automated = true;
            payment.classifications.add(classification);

            // Automated classification doesn't allow multi classification, so we early exit.
            return true;
        }

        return false;
    }
}
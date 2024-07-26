package link.biosmarcel.baka.view;

import link.biosmarcel.baka.data.Payment;

import java.util.function.Predicate;

/**
 * This is snapshot of a {@link link.biosmarcel.baka.data.ClassificationRule}, also containing a pre-compiled predicate.
 */
public class CompiledClassificationRule {
    public final String name;
    public final String tag;
    public final String query;
    private final Predicate<Payment> compiled;

    public CompiledClassificationRule(final String name, final String tag, final String query) {
        this.name = name;
        this.tag = tag;
        this.query = query;
        this.compiled = compile();
    }

    private Predicate<Payment> compile() {
        try {
            final var compiled = new PaymentFilter();
            compiled.setQuery(query);
            return compiled;
        } catch (final RuntimeException exception) {
            System.out.println("Error: " + exception.getMessage());
            return _ -> false;
        }
    }

    public boolean test(final Payment payment) {
        return compiled.test(payment);
    }
}

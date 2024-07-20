package link.biosmarcel.baka.data;

import link.biosmarcel.baka.view.PaymentFilter;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

public class ClassificationRule {
    public String name = "";
    public String tag = "";
    public String query = "";

    public void setQuery(final String query) {
        this.compiled = null;
        this.query = query;
    }

    private transient @Nullable Predicate<Payment> compiled = null;

    public boolean test(final Payment payment) {
        if (compiled == null) {
            final var compiled = new PaymentFilter();
            try {
                compiled.setQuery(query);
                this.compiled = compiled;
            } catch (final RuntimeException exception) {
                // FIXME This should give user feedback
                this.compiled = _ -> false;
            }
        }

        return compiled.test(payment);
    }
}

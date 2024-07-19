package link.biosmarcel.baka.data;

import link.biosmarcel.baka.view.PaymentFilter;
import org.jspecify.annotations.Nullable;

public class ClassificationRule {
    public String name = "";
    public String tag = "";
    public String query = "";

    public void setQuery(final String query) {
        this.compiled = null;
        this.query = query;
    }

    private transient @Nullable PaymentFilter compiled = null;

    public boolean test(final Payment payment) {
        if (compiled == null) {
            compiled = new PaymentFilter();
            try {
                compiled.setQuery(query);
            } catch (final RuntimeException exception) {
                return false;
            }
        }

        return compiled.test(payment);
    }
}

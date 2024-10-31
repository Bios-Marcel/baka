package link.biosmarcel.baka.data;

import link.biosmarcel.baka.view.CompiledClassificationRule;

public class ClassificationRule {
    public String name = "";
    public String tag = "";
    public String query = "";

    /**
     * For negative (outgoing) payments, this will make sure that the payment does not count as a spending.
     */
    public boolean ignoreSpending = false;

    public CompiledClassificationRule compile() {
        return new CompiledClassificationRule(name, tag, ignoreSpending, query);
    }
}

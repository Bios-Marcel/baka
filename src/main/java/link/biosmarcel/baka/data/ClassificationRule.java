package link.biosmarcel.baka.data;

import link.biosmarcel.baka.view.CompiledClassificationRule;

public class ClassificationRule {
    public String name = "";
    public String tag = "";
    public String query = "";

    public CompiledClassificationRule compile() {
        return new CompiledClassificationRule(name, tag, query);
    }
}

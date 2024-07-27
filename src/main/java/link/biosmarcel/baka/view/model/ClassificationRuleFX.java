package link.biosmarcel.baka.view.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import link.biosmarcel.baka.data.ClassificationRule;

public class ClassificationRuleFX {

    public final ClassificationRule rule;

    public final StringProperty name = new SimpleStringProperty("");
    public final StringProperty tag = new SimpleStringProperty("");
    public final StringProperty query = new SimpleStringProperty("");

    public ClassificationRuleFX(ClassificationRule rule) {
        this.rule = rule;

        name.set(rule.name);
        tag.set(rule.tag);
        query.set(rule.query);
    }

    public void apply() {
        rule.name = name.get();
        rule.tag = tag.get();
        rule.query = query.get();
    }
}

package link.biosmarcel.baka.view.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import link.biosmarcel.baka.data.ClassificationRule;

public class ClassificationRuleFX {
    public final ClassificationRule rule;

    public final StringProperty name = new SimpleStringProperty("");
    public final StringProperty tag = new SimpleStringProperty("");
    public final StringProperty query = new SimpleStringProperty("");
    public final BooleanProperty ignoreSpending = new SimpleBooleanProperty();

    public ClassificationRuleFX(ClassificationRule rule) {
        this.rule = rule;

        name.set(rule.name);
        tag.set(rule.tag);
        query.set(rule.query);
        ignoreSpending.set(rule.ignoreSpending);
    }

    public void apply() {
        rule.name = name.get();
        rule.tag = tag.get();
        rule.query = query.get();
        rule.ignoreSpending = ignoreSpending.get();
    }
}

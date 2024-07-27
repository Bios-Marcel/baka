package link.biosmarcel.baka.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import link.biosmarcel.baka.data.Classification;

import java.math.BigDecimal;

public class ClassificationFX {
    public final Classification classification;

    public final StringProperty tag = new SimpleStringProperty("");
    public final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>(BigDecimal.ZERO);

    public ClassificationFX(final Classification classification) {
        this.classification = classification;

        tag.set(classification.tag);
        amount.set(classification.amount);
    }

    public void apply() {
        classification.amount = amount.get();
        classification.tag = tag.get();
    }
}

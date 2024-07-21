package link.biosmarcel.baka.filter;

import org.jspecify.annotations.Nullable;

public enum Operator {
    EQ("="),
    NOT_EQ("!="),
    HAS("has"),
    LT("<"),
    LT_EQ("<="),
    GT(">"),
    GT_EQ(">=");

    public final String text;

    Operator(final String text) {
        this.text = text;
    }

    public static @Nullable Operator match(final String text) {
        for (final var operator : Operator.values()) {
            if (operator.text.equalsIgnoreCase(text)) {
                return operator;
            }
        }

        return null;
    }
}

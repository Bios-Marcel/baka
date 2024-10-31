package link.biosmarcel.baka.filter;

import org.jspecify.annotations.Nullable;

/**
 * Logical operators that can be used inside an {@link Expression}.
 */
public enum Operator {
    EQ("="),
    NOT_EQ("!="),
    HAS("has"),
    LT("<"),
    LT_EQ("<="),
    GT(">"),
    GT_EQ(">=");

    /**
     * Identifier for this operator.
     */
    public final String text;

    /**
     *
     * @param text {@link #text}
     */
    Operator(final String text) {
        this.text = text;
    }

    /**
     * Looks up the matching operator for the given input.
     *
     * @param text operator input
     *
     * @return matching {@link Operator} or {@code null} if no match was found
     */
    public static @Nullable Operator match(final String text) {
        for (final var operator : Operator.values()) {
            if (operator.text.equalsIgnoreCase(text)) {
                return operator;
            }
        }

        return null;
    }
}

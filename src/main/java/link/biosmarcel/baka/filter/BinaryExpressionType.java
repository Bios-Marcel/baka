package link.biosmarcel.baka.filter;

/**
 * Binary expressions are a combination of two other expressions, such as `(a == b) OR (c == d)`.
 */
public enum BinaryExpressionType {
    /**
     * Evaluate to {@code true} if both sides of the expression evaluate to {@code true}
     */
    AND("and"),
    /**
     * Evaluate to {@code true} if either side of the expression evaluates to {@code true}.
     * If the left-hand side evaluates to true, the right side needn't be executed anymore.
     */
    OR("or"),
    ;

    /**
     * Identifier for this expression.
     */
    public final String text;

    BinaryExpressionType(String text) {
        this.text = text;
    }
}
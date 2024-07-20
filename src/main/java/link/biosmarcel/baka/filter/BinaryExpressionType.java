package link.biosmarcel.baka.filter;

public enum BinaryExpressionType {
    AND("and"),
    OR("or"),
    ;

    public final String text;

    BinaryExpressionType(String text) {
        this.text = text;
    }
}
package link.biosmarcel.baka.filter;

public enum BinaryExpressionType {
    AND("AND"),
    OR("OR"),
    ;

    public final String text;

    BinaryExpressionType(String text) {
        this.text = text;
    }
}
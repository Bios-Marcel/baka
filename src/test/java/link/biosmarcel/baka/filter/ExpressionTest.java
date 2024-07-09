package link.biosmarcel.baka.filter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExpressionTest {
    class Dummy {
        String name;
    }

    @Test
    public void test_and() {
        final var tay = new Dummy();
        tay.name = "tay";
        final var taylor = new Dummy();
        taylor.name = "taylor";
        final var fred = new Dummy();
        fred.name = "fred";

        final var expression = new Expression<Dummy>();
        expression.predicate = item -> item.name.contains("tay");
        expression.binaryExpressionType = BinaryExpressionType.AND;
        final var next = new Expression<Dummy>();
        next.predicate = item -> item.name.contains("lor");
        expression.next = next;

        Assertions.assertTrue(expression.test(taylor));
        Assertions.assertFalse(expression.test(tay));
        Assertions.assertFalse(expression.test(fred));
    }
}
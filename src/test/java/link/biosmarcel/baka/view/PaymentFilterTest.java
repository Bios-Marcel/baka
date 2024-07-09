package link.biosmarcel.baka.view;

import link.biosmarcel.baka.data.Account;
import link.biosmarcel.baka.data.Payment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

class PaymentFilterTest {
    /**
     * Tests whether expression types (expression, binary expression and grouped expression) are correctly parsed and
     * treated in terms of precedence.
     */
    @Test
    public void test_ExpressionTypes() {
        final var now = LocalDateTime.now();
        final var account = new Account();
        account.name = "dkb";
        final var payment = new Payment(account, BigDecimal.ONE, "test reference", "fred", now, now);

        final var filter = new PaymentFilter();

        filter.setQuery("""
                name="fred" AND name="fred"
                """);
        Assertions.assertTrue(filter.test(payment));

        filter.setQuery("""
                name="fred" OR name="fred"
                """);
        Assertions.assertTrue(filter.test(payment));

        filter.setQuery("""
                name="abc" OR name="fred"
                """);
        Assertions.assertTrue(filter.test(payment));

        filter.setQuery("""
                name="fred" OR name="abc"
                """);
        Assertions.assertTrue(filter.test(payment));

        filter.setQuery("""
                name="fred" AND name="abc"
                """);
        Assertions.assertFalse(filter.test(payment));

        filter.setQuery("""
                name="fred" AND name="abc"
                """);
        Assertions.assertFalse(filter.test(payment));

        filter.setQuery("""
                name="abc" AND name="deg"
                """);
        Assertions.assertFalse(filter.test(payment));

        filter.setQuery("""
                name="abc" OR name="fred" AND name != "fred"
                """);
        Assertions.assertFalse(filter.test(payment));

        filter.setQuery("""
                name="abc" OR name!="fred" OR name = "fred"
                """);
        Assertions.assertTrue(filter.test(payment));

        filter.setQuery("""
                name="abc" OR name="fred" AND name = "abc"
                """);
        Assertions.assertFalse(filter.test(payment));

        filter.setQuery("""
                name="fred" AND name="kek" OR name = "fred"
                """);
        Assertions.assertTrue(filter.test(payment));

        filter.setQuery("""
                (name="fred" AND name="kek") OR name = "fred"
                """);
        Assertions.assertTrue(filter.test(payment));
    }
}
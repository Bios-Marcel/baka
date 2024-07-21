package link.biosmarcel.baka.view;

import link.biosmarcel.baka.data.Account;
import link.biosmarcel.baka.data.Payment;
import link.biosmarcel.baka.filter.IncompleteQueryException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

class PaymentFilterTest {
    @Test
    public void test_emptyGroup() {
        final var filter = new PaymentFilter();
        Assertions.assertThrows(IncompleteQueryException.class, () -> filter.setQuery("()"));
    }

    @Test
    public void test_BinaryExpression_LowerCase() {
        final var now = LocalDateTime.now();
        final var account = new Account();
        account.name = "dkb";
        final var payment = new Payment(account, BigDecimal.ONE, "test reference", "fred", now, now);

        final var filter = new PaymentFilter();

        filter.setQuery("""
                name="fred" and name="fred"
                """);
        Assertions.assertTrue(filter.test(payment));
    }

    @Test
    public void test_complex() {

        final var now = LocalDateTime.now();
        final var account = new Account();
        account.name = "dkb";
        final var payment = new Payment(account, BigDecimal.ONE, "Nettowelt GmbH", "aifinyo Finance GmbH", now, now);

        final var filter = new PaymentFilter();

        filter.setQuery("""
                name has versicherung or reference has versicherung or name has prokundo or name has finance
                """);
        Assertions.assertTrue(filter.test(payment));
    }

    @Test
    public void test_jimBlock() {

        final var now = LocalDateTime.now();
        final var account = new Account();
        account.name = "dkb";
        final var payment = new Payment(account, BigDecimal.ONE, "Debitcard", "JIM.BLOCK", now, now);

        final var filter = new PaymentFilter();

        filter.setQuery("""
                name has "jim.block" or reference has "takeaway.com"
                """);
        Assertions.assertTrue(filter.test(payment));
    }

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
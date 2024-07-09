package link.biosmarcel.baka.view;

import link.biosmarcel.baka.data.Payment;
import link.biosmarcel.baka.filter.Filter;

public class PaymentFilter extends Filter<Payment> {
    {
        register("name", "=", payment -> payment.name);
        register("name", "!=", payment -> payment.name);
    }
}

package link.biosmarcel.baka.view;

import link.biosmarcel.baka.data.Payment;
import link.biosmarcel.baka.filter.Filter;
import link.biosmarcel.baka.filter.Operator;

public class PaymentFilter extends Filter<Payment> {
    {
        register("name", Operator.EQ, (payment, value) -> payment.name.equalsIgnoreCase((String) value), String::toLowerCase);
        register("name", Operator.NOT_EQ, (payment, value) -> !payment.name.equalsIgnoreCase((String) value), String::toLowerCase);
        register("name", Operator.HAS, (payment, value) -> payment.name.toLowerCase().contains((String) value), String::toLowerCase);

        register("reference", Operator.EQ, (payment, value) -> payment.reference.equalsIgnoreCase((String) value), String::toLowerCase);
        register("reference", Operator.NOT_EQ, (payment, value) -> !payment.reference.equalsIgnoreCase((String) value), String::toLowerCase);
        register("reference", Operator.HAS, (payment, value) -> payment.reference.toLowerCase().contains((String) value), String::toLowerCase);

        register("participant", Operator.EQ, (payment, value) -> payment.participant != null && payment.participant.equalsIgnoreCase((String) value), String::toLowerCase);
        register("participant", Operator.NOT_EQ, (payment, value) -> payment.participant == null || payment.participant.equalsIgnoreCase((String) value), String::toLowerCase);
        register("participant", Operator.HAS, (payment, value) -> payment.participant != null && payment.participant.toLowerCase().contains((String) value), String::toLowerCase);
    }
}

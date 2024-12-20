package link.biosmarcel.baka.data;

import java.math.BigDecimal;

public class Classification {
    /**
     * Single-Tag so we can properly display a pie-chart later on. The tag will be treated case-insensitive.
     */
    public String tag = "";

    /**
     * Amount, since one payment can make up multiple things. For example if you get 100€ at the bank, you
     * could've spent 60€ on food and the other 40€ on weed.
     */
    public BigDecimal amount = BigDecimal.ZERO;

    /**
     * Classifications can be done automatically by specifying {@link ClassificationRule classification rules}.
     * They can also be done manually though.
     */
    public boolean automated = false;
}

package link.biosmarcel.baka.data;

import java.math.BigDecimal;

public class Classification {
    /**
     * Single-Tag so we can properly display a pie-chart later on. The tag will be treated case-insensitive.
     */
    public String tag;

    /**
     * Amount, since one payment can make up multiple things. For example if you get 100€ at the bank, you
     * could've spent 60€ on food and the other 40€ on weed.
     */
    public BigDecimal amount;

    public String getTag() {
        return tag;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}

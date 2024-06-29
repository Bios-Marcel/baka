package link.biosmarcel.baka;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Payment {
    public BigDecimal amount;
    public String reference;
    public String name;
    /**
     * IBAN of the sender / recipient.
     */
    public String account;
    public @Nullable String account;

    public LocalDateTime bookingDate;
    public LocalDateTime effectiveDate;

    public List<Classification> classifications = new ArrayList<>();
}

package link.biosmarcel.baka;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Payment {
    public BigDecimal amount;
    public String reference;
    public String name;

    public LocalDateTime bookingDate;
    public LocalDateTime effectiveDate;

    public List<Classification> classifications = new ArrayList<>();
}

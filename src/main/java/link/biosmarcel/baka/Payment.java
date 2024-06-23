package link.biosmarcel.baka;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Payment {
    public BigDecimal amount;
    public String reference;

    public LocalDate bookingDate;
    public LocalDate effectiveDate;

    public List<Classification> classifications = new ArrayList<>();
}

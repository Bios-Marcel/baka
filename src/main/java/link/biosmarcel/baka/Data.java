package link.biosmarcel.baka;

import java.util.ArrayList;
import java.util.List;

/**
 * Root-Object for our storage. Getters and setters are not required here. Collection values are automatically
 * initialised once data is found. By default, such fields will be {@code null}.
 */
public class Data {
    /**
     * A list of ALL incoming payments. Sorted by date descending.
     */
    public List<Payment> payments = new ArrayList<>();
}

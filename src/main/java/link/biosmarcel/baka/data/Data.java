package link.biosmarcel.baka.data;

import java.util.*;

/**
 * Root-Object for our storage. Getters and setters are not required here.
 * Collection values are automatically  initialised once data is found.
 * By default, such fields will be {@code null}.
 */
public class Data {
    public int version;

    public ConvenienceState convenienceState = new ConvenienceState();

    /**
     * A list of ALL payments. Sorted by {@link Payment#effectiveDate} descending.
     */
    public List<Payment> payments = new ArrayList<>();

    public List<Account> accounts = new ArrayList<>();

    public List<ClassificationRule> classificationRules = new ArrayList<>();

    /**
     * Init makes sure all fields have their initial value if they aren't set yet.
     * While this will happen correctly on first creation of the database, adding fields will require this.
     */
    @SuppressWarnings("ConstantValue")
    public void init() {
        if (convenienceState == null) {
            convenienceState = new ConvenienceState();
        }
        convenienceState.init();
        if (payments == null) {
            payments = new ArrayList<>();
        }
        if (accounts == null) {
            accounts = new ArrayList<>();
        }
        if (classificationRules == null) {
            classificationRules = new ArrayList<>();
        }
    }
}

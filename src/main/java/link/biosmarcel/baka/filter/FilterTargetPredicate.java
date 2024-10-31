package link.biosmarcel.baka.filter;

/**
 * Function for testing a concrete value of the filter target.
 *
 * @param <FilterTarget> Type of the class that contains the field
 * @param <ValueType> Type of the field
 */
@FunctionalInterface
public interface FilterTargetPredicate<FilterTarget, ValueType> {
    boolean test(FilterTarget target, ValueType value);
}

package link.biosmarcel.baka.filter;

@FunctionalInterface
public interface FilterTargetPredicate<FilterTarget, ValueType> {
    boolean test(FilterTarget target, ValueType value);
}

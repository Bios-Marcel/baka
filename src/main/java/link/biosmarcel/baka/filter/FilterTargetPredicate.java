package link.biosmarcel.baka.filter;

@FunctionalInterface
public interface FilterTargetPredicate<FilterTarget> {
    boolean test(FilterTarget target, Object value);
}

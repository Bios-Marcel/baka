package link.biosmarcel.baka.filter;

import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

/**
 * Info:  a & b | c equals a & (b | c)
 *
 * @param <FilterTarget> Type of the object we intend to test against
 */
class Expression<FilterTarget> implements Predicate<FilterTarget> {
    public Predicate<FilterTarget> predicate;
    public BinaryExpressionType binaryExpressionType;
    public @Nullable Predicate<FilterTarget> next;

    public boolean test(final FilterTarget filterTarget) {
        final var passed = predicate.test(filterTarget);
        if (next == null) {
            return passed;
        }

        return switch (binaryExpressionType) {
            case AND -> passed && next.test(filterTarget);
            case OR -> passed || next.test(filterTarget);
        };
    }
}
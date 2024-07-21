package link.biosmarcel.baka.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FilterAutocompleteGenerator {
    private final Filter<?> filter;

    public FilterAutocompleteGenerator(final Filter<?> filter) {
        this.filter = filter;
    }

    public List<String> generate(final String value) {
        try {
            filter.setQuery(value);
            if (value.endsWith(")") || value.endsWith(" ") || value.endsWith("\n")) {
                // If the query is valid so far, we can always attach another via binary operators.
                return Arrays.stream(BinaryExpressionType.values()).map(bo -> bo.text).toList();
            }
            return Collections.EMPTY_LIST;
        } catch (final IncompleteQueryException exception) {
            if ((value.endsWith("(") && exception.token.isBlank()) ||
                    (value.endsWith(exception.token) &&
                            (!exception.token.isEmpty() || value.isEmpty() || !value.stripTrailing().equalsIgnoreCase(value)))
            ) {
                return exception.options;
            }
            return Collections.EMPTY_LIST;
        } catch (final RuntimeException exception) {
            System.out.println(exception.getMessage());
            return Collections.EMPTY_LIST;
        }
    }
}

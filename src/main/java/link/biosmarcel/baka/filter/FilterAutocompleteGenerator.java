package link.biosmarcel.baka.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Generates autocompletion items for a given filter.
 */
public class FilterAutocompleteGenerator {
    private final Filter<?> filter;

    /**
     * @param filter {@link Filter} for which to generate autocompletion values
     */
    public FilterAutocompleteGenerator(final Filter<?> filter) {
        this.filter = filter;
    }

    /**
     * Generate autocompletion options for input.
     *
     * @param input Input to generate autocompletion options for
     *
     * @return List of options or an empty list if no options are available
     */
    public List<String> generate(final String input) {
        try {
            filter.setQuery(input);
            if (input.endsWith(")") || input.endsWith(" ") || input.endsWith("\n")) {
                // If the query is valid so far, we can always attach another via binary operators.
                return Arrays.stream(BinaryExpressionType.values()).map(bo -> bo.text).toList();
            }
            return Collections.EMPTY_LIST;
        } catch (final IncompleteQueryException exception) {
            if ((input.endsWith("(") && exception.token.isBlank()) ||
                    (input.endsWith(exception.token) &&
                            (!exception.token.isEmpty() || input.isEmpty() || !input.stripTrailing().equalsIgnoreCase(input)))
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

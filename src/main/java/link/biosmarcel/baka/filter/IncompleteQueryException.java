package link.biosmarcel.baka.filter;

import java.util.List;

/**
 * Indicates that a query is not necessarily incorrect, but incomplete. It lists the available inputs where possible.
 */
public class IncompleteQueryException extends RuntimeException {
    /**
     * Indicates whether the whole query was empty.
     */
    public final boolean empty;
    /**
     * Partial token that is not yet complete or empty string, if no new token was started.
     */
    public final String token;
    /**
     * Available input operators if any fixed set is available.
     */
    public final List<String> options;

    /**
     * @param empty   {@link #empty}
     * @param token   {@link #token}
     * @param options {@link #options}
     */
    IncompleteQueryException(final boolean empty,
                             final String token,
                             final List<String> options) {
        this.empty = empty;
        this.token = token;
        this.options = options;
    }
}

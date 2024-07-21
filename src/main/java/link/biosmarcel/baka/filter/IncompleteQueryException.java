package link.biosmarcel.baka.filter;

import java.util.List;

public class IncompleteQueryException extends RuntimeException {
    public final boolean empty;
    public final String token;
    public final List<String> options;

    public IncompleteQueryException(final boolean empty,
                                    final String token,
                                    final List<String> options) {
        this.empty = empty;
        this.token = token;
        this.options = options;
    }
}

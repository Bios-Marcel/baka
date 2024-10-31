package link.biosmarcel.baka.data;

import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains all state, that is irrelevant for business logic. These can be things such as file paths, window sizes or
 * other things that aren't necessary for producing / storing the payment related data.
 */
public class ConvenienceState {
    /**
     * Holds the last chosen import directories on a per-account basis. This data is for convenience only.
     * We save a {@link String} instead of {@link Path} for portability reasons, as {@link Path}-Implementations are
     * OS-dependent.
     */
    public Map<Account, String> importDirectories = new HashMap<>();

    /**
     * Similar to {@link #importDirectories}, but only holds the last state for whatever account, so we can use it
     * as a fallback for new accounts / ones that haven't had any data imported.
     */
    public @Nullable String lastImportPath;

    /**
     * Init makes sure all fields have their initial value if they aren't set yet. While this will happen correctly
     * on first creation of the database, adding fields will require this.
     */
    @SuppressWarnings("ConstantValue")
    public void init() {
        if (importDirectories == null) {
            importDirectories = new HashMap<>();
        }
    }
}

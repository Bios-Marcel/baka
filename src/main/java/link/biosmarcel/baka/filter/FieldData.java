package link.biosmarcel.baka.filter;

import java.util.function.Function;

public class FieldData<FilterTarget> {
    final FilterTargetPredicate<FilterTarget> operate;
    final Function<String, Object> convertString;

    public FieldData(final FilterTargetPredicate<FilterTarget> operate, final Function<String, Object> convertString) {
        this.operate = operate;
        this.convertString = convertString;
    }
}
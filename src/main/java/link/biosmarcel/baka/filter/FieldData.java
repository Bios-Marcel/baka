package link.biosmarcel.baka.filter;

import java.util.function.Function;

public class FieldData<FilterTarget, ValueType> {
    final FilterTargetPredicate<FilterTarget, ValueType> operate;
    final Function<String, ValueType> convertString;

    public FieldData(final FilterTargetPredicate<FilterTarget, ValueType> operate, final Function<String, ValueType> convertString) {
        this.operate = operate;
        this.convertString = convertString;
    }
}
package link.biosmarcel.baka.filter;

import java.util.function.Function;

/**
 * Simple Data-Class to be used as Map-Value for fields that can be operated on.
 */
class FieldData<FilterTarget, ValueType> {
    final FilterTargetPredicate<FilterTarget, ValueType> operate;
    final Function<String, ValueType> convertString;

    FieldData(final FilterTargetPredicate<FilterTarget, ValueType> operate, final Function<String, ValueType> convertString) {
        this.operate = operate;
        this.convertString = convertString;
    }
}
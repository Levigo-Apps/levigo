package org.getcarebase.carebase.utils;

import org.getcarebase.carebase.R;

public class GreaterThanZeroValidationRule<D> extends ValidationRule<D,Integer>{
    public GreaterThanZeroValidationRule(String fieldName,FieldGetter<Integer> fieldGetter) {
        super(fieldName,fieldGetter,R.string.error_must_be_greater_than_zero);
    }

    @Override
    public boolean validate(D data) {
        return getFieldGetter().getField() > 0;
    }
}

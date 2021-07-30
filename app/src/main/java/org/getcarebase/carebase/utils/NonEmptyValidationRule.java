package org.getcarebase.carebase.utils;

import org.getcarebase.carebase.R;

import java.util.Objects;

public class NonEmptyValidationRule<D> extends ValidationRule<D,String> {
    public NonEmptyValidationRule(String fieldName,FieldGetter<String> getter) {
        super(fieldName,getter,R.string.error_missing_required_fields);
    }

    @Override
    public boolean validate(D data) {
       return getFieldGetter().getField() != null && !getFieldGetter().getField().isEmpty();

    }
}

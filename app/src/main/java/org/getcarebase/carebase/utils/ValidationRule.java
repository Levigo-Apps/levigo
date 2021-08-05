package org.getcarebase.carebase.utils;

public abstract class ValidationRule<D,F> {
    public interface FieldGetter<F> {
        F getField();
    }
    private final String fieldName;
    private final FieldGetter<F> fieldGetter;
    private final int referenceString;

    public ValidationRule(String fieldName, FieldGetter<F> getter,int referenceString) {
        this.fieldName = fieldName;
        this.fieldGetter = getter;
        this.referenceString = referenceString;
    }

    public String getFieldName() {
        return fieldName;
    }

    public FieldGetter<F> getFieldGetter() {
        return fieldGetter;
    }

    public int getReferenceString() {
        return referenceString;
    }

    public abstract boolean validate(D data);
}

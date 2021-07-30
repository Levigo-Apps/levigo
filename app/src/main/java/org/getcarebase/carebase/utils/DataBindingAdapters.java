package org.getcarebase.carebase.utils;

import androidx.databinding.BindingAdapter;

import com.google.android.material.textfield.TextInputLayout;

public class DataBindingAdapters {
    @BindingAdapter("errorText")
    public static void setErrorMessage(TextInputLayout view, int errorResource) {
        if (errorResource != 0) {
            view.setError(view.getResources().getString(errorResource));
            view.invalidate();
        }

    }
}

package org.getcarebase.carebase.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.common.escape.CharEscaper;

import org.getcarebase.carebase.R;

public class DetailLabeledTextView extends LinearLayout {
    private final TextView valueTextView;
    private final TextView labelTextView;

    public DetailLabeledTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View rootView = inflate(context,R.layout.detail_text_view_layout,this);
        valueTextView = rootView.findViewById(R.id.value);
        labelTextView = rootView.findViewById(R.id.label);
        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DetailLabeledTextView,0,0);
            CharSequence value = a.getText(R.styleable.DetailLabeledTextView_value_text);
            valueTextView.setText(value);
            CharSequence label = a.getText(R.styleable.DetailLabeledTextView_label_text);
            labelTextView.setText(label);
        }
    }

    public CharSequence getTextValue() {
        return valueTextView.getText();
    }

    public void setTextValue(CharSequence value) {
        valueTextView.setText(value);
        invalidate();
        requestLayout();
    }

    public void setLabel(CharSequence label) {
        labelTextView.setText(label);
        invalidate();
        requestLayout();
    }
}

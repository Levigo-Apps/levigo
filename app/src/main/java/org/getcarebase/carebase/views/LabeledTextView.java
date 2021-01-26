package org.getcarebase.carebase.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import org.getcarebase.carebase.R;

public class LabeledTextView extends AppCompatTextView {

    private final CharSequence label;
    private CharSequence value;
    private boolean showLabel;

    public LabeledTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LabeledTextView,0,0);

        try {
            label = a.getText(R.styleable.LabeledTextView_label_text);
            value = a.getText(R.styleable.LabeledTextView_text_value);
            showLabel = a.getBoolean(R.styleable.LabeledTextView_show_label,true);
        } finally {
            a.recycle();
        }
    }

    private void setLabeledText() {
        CharSequence labeledText;
        if (showLabel) {
            labeledText = label + ": " + value;
        } else {
            labeledText = value;
        }
        super.setText(labeledText);
    }

    public CharSequence getValue() {
        return value;
    }

    public void setValue(CharSequence value) {
        this.value = value;
        setLabeledText();
        invalidate();
        requestLayout();
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
        setLabeledText();
        invalidate();
        requestLayout();
    }
}

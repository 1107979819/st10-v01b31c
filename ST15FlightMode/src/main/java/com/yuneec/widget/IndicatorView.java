package com.yuneec.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.yuneec.flightmode15.R;

public class IndicatorView extends LinearLayout {
    private String mLabel;
    private TextView mLabelView;
    private String mValue;
    private TextView mValueView;

    public IndicatorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.indicator_layout, this);
        setGravity(17);
        setOrientation(1);
        setPadding(10, 2, 10, 10);
        setBackgroundResource(R.drawable.indicator_bg);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Indicator, defStyle, 0);
        this.mLabel = a.getString(0);
        this.mValue = a.getString(1);
        a.recycle();
    }

    public IndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorView(Context context) {
        this(context, null);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLabelView = (TextView) findViewById(R.id.label);
        this.mValueView = (TextView) findViewById(R.id.value);
        setLabelText(this.mLabel);
        setValueText(this.mValue);
    }

    public void setLabelText(CharSequence text) {
        if (this.mLabelView != null) {
            this.mLabelView.setText(text);
        }
    }

    public CharSequence getValueText() {
        return this.mValueView.getText();
    }

    public void setValueText(CharSequence text) {
        if (this.mValueView != null) {
            this.mValueView.setText(text);
        }
    }

    public void setValueTextSize(float size) {
        if (this.mValueView != null) {
            this.mValueView.setTextSize(size);
        }
    }

    public void setValueColor(int color) {
        this.mValueView.setTextColor(color);
    }

    public void setValueTextDrawable(Drawable drawable) {
        this.mValueView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
        this.mValueView.setCompoundDrawablePadding(-4);
        this.mValueView.setPadding(0, 4, 0, 0);
    }

    public void setValueTextDrawableLevel(int level) {
        Drawable dr = this.mValueView.getCompoundDrawables()[1];
        if (dr != null) {
            dr.setLevel(level);
        }
    }
}

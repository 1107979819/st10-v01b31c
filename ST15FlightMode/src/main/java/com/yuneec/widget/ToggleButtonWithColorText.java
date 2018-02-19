package com.yuneec.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

public class ToggleButtonWithColorText extends ToggleButton {
    public ToggleButtonWithColorText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ToggleButtonWithColorText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToggleButtonWithColorText(Context context) {
        super(context);
    }

    public void setChecked(boolean checked) {
        super.setChecked(checked);
        playSoundEffect(0);
    }
}

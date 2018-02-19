package com.yuneec.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class SyncToggleButton extends ToggleButton {
    private boolean mByUser = true;
    private OnCheckedChangeListener mListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SyncToggleButton.this.mUpdateChangeListener.onCheckedChanged(buttonView, isChecked, SyncToggleButton.this.mByUser);
            SyncToggleButton.this.mByUser = true;
        }
    };
    private OnUpdateChangeListener mUpdateChangeListener;

    public interface OnUpdateChangeListener {
        void onCheckedChanged(CompoundButton compoundButton, boolean z, boolean z2);
    }

    public SyncToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SyncToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SyncToggleButton(Context context) {
        super(context);
    }

    public void updateToogleButton(boolean checked) {
        super.setChecked(checked);
    }

    public void syncState(boolean checked) {
        this.mByUser = false;
        super.setChecked(checked);
    }

    public void setChecked(boolean checked) {
        this.mByUser = true;
        super.setChecked(checked);
    }

    public boolean isCheckedByUser() {
        return this.mByUser;
    }

    public void setOnUpdateChangeListener(OnUpdateChangeListener updateChangeListener) {
        super.setOnCheckedChangeListener(this.mListener);
        this.mUpdateChangeListener = updateChangeListener;
    }
}

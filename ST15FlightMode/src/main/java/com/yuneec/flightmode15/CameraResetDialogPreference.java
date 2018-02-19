package com.yuneec.flightmode15;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.AttributeSet;
import android.view.View;

public class CameraResetDialogPreference extends DialogPreference {
    private OnPreferenceChangeListener listener;

    public CameraResetDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CameraResetDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
    }

    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        this.listener = onPreferenceChangeListener;
    }

    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        this.listener.onPreferenceChange(this, Boolean.valueOf(which == -1));
    }
}

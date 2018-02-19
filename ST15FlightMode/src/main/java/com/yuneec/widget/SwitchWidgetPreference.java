package com.yuneec.widget;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.yuneec.flightmode15.R;

public class SwitchWidgetPreference extends Preference {
    private boolean isOnStates = false;
    private LinearLayout offStatesLayout;
    private LinearLayout onStatesLayout;

    public SwitchWidgetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        this.onStatesLayout = (LinearLayout) view.findViewById(R.id.widget_switch_on);
        this.offStatesLayout = (LinearLayout) view.findViewById(R.id.widget_switch_off);
        setWidgetStates(this.isOnStates);
    }

    public void setWidgetStates(boolean isOn) {
        this.isOnStates = isOn;
        if (this.onStatesLayout != null && this.offStatesLayout != null) {
            if (isOn) {
                this.onStatesLayout.setVisibility(0);
                this.offStatesLayout.setVisibility(8);
                return;
            }
            this.onStatesLayout.setVisibility(8);
            this.offStatesLayout.setVisibility(0);
        }
    }

    public boolean getIsOnStates() {
        return this.isOnStates;
    }
}

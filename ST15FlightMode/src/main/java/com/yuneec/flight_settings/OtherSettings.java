package com.yuneec.flight_settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import com.yuneec.flightmode15.R;

public class OtherSettings extends Activity {
    private int isMetricOrImperial;
    private SharedPreferences mPrefs;
    private Switch unit_switch;

    private class GenderOnCheckedChangeListener implements OnCheckedChangeListener {
        private GenderOnCheckedChangeListener() {
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!buttonView.equals(OtherSettings.this.unit_switch)) {
                return;
            }
            if (isChecked) {
                OtherSettings.this.mPrefs.edit().putInt(FlightSettings.VELOCITY_UNIT, 2).commit();
            } else {
                OtherSettings.this.mPrefs.edit().putInt(FlightSettings.VELOCITY_UNIT, 1).commit();
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        setContentView(R.layout.other_settings_main);
        this.mPrefs = getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0);
        this.isMetricOrImperial = this.mPrefs.getInt(FlightSettings.VELOCITY_UNIT, 2);
        this.unit_switch = (Switch) findViewById(R.id.unit_switch);
        this.unit_switch.setOnCheckedChangeListener(new GenderOnCheckedChangeListener());
        if (this.isMetricOrImperial == 2) {
            this.unit_switch.setChecked(true);
        } else {
            this.unit_switch.setChecked(false);
        }
    }
}

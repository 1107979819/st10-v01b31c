package com.spreadtrum.android.eng;

import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class EngModeAllActivity extends PreferenceActivity implements OnPreferenceClickListener {
    private CheckBoxPreference mQosSwitch;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.engmodeallactivity);
        this.mQosSwitch = (CheckBoxPreference) findPreference("Qos_switch");
        if (this.mQosSwitch != null) {
            this.mQosSwitch.setOnPreferenceClickListener(this);
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        Log.w("EngModeAllActivity", "onPreferenceClick(), " + key);
        if (!"Qos_switch".equals(key)) {
            return false;
        }
        if (this.mQosSwitch.isChecked()) {
            SystemProperties.set("persist.sys.qosstate", "1");
        } else {
            SystemProperties.set("persist.sys.qosstate", "0");
        }
        return true;
    }
}

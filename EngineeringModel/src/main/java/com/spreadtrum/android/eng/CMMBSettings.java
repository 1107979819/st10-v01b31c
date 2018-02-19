package com.spreadtrum.android.eng;

import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

public class CMMBSettings extends PreferenceActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.cmmb_setting);
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if ("test_mode".equals(key)) {
            SystemProperties.set("ro.hisense.cmcc.test", ((CheckBoxPreference) preference).isChecked() ? "1" : "0");
        } else if (!"wire_test_mode".equals(key)) {
            return false;
        } else {
            SystemProperties.set("ro.hisense.cmcc.test.cmmb.wire", ((CheckBoxPreference) preference).isChecked() ? "1" : "0");
        }
        return true;
    }
}

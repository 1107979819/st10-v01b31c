package com.spreadtrum.android.eng;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.telephony.TelephonyManager;

public class EngMobileSimChoose extends PreferenceActivity {
    public static String CLASS_NAME = "class_name";
    public static String CLASS_NAME_OTHER = "class_name_other";
    private static String KEY1 = "mobile_sim1_settings";
    private static String KEY2 = "mobile_sim2_settings";
    private static String KEYOther = "mobile_other_settings";
    public static String PACKAGE_NAME = "package_name";
    public static String SUB_ID = "sub_id";
    private Preference mOtherPref;
    private Preference mSim1Pref;
    private Preference mSim2Pref;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getStringExtra(CLASS_NAME_OTHER) == null) {
            addPreferencesFromResource(R.layout.mobile_sim_choose);
            PreferenceScreen prefSet = getPreferenceScreen();
            this.mSim1Pref = prefSet.findPreference(KEY1);
            this.mSim2Pref = prefSet.findPreference(KEY2);
            return;
        }
        addPreferencesFromResource(R.layout.mobile_sim_choose_other);
        prefSet = getPreferenceScreen();
        this.mSim1Pref = prefSet.findPreference(KEY1);
        this.mSim2Pref = prefSet.findPreference(KEY2);
        this.mOtherPref = prefSet.findPreference(KEYOther);
    }

    protected void onResume() {
        super.onResume();
        if (TelephonyManager.getDefault(0).getSimState() == 5) {
            this.mSim1Pref.setEnabled(true);
        } else {
            this.mSim1Pref.setEnabled(false);
        }
        if (TelephonyManager.getDefault(1).getSimState() == 5) {
            this.mSim2Pref.setEnabled(true);
        } else {
            this.mSim2Pref.setEnabled(false);
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setComponent(new ComponentName(getIntent().getStringExtra(PACKAGE_NAME), getIntent().getStringExtra(CLASS_NAME)));
        if (preference == this.mSim1Pref) {
            intent.putExtra(SUB_ID, 0);
        } else if (preference == this.mSim2Pref) {
            intent.putExtra(SUB_ID, 1);
        } else if (preference == this.mOtherPref) {
        }
        startActivity(intent);
        return true;
    }
}

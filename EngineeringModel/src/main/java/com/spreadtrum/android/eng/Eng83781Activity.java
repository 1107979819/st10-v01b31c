package com.spreadtrum.android.eng;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.telephony.TelephonyManager;
import android.util.Log;

public class Eng83781Activity extends PreferenceActivity {
    private Preference mSerialPref;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.prefs_83781);
        this.mSerialPref = getPreferenceScreen().findPreference("key_gprs");
        this.mSerialPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent("android.intent.action.MAIN");
                if (TelephonyManager.isMultiSim()) {
                    Log.e("TAG", "TelephonyManager is MultiSim========================");
                    intent.setComponent(new ComponentName("com.spreadtrum.android.eng", "com.spreadtrum.android.eng.EngMobileSimChoose"));
                    intent.putExtra(EngMobileSimChoose.PACKAGE_NAME, "com.android.phone");
                    intent.putExtra(EngMobileSimChoose.CLASS_NAME, "com.android.phone.Settings");
                } else {
                    Log.e("TAG", "TelephonyManager is  single Sim========================");
                    intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.Settings"));
                }
                try {
                    Eng83781Activity.this.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e("TAG", "Not found Activity !");
                }
                return true;
            }
        });
    }
}

package com.spreadtrum.android.eng;

import android.content.Context;
import android.net.LocalSocket;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

public class AOTASetting extends PreferenceActivity {
    private Preference mClear;
    private Context mContext = this;
    private CheckBoxPreference mEnable;
    private CheckBoxPreference mPreload;
    private CheckBoxPreference mUser;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.aota_setting);
        this.mEnable = (CheckBoxPreference) findPreference("aota_support");
        this.mPreload = (CheckBoxPreference) findPreference("authorized_preload");
        this.mClear = findPreference("aota_clear");
        this.mUser = (CheckBoxPreference) findPreference("aota_user_enable");
    }

    private void runCommand(final int command, boolean isEnable) {
        final String option = isEnable ? " enable " : " disable ";
        Log.e("duke", "isEnable = " + option + "command = " + command);
        new Thread() {
            public void run() {
                try {
                    if (command == 0) {
                        Log.e("duke", "pm enable/disable");
                        Runtime.getRuntime().exec("pm" + option + "com.android.synchronism");
                    }
                    if (command == 1) {
                        Log.e("duke", "rm command");
                        Runtime.getRuntime().exec("rm /data/preloadapp/Synchronism.apk").waitFor();
                        Runtime.getRuntime().exec("rm /data/dalvik-cache/data@preloadapp@Synchronism.apk@classes.dex");
                    }
                } catch (Exception e) {
                    Log.e("duke", "run command crashed " + e);
                }
            }
        }.start();
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        LocalSocket clientSocket = new LocalSocket();
        if (preference == this.mUser) {
            SystemProperties.set("persist.sys.synchronism.enable", this.mUser.isChecked() ? "1" : "0");
            Log.e("duke", "isAOTAEnable = " + String.valueOf(SystemProperties.getBoolean("persist.sys.synchronism.enable", false)));
            runCommand(0, this.mUser.isChecked());
            return true;
        } else if (preference == this.mClear) {
            Log.e("duke", "mClear == preference");
            runCommand(1, false);
            return true;
        } else if (preference == this.mEnable) {
            Log.e("duke", "mEnable == preference");
            SystemProperties.set("persist.sys.synchronism.support", this.mEnable.isChecked() ? "1" : "0");
            return true;
        } else if (preference != this.mPreload) {
            return false;
        } else {
            Log.e("duke", "mPreload");
            SystemProperties.set("persist.sys.authorized.preload", this.mPreload.isChecked() ? "1" : "0");
            return true;
        }
    }
}

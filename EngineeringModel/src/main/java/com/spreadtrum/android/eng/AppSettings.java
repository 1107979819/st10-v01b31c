package com.spreadtrum.android.eng;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Debug;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;

public class AppSettings extends PreferenceActivity {
    private static final boolean isDebug = Debug.isDebug();
    private CheckBoxPreference mAcceRotation;
    private CheckBoxPreference mAutoAnswer;
    private CheckBoxPreference mBtSwitch;
    private engfetch mEf;
    private CheckBoxPreference mEnableUsbFactoryMode;
    private CheckBoxPreference mEnableVserGser;
    private EngSqlite mEngSqlite;
    private CheckBoxPreference mModemDebugPM;
    private CheckBoxPreference mModemReset;
    private Preference mNetworkSelect;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.appset);
        this.mAutoAnswer = (CheckBoxPreference) findPreference("autoanswer_call");
        this.mEnableVserGser = (CheckBoxPreference) findPreference("enable_vser_gser");
        this.mAcceRotation = (CheckBoxPreference) findPreference("accelerometer_rotation");
        this.mModemDebugPM = (CheckBoxPreference) findPreference("ModemDebugPM");
        this.mBtSwitch = (CheckBoxPreference) findPreference("BTswitch");
        this.mEnableUsbFactoryMode = (CheckBoxPreference) findPreference("enable_usb_factory_mode");
        this.mModemReset = (CheckBoxPreference) findPreference("modem_reset");
        String result = SystemProperties.get("persist.sys.sprd.modemreset");
        Log.e("engineeringmodel", "result: " + result + ", result.equals(): " + result.equals("1"));
        this.mModemReset.setChecked(result.equals("1"));
        this.mEngSqlite = EngSqlite.getInstance(this);
        this.mNetworkSelect = findPreference("networkselect");
        if (SystemProperties.get("ro.product.board", "sp8810ga").substring(0, 4).equals("sp68")) {
            getPreferenceScreen().removePreference(this.mNetworkSelect);
        }
        if (!isDebug) {
            getPreferenceScreen().removePreference(this.mModemDebugPM);
        }
        this.mEf = new engfetch();
    }

    protected void onResume() {
        boolean check;
        if (System.getInt(getContentResolver(), "accelerometer_rotation", 1) == 1) {
            check = true;
        } else {
            check = false;
        }
        if (check) {
            SystemProperties.set("persist.sys.acce_enable", check ? "1" : "0");
        }
        this.mAcceRotation.setChecked(check);
        String usbMode = SystemProperties.get("sys.usb.config", "");
        Log.e("engineeringmodel", " usbMode = " + usbMode);
        this.mEnableVserGser.setChecked(usbMode.endsWith("vser,gser"));
        if (this.mEngSqlite.queryData("engtestmode")) {
            this.mEnableUsbFactoryMode.setChecked(this.mEngSqlite.queryFactoryModeDate("engtestmode") == 1);
        } else {
            this.mEnableUsbFactoryMode.setChecked(true);
        }
        if (isDebug) {
            boolean isdebugdmcheck;
            if (this.mEf.enggetdebugnowakelock(0) == 1) {
                isdebugdmcheck = true;
            } else {
                isdebugdmcheck = false;
            }
            this.mModemDebugPM.setChecked(isdebugdmcheck);
        }
        super.onStart();
    }

    protected void onDestroy() {
        this.mEngSqlite.release();
        super.onDestroy();
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        int i = 0;
        boolean isChecked;
        if (preference == this.mAutoAnswer) {
            isChecked = this.mAutoAnswer.isChecked();
            Editor edit = getSharedPreferences("ENGINEERINGMODEL", 0).edit();
            edit.putBoolean("autoanswer_call", isChecked);
            edit.commit();
            if (isChecked) {
                getApplicationContext().startService(new Intent(getApplicationContext(), AutoAnswerService.class));
            } else {
                getApplicationContext().stopService(new Intent(getApplicationContext(), AutoAnswerService.class));
            }
            Log.e("engineeringmodel", "auto answer state " + isChecked);
            return true;
        } else if (preference == this.mEnableVserGser) {
            isChecked = this.mEnableVserGser.isChecked();
            ContentResolver contentResolver = getContentResolver();
            r4 = "vser_gser_enabled";
            if (isChecked) {
                i = 1;
            }
            Secure.putInt(contentResolver, r4, i);
            return true;
        } else if (preference == this.mAcceRotation) {
            boolean isChecked2 = this.mAcceRotation.isChecked();
            SystemProperties.set("persist.sys.acce_enable", isChecked2 ? "1" : "0");
            ContentResolver contentResolver2 = getContentResolver();
            r4 = "accelerometer_rotation";
            if (isChecked2) {
                i = 1;
            }
            System.putInt(contentResolver2, r4, i);
            return true;
        } else if (preference == this.mEnableUsbFactoryMode) {
            isChecked = this.mEnableUsbFactoryMode.isChecked();
            EngSqlite engSqlite = this.mEngSqlite;
            r4 = "engtestmode";
            if (isChecked) {
                i = 1;
            }
            engSqlite.updataFactoryModeDB(r4, i);
            return true;
        } else if (preference == this.mModemDebugPM) {
            if (this.mModemDebugPM.isChecked()) {
                this.mEf.enablemodemdebugpm(1);
                return true;
            }
            this.mEf.disablemodemdebugpm(0);
            return true;
        } else if (preference != this.mBtSwitch) {
            String key = preference.getKey();
            if ("call_forward_query".equals(key)) {
                if (!(preference instanceof CheckBoxPreference)) {
                    return true;
                }
                SystemProperties.set("persist.sys.callforwarding", ((CheckBoxPreference) preference).isChecked() ? "1" : "0");
                return true;
            } else if ("emergency_call_retry".equals(key)) {
                if (!(preference instanceof CheckBoxPreference)) {
                    return true;
                }
                SystemProperties.set("persist.sys.emergencyCallRetry", ((CheckBoxPreference) preference).isChecked() ? "1" : "0");
                return true;
            } else if ("card_log".equals(key)) {
                if (!(preference instanceof CheckBoxPreference)) {
                    return true;
                }
                SystemProperties.set("persist.sys.cardlog", ((CheckBoxPreference) preference).isChecked() ? "1" : "0");
                return true;
            } else if (!"modem_reset".equals(key)) {
                return false;
            } else {
                if (!(preference instanceof CheckBoxPreference)) {
                    return true;
                }
                SystemProperties.set("persist.sys.sprd.modemreset", ((CheckBoxPreference) preference).isChecked() ? "1" : "0");
                return true;
            }
        } else if (this.mBtSwitch.isChecked()) {
            System.putString(getContentResolver(), "Bt_Switch", "true");
            return true;
        } else {
            System.putString(getContentResolver(), "Bt_Switch", "false");
            return true;
        }
    }
}

package com.spreadtrum.android.eng;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

public class WifiCMCCTest extends PreferenceActivity {
    public boolean isAnritsu;
    private boolean isRunning = false;
    private CheckBoxPreference mAnritsuCheckbox;
    private engfetch mEf;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            CheckBoxPreference checkbox;
            int what = msg.what;
            if (WifiCMCCTest.this.isAnritsu) {
                checkbox = WifiCMCCTest.this.mAnritsuCheckbox;
            } else {
                checkbox = WifiCMCCTest.this.mNonAnritsuCheckbox;
            }
            switch (what) {
                case 4:
                    checkbox.setSummary("wifi open failed");
                    return;
                case 5:
                    checkbox.setSummary("wifi close failed");
                    return;
                case 6:
                    checkbox.setSummary("");
                    return;
                case 7:
                    checkbox.setSummary("");
                    return;
                case 8:
                    WifiCMCCTest.this.mRunTestCheckbox.setEnabled(true);
                    return;
                case 9:
                    WifiCMCCTest.this.mRunTestCheckbox.setChecked(false);
                    WifiCMCCTest.this.mRunTestCheckbox.setEnabled(false);
                    return;
                default:
                    return;
            }
        }
    };
    private CheckBoxPreference mNonAnritsuCheckbox;
    private CheckBoxPreference mRunTestCheckbox;
    private int outWifiState;
    private int waitTimes = 10;
    private WifiManager wifiManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.prefs_wifi_cmcc_test);
        this.mAnritsuCheckbox = (CheckBoxPreference) findPreference("anritsu_test");
        this.mNonAnritsuCheckbox = (CheckBoxPreference) findPreference("non_anritsu_test");
        this.mRunTestCheckbox = (CheckBoxPreference) findPreference("run_test");
        this.mAnritsuCheckbox.setSummary("");
        this.mNonAnritsuCheckbox.setSummary("");
        this.mRunTestCheckbox.setSummary("iwconfig when power off");
        this.mAnritsuCheckbox.setEnabled(false);
        this.mNonAnritsuCheckbox.setEnabled(false);
        this.mRunTestCheckbox.setEnabled(true);
        this.wifiManager = (WifiManager) getSystemService("wifi");
        this.outWifiState = this.wifiManager.getWifiState();
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if ("run_test".equals(preference.getKey()) && (preference instanceof CheckBoxPreference)) {
            CheckBoxPreference checkbox = (CheckBoxPreference) preference;
            if (checkbox.isChecked()) {
                cmccTest();
                checkbox.setEnabled(false);
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    protected void onStop() {
        if (this.isRunning) {
            cmccStop();
        }
        Log.d("WifiCMCCTest", "outWifiState=" + this.outWifiState);
        if (this.outWifiState == 3 || this.outWifiState == 2) {
            this.wifiManager.setWifiEnabled(true);
        } else {
            this.wifiManager.setWifiEnabled(false);
        }
        super.onStop();
    }

    private void cmccStop() {
        writeCmd("CMCC STOP");
        this.mHandler.sendEmptyMessage(7);
        Log.d("WifiCMCCTest", "xbin:CMCC STOP");
        this.wifiManager.setWifiEnabled(false);
        this.waitTimes = 10;
        waitForWifiOff();
    }

    private void cmccTest() {
        writeCmd("CMCC TEST");
        this.mHandler.sendEmptyMessage(6);
        Log.d("WifiCMCCTest", "xbin:CMCC TEST");
    }

    private void writeCmd(String cmd) {
        if (this.mEf == null) {
            this.mEf = new engfetch();
        }
        this.mEf.writeCmd(cmd);
    }

    private boolean waitForWifiOff() {
        while (true) {
            int i = this.waitTimes;
            this.waitTimes = i - 1;
            if (i <= 0) {
                break;
            }
            Log.d("WifiCMCCTest", "off waitTimes=" + this.waitTimes);
            int wifiState = this.wifiManager.getWifiState();
            Log.d("WifiCMCCTest", "off wifiState=" + wifiState);
            if (wifiState == 1) {
                break;
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (this.waitTimes > 0) {
            return true;
        }
        return false;
    }
}

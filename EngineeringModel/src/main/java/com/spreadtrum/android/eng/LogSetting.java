package com.spreadtrum.android.eng;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class LogSetting extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private ListPreference DspPrefs;
    private CheckBoxPreference androidLogPrefs;
    private CheckBoxPreference kdumpPrefs;
    private String mATResponse;
    private String mATline;
    private engfetch mEf;
    private int mSocketID = 0;
    private int oldDSPValue = 0;
    private ByteArrayOutputStream outputBuffer;
    private DataOutputStream outputBufferStream;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.logsetting);
        Log.d("LogSetting", "logsetting activity onCreate.");
        this.androidLogPrefs = (CheckBoxPreference) findPreference("android_log_enable");
        this.kdumpPrefs = (CheckBoxPreference) findPreference("kdump_enable");
        this.DspPrefs = (ListPreference) findPreference("dsplog_enable");
        this.mEf = new engfetch();
        this.mSocketID = this.mEf.engopen();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    protected void onStart() {
        boolean z = true;
        Log.d("LogSetting", "logsetting activity onStart.");
        this.androidLogPrefs.setChecked(LogSettingGetLogState(4) == 1);
        CheckBoxPreference checkBoxPreference = this.kdumpPrefs;
        if (LogSettingGetLogState(5) != 1) {
            z = false;
        }
        checkBoxPreference.setChecked(z);
        this.oldDSPValue = LogSettingGetLogState(2);
        updataDSPOption(this.oldDSPValue);
        super.onStart();
    }

    private void updataDSPOption(int selectedId) {
        Log.d("LogSetting", "updataDSPOption selectedId=[" + selectedId + "]");
        this.DspPrefs.setValueIndex(selectedId);
        this.DspPrefs.setSummary(this.DspPrefs.getEntry());
    }

    protected void onDestroy() {
        this.mEf.engclose(this.mSocketID);
        super.onDestroy();
        Log.d("LogSetting", "logsetting activity onDestroy.");
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        Log.d("LogSetting", "[TreeCllik]onPreferenceTreeClick  key=" + key);
        if (key != null) {
            int logType;
            int newstate = 0;
            if (preference instanceof CheckBoxPreference) {
                if (((CheckBoxPreference) preference).isChecked()) {
                    newstate = 1;
                } else {
                    newstate = 0;
                }
            } else if (preference instanceof ListPreference) {
            }
            if (key.equals("applog_enable")) {
                logType = 0;
            } else if (key.equals("modemlog_enable")) {
                logType = 1;
            } else if ("modem_arm_log".equals(key)) {
                logType = 3;
            } else if ("android_log_enable".equals(key)) {
                logType = 4;
            } else if ("kdump_enable".equals(key)) {
                logType = 5;
            } else {
                Log.e("LogSetting", "Unknown type!");
            }
            int oldstate = LogSettingGetLogState(logType);
            if (oldstate < 0) {
                Log.d("LogSetting", "Invalid log state.");
            } else if (oldstate == newstate) {
                Toast.makeText(getApplicationContext(), "Replicated setting!", 0).show();
            } else {
                LogSettingSaveLogState(logType, newstate);
                String str = "LogSetting";
                Log.d(str, String.format("Log state changed, new state:%d, old state:%d", new Object[]{Integer.valueOf(newstate), Integer.valueOf(oldstate)}));
            }
        }
        return false;
    }

    private int LogSettingGetLogState(int logType) {
        int state = 1;
        byte[] inputBytes;
        switch (logType) {
            case 0:
                String property = SystemProperties.get("persist.sys.logstate", "CCC");
                if (property == "CCC") {
                    Log.d("LogSetting", "logcat property no exist.");
                    break;
                }
                state = property.compareTo("disable");
                break;
            case 1:
                this.outputBuffer = new ByteArrayOutputStream();
                this.outputBufferStream = new DataOutputStream(this.outputBuffer);
                try {
                    Log.e("LogSetting", "Engmode socket open, id:" + this.mSocketID);
                    this.mATline = 5 + "," + 0;
                    this.outputBufferStream.writeBytes(this.mATline);
                    this.mEf.engwrite(this.mSocketID, this.outputBuffer.toByteArray(), this.outputBuffer.toByteArray().length);
                    inputBytes = new byte[128];
                    this.mATResponse = new String(inputBytes, 0, this.mEf.engread(this.mSocketID, inputBytes, 128), Charset.defaultCharset());
                    try {
                        state = Integer.parseInt(this.mATResponse);
                        if (state > 1) {
                            state = -1;
                            break;
                        }
                    } catch (Exception e) {
                        Log.e("LogSetting", "NumberFormatException! : mATResponse = " + this.mATResponse);
                        return -1;
                    }
                } catch (IOException e2) {
                    Log.e("LogSetting", "writeBytes() error!");
                    return -1;
                } catch (NumberFormatException e3) {
                    Log.e("LogSetting", "at command return error");
                    return -1;
                }
                break;
            case 2:
                this.outputBuffer = new ByteArrayOutputStream();
                this.outputBufferStream = new DataOutputStream(this.outputBuffer);
                Log.e("LogSetting", "Engmode socket open, id:" + this.mSocketID);
                this.mATline = 61 + "," + 0;
                try {
                    this.outputBufferStream.writeBytes(this.mATline);
                    this.mEf.engwrite(this.mSocketID, this.outputBuffer.toByteArray(), this.outputBuffer.toByteArray().length);
                    inputBytes = new byte[128];
                    this.mATResponse = new String(inputBytes, 0, this.mEf.engread(this.mSocketID, inputBytes, 128), Charset.defaultCharset());
                    try {
                        state = Integer.parseInt(this.mATResponse);
                        if (state > 2 || state < 0) {
                            state = 0;
                            break;
                        }
                    } catch (Exception e4) {
                        Log.e("LogSetting", "LOG_DSP NumberFormatException! : mATResponse = " + this.mATResponse);
                        return 0;
                    }
                } catch (IOException e5) {
                    Log.e("LogSetting", "writeBytes() error!");
                    return -1;
                }
            case 3:
                try {
                    state = Integer.parseInt(SystemProperties.get("persist.sys.cardlog", "0"));
                    break;
                } catch (Exception e6) {
                    state = 0;
                    break;
                }
            case 4:
                state = "running".equals(SystemProperties.get("init.svc.logs4android", "")) ? 1 : 0;
                break;
            case 5:
                try {
                    state = Integer.parseInt(SystemProperties.get("persist.sys.kdump.enable", "0"));
                    break;
                } catch (Exception e7) {
                    state = 0;
                    break;
                }
        }
        return state;
    }

    private void LogSettingSaveLogState(int logType, int state) {
        byte[] inputBytes;
        switch (logType) {
            case 0:
                String property = state == 1 ? "enable" : "disable";
                SystemProperties.set("persist.sys.logstate", property);
                Log.d("LogSetting", "Set logcat property:" + property);
                return;
            case 1:
                this.outputBuffer = new ByteArrayOutputStream();
                this.outputBufferStream = new DataOutputStream(this.outputBuffer);
                Log.e("LogSetting", "Engmode socket open, id:" + this.mSocketID);
                this.mATline = 4 + "," + 1 + "," + state;
                try {
                    this.outputBufferStream.writeBytes(this.mATline);
                    this.mEf.engwrite(this.mSocketID, this.outputBuffer.toByteArray(), this.outputBuffer.toByteArray().length);
                    inputBytes = new byte[128];
                    this.mATResponse = new String(inputBytes, 0, this.mEf.engread(this.mSocketID, inputBytes, 128), Charset.defaultCharset());
                    Log.d("LogSetting", "AT response:" + this.mATResponse);
                    if (this.mATResponse.equals("OK")) {
                        Toast.makeText(getApplicationContext(), "Success!", 0).show();
                        return;
                    } else {
                        Toast.makeText(getApplicationContext(), "Fail!", 0).show();
                        return;
                    }
                } catch (IOException e) {
                    Log.e("LogSetting", "writeBytes() error!");
                    return;
                }
            case 2:
                this.outputBuffer = new ByteArrayOutputStream();
                this.outputBufferStream = new DataOutputStream(this.outputBuffer);
                Log.e("LogSetting", "Engmode socket open, id:" + this.mSocketID);
                this.mATline = 62 + "," + 1 + "," + state;
                try {
                    this.outputBufferStream.writeBytes(this.mATline);
                    this.mEf.engwrite(this.mSocketID, this.outputBuffer.toByteArray(), this.outputBuffer.toByteArray().length);
                    inputBytes = new byte[128];
                    this.mATResponse = new String(inputBytes, 0, this.mEf.engread(this.mSocketID, inputBytes, 128), Charset.defaultCharset());
                    Log.d("LogSetting", "AT response:" + this.mATResponse);
                    if (this.mATResponse.equals("OK")) {
                        Toast.makeText(getApplicationContext(), "Success!", 0).show();
                        updataDSPOption(state);
                        this.oldDSPValue = state;
                        return;
                    }
                    Toast.makeText(getApplicationContext(), "Fail!", 0).show();
                    updataDSPOption(this.oldDSPValue);
                    return;
                } catch (IOException e2) {
                    Log.e("LogSetting", "writeBytes() error!");
                    return;
                }
            case 3:
                SystemProperties.set("persist.sys.cardlog", state == 1 ? "1" : "0");
                return;
            case 4:
                if (state == 1) {
                    SystemProperties.set("ctl.start", "logs4android");
                    return;
                } else {
                    SystemProperties.set("ctl.stop", "logs4android");
                    return;
                }
            case 5:
                if (state == 1) {
                    SystemProperties.set("persist.sys.kdump.enable", "1");
                    return;
                } else {
                    SystemProperties.set("persist.sys.kdump.enable", "0");
                    return;
                }
            default:
                return;
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String re;
        if (key.equals("modem_arm_log")) {
            re = sharedPreferences.getString(key, "");
            Log.d("LogSetting", "onSharedPreferenceChanged key=" + key + " value=" + re);
            LogSettingSaveLogState(3, Integer.parseInt(re));
        } else if (key.equals("dsplog_enable")) {
            re = sharedPreferences.getString(key, "");
            Log.d("LogSetting", "onSharedPreferenceChanged key=" + key + " value=" + re);
            if (Integer.parseInt(re) != this.oldDSPValue) {
                LogSettingSaveLogState(2, Integer.parseInt(re));
            }
        }
    }
}

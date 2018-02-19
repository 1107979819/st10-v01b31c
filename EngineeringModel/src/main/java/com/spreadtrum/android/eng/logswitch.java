package com.spreadtrum.android.eng;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class logswitch extends PreferenceActivity implements OnPreferenceChangeListener {
    private engfetch mEf;
    private EventHandler mHandler;
    private CheckBoxPreference mPreference03;
    private CheckBoxPreference mPreference04;
    private CheckBoxPreference mPreference05;
    private int openlog = 0;
    private int sockid = 0;

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 4:
                case 10:
                case 58:
                    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
                    DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
                    Log.e("logswitch", "engopen sockid=" + logswitch.this.sockid);
                    try {
                        if (4 == msg.what) {
                            outputBufferStream.writeBytes(msg.what + "," + 1 + "," + logswitch.this.openlog);
                        } else if (58 == msg.what) {
                            outputBufferStream.writeBytes(msg.what + "," + 1 + "," + logswitch.this.openlog);
                        } else {
                            outputBufferStream.writeBytes(msg.what + "," + 2 + "," + msg.arg1 + "," + msg.arg2);
                        }
                        logswitch.this.mEf.engwrite(logswitch.this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                        Log.d("logswitch", "after engwrite");
                        byte[] inputBytes = new byte[512];
                        String str1 = new String(inputBytes, 0, logswitch.this.mEf.engread(logswitch.this.sockid, inputBytes, 512), Charset.defaultCharset());
                        if (str1.equals("OK")) {
                            logswitch.this.DisplayToast("Set Success.");
                            return;
                        } else if (str1.equals("ERROR")) {
                            logswitch.this.DisplayToast("Set Failed.");
                            return;
                        } else {
                            logswitch.this.DisplayToast("Unknown");
                            return;
                        }
                    } catch (IOException e) {
                        Log.e("logswitch", "writebytes error");
                        return;
                    }
                default:
                    return;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.logswitch);
        this.mPreference03 = (CheckBoxPreference) findPreference("integrity_set");
        this.mPreference03.setOnPreferenceChangeListener(this);
        this.mPreference04 = (CheckBoxPreference) findPreference("fband_set");
        this.mPreference04.setOnPreferenceChangeListener(this);
        this.mPreference05 = (CheckBoxPreference) findPreference("cap_log");
        this.mPreference05.setOnPreferenceChangeListener(this);
        initialpara();
    }

    private void initialpara() {
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        this.mHandler = new EventHandler(Looper.myLooper());
        this.mHandler.removeMessages(0);
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private void DisplayToast(String str) {
        Toast.makeText(this, str, 0).show();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        Log.d("logswitch", "onPreferenceChange newValue.toString() = " + newValue.toString());
        if (newValue.toString().equals("true")) {
            this.openlog = 1;
        } else if (newValue.toString().equals("false")) {
            this.openlog = 0;
        }
        if ("integrity_set".equals(key)) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(10, 1, this.openlog, Integer.valueOf(0)));
            return true;
        } else if ("fband_set".equals(key)) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(10, 11, this.openlog, Integer.valueOf(0)));
            return true;
        } else if (!"cap_log".equals(key)) {
            return false;
        } else {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(58, 0, 0, Integer.valueOf(0)));
            return true;
        }
    }
}

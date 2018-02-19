package com.spreadtrum.android.eng;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class aocsettings extends PreferenceActivity implements OnPreferenceChangeListener {
    private engfetch mEf;
    private EventHandler mHandler;
    private CheckBoxPreference mPreference01;
    private EditTextPreference mPreference02;
    private NumberKeyListener numberKeyListener = new NumberKeyListener() {
        private char[] numberChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        public int getInputType() {
            return 3;
        }

        protected char[] getAcceptedChars() {
            return this.numberChars;
        }
    };
    private int sockid = 0;
    private String strInput;

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
            DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
            Log.d("aocsettings", "engopen sockid=" + aocsettings.this.sockid);
            try {
                switch (msg.what) {
                    case 52:
                    case 80:
                    case 81:
                        outputBufferStream.writeBytes(msg.what + "," + 0);
                        break;
                    case 53:
                        outputBufferStream.writeBytes(msg.what + "," + 2 + "," + aocsettings.this.strInput + "," + "2345");
                        break;
                    default:
                        return;
                }
                aocsettings.this.mEf.engwrite(aocsettings.this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                byte[] inputBytes = new byte[128];
                String str1 = new String(inputBytes, 0, aocsettings.this.mEf.engread(aocsettings.this.sockid, inputBytes, 128), Charset.defaultCharset());
                Log.d("aocsettings", "AT = " + msg.what + ";return = " + str1);
                if (str1.equals("OK")) {
                    aocsettings.this.DisplayToast("Set Success.");
                } else if (str1.equals("ERROR")) {
                    r9 = aocsettings.this.mPreference01;
                    if (aocsettings.this.mPreference01.isChecked()) {
                        z = false;
                    }
                    r9.setChecked(z);
                    aocsettings.this.DisplayToast("Set Failed.");
                } else {
                    r9 = aocsettings.this.mPreference01;
                    if (aocsettings.this.mPreference01.isChecked()) {
                        z = false;
                    }
                    r9.setChecked(z);
                    aocsettings.this.DisplayToast("Unknown");
                }
            } catch (IOException e) {
                Log.e("aocsettings", "writebytes error");
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.aocsettings);
        this.mPreference01 = (CheckBoxPreference) findPreference("aoc_active");
        this.mPreference01.setOnPreferenceChangeListener(this);
        this.mPreference02 = (EditTextPreference) findPreference("aoc_setting");
        this.mPreference02.setOnPreferenceChangeListener(this);
        this.mPreference02.getEditText().setKeyListener(this.numberKeyListener);
        initialpara();
    }

    private void initialpara() {
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        this.mHandler = new EventHandler(Looper.myLooper());
        this.mHandler.removeMessages(0);
    }

    private void DisplayToast(String str) {
        Toast mToast = Toast.makeText(this, str, 0);
        mToast.setGravity(48, 0, 100);
        mToast.show();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if ("aoc_setting".equals(key)) {
            this.mPreference02.setSummary(newValue.toString());
            this.strInput = newValue.toString();
            this.mHandler.sendMessage(this.mHandler.obtainMessage(53, 0, 0, Integer.valueOf(0)));
            return true;
        } else if (!"aoc_active".equals(key)) {
            return false;
        } else {
            if (this.mPreference01.isChecked()) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(52, 0, 0, Integer.valueOf(0)));
                return true;
            }
            this.mHandler.sendMessage(this.mHandler.obtainMessage(80, 0, 0, Integer.valueOf(0)));
            return true;
        }
    }
}

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

public class autoattachatpoweron extends PreferenceActivity implements OnPreferenceChangeListener {
    private boolean hasPara;
    private CheckBoxPreference mCheckBoxPreference;
    private engfetch mEf;
    private EventHandler mHandler;
    private int onoroff = 0;
    private int sockid = 0;
    private String str;

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 45:
                case 46:
                    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
                    DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
                    Log.e("autoattachatpoweron", "engopen sockid=" + autoattachatpoweron.this.sockid);
                    if (autoattachatpoweron.this.hasPara) {
                        autoattachatpoweron.this.str = msg.what + "," + 1 + "," + autoattachatpoweron.this.onoroff;
                    } else {
                        autoattachatpoweron.this.str = msg.what + "," + 0;
                    }
                    try {
                        outputBufferStream.writeBytes(autoattachatpoweron.this.str);
                        autoattachatpoweron.this.mEf.engwrite(autoattachatpoweron.this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                        byte[] inputBytes = new byte[128];
                        String str1 = new String(inputBytes, 0, autoattachatpoweron.this.mEf.engread(autoattachatpoweron.this.sockid, inputBytes, 128), Charset.defaultCharset());
                        Log.e("autoattachatpoweron", "str1=" + str1);
                        if (str1.equals("0")) {
                            autoattachatpoweron.this.mCheckBoxPreference.setChecked(false);
                            autoattachatpoweron.this.onoroff = 1;
                            return;
                        } else if (str1.equals("1")) {
                            autoattachatpoweron.this.mCheckBoxPreference.setChecked(true);
                            autoattachatpoweron.this.onoroff = 0;
                            return;
                        } else if (str1.equals("OK")) {
                            Toast.makeText(autoattachatpoweron.this.getApplicationContext(), "Set Success.", 0).show();
                            if (1 == autoattachatpoweron.this.onoroff) {
                                autoattachatpoweron.this.onoroff = 0;
                                return;
                            } else {
                                autoattachatpoweron.this.onoroff = 1;
                                return;
                            }
                        } else if (str1.equals("error")) {
                            Toast.makeText(autoattachatpoweron.this.getApplicationContext(), "Set Failed.", 0).show();
                            return;
                        } else {
                            Toast.makeText(autoattachatpoweron.this.getApplicationContext(), "Unknown", 0).show();
                            return;
                        }
                    } catch (IOException e) {
                        Log.e("autoattachatpoweron", "writebytes error");
                        return;
                    }
                default:
                    return;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.autoattach);
        this.mCheckBoxPreference = (CheckBoxPreference) findPreference("autoattach_value");
        this.mCheckBoxPreference.setOnPreferenceChangeListener(this);
        this.hasPara = false;
        initialpara();
    }

    private void initialpara() {
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        this.mHandler = new EventHandler(Looper.myLooper());
        this.mHandler.removeMessages(0);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(45, 0, 0, Integer.valueOf(0)));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!preference.getKey().equals("autoattach_value")) {
            return false;
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(46, 0, 0, Integer.valueOf(0)));
        this.hasPara = true;
        return true;
    }
}

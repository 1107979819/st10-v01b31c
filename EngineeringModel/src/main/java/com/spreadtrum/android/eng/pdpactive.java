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

public class pdpactive extends PreferenceActivity implements OnPreferenceChangeListener {
    private int active = 0;
    private CheckBoxPreference mCheckBoxPreference;
    private engfetch mEf;
    private EventHandler mHandler;
    private int sockid = 0;
    private String str;

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 47:
                    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
                    DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
                    Log.e("pdpact", "engopen sockid=" + pdpactive.this.sockid);
                    pdpactive.this.str = msg.what + "," + 2 + "," + pdpactive.this.active + "," + 1;
                    Log.e("pdpact", "str=" + pdpactive.this.str);
                    try {
                        outputBufferStream.writeBytes(pdpactive.this.str);
                        pdpactive.this.mEf.engwrite(pdpactive.this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                        byte[] inputBytes = new byte[128];
                        String str1 = new String(inputBytes, 0, pdpactive.this.mEf.engread(pdpactive.this.sockid, inputBytes, 128));
                        if (str1.equals("OK")) {
                            Toast.makeText(pdpactive.this.getApplicationContext(), "Set Success.", 0).show();
                            if (1 == pdpactive.this.active) {
                                pdpactive.this.active = 0;
                                return;
                            } else {
                                pdpactive.this.active = 1;
                                return;
                            }
                        } else if (str1.equals("ERROR")) {
                            Toast.makeText(pdpactive.this.getApplicationContext(), "Set Failed.", 0).show();
                            return;
                        } else {
                            Toast.makeText(pdpactive.this.getApplicationContext(), "Unknown", 0).show();
                            return;
                        }
                    } catch (IOException e) {
                        Log.e("pdpact", "writebytes error");
                        return;
                    }
                default:
                    return;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.pdpactive);
        this.mCheckBoxPreference = (CheckBoxPreference) findPreference("pdpact");
        this.mCheckBoxPreference.setOnPreferenceChangeListener(this);
        initialpara();
    }

    private void initialpara() {
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        this.mHandler = new EventHandler(Looper.myLooper());
        this.mHandler.removeMessages(0);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!preference.getKey().equals("pdpact")) {
            return false;
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(47, 0, 0, Integer.valueOf(0)));
        return true;
    }
}

package com.spreadtrum.android.eng;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class smsserver extends PreferenceActivity implements OnPreferenceChangeListener {
    private engfetch mEf;
    private EventHandler mHandler;
    private ListPreference mListPreference;
    private int sockid = 0;
    private int valueofsms = 0;

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 51:
                    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
                    DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
                    Log.e("smsserver", "engopen sockid=" + smsserver.this.sockid);
                    try {
                        outputBufferStream.writeBytes(msg.what + "," + 1 + "," + smsserver.this.valueofsms);
                        smsserver.this.mEf.engwrite(smsserver.this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                        byte[] inputBytes = new byte[128];
                        String str1 = new String(inputBytes, 0, smsserver.this.mEf.engread(smsserver.this.sockid, inputBytes, 128), Charset.defaultCharset());
                        if (str1.equals("OK")) {
                            Toast.makeText(smsserver.this.getApplicationContext(), "Send Success.", 0).show();
                            return;
                        } else if (str1.equals("ERROR")) {
                            Toast.makeText(smsserver.this.getApplicationContext(), "Send Failed.", 0).show();
                            return;
                        } else {
                            Toast.makeText(smsserver.this.getApplicationContext(), "Unknown", 0).show();
                            return;
                        }
                    } catch (IOException e) {
                        Log.e("smsserver", "writebytes error");
                        return;
                    }
                default:
                    return;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.smsserver);
        this.mListPreference = (ListPreference) findPreference("smsserver");
        this.mListPreference.setOnPreferenceChangeListener(this);
        this.mListPreference.setSummary(this.mListPreference.getEntry());
        initialpara();
    }

    private void initialpara() {
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        this.mHandler = new EventHandler(Looper.myLooper());
        this.mHandler.removeMessages(0);
    }

    private void updateSelectList(Object value) {
        CharSequence[] summaries = getResources().getTextArray(R.array.smsinfo);
        CharSequence[] mEntryValue = this.mListPreference.getEntryValues();
        for (int i = 0; i < mEntryValue.length; i++) {
            if (mEntryValue[i].equals(value)) {
                this.mListPreference.setSummary(summaries[i]);
                this.mListPreference.setValueIndex(i);
                this.valueofsms = i;
                return;
            }
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(this.mListPreference.getKey())) {
            updateSelectList(newValue);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(51, 0, 0, Integer.valueOf(0)));
        }
        return false;
    }
}

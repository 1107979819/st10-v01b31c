package com.spreadtrum.android.eng;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
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

public class HsupaSetting extends PreferenceActivity implements OnPreferenceChangeListener {
    private CheckBoxPreference mCheckBoxPreference;
    private engfetch mEf;
    private EventHandler mEventHandler;
    private Handler mHander = new Handler();
    private HandlerThread mThread;
    private int sockid = 0;

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            String str;
            ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
            DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
            Log.e("HsupaSetting", "engopen sockid=" + HsupaSetting.this.sockid);
            switch (msg.what) {
                case 1:
                    str = 117 + "," + 0;
                    break;
                case 2:
                    str = 118 + "," + 0;
                    break;
                case 3:
                    str = 118 + "," + 0;
                    break;
                default:
                    return;
            }
            try {
                outputBufferStream.writeBytes(str);
                HsupaSetting.this.mEf.engwrite(HsupaSetting.this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                byte[] inputBytes = new byte[128];
                String str1 = new String(inputBytes, 0, HsupaSetting.this.mEf.engread(HsupaSetting.this.sockid, inputBytes, 128), Charset.defaultCharset());
                Log.e("HsupaSetting", "str1=" + str1);
                if (str1.equals("1")) {
                    HsupaSetting.this.setChecked(false);
                } else if (str1.equals("3")) {
                    HsupaSetting.this.setChecked(true);
                } else if (str1.equals("OK")) {
                    Toast.makeText(HsupaSetting.this.getApplicationContext(), "Set Success.", 0).show();
                } else if (str1.equals("error")) {
                    Toast.makeText(HsupaSetting.this.getApplicationContext(), "Set Failed.", 0).show();
                }
            } catch (IOException e) {
                Log.e("HsupaSetting", "writebytes error");
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.hsupa_setting);
        this.mCheckBoxPreference = (CheckBoxPreference) findPreference("hsupa_setting");
        this.mCheckBoxPreference.setOnPreferenceChangeListener(this);
        this.mThread = new HandlerThread("hsupa_setting");
        this.mThread.start();
        this.mEventHandler = new EventHandler(this.mThread.getLooper());
        initialpara();
    }

    private void initialpara() {
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        this.mEventHandler.sendMessage(this.mEventHandler.obtainMessage(1));
    }

    private void setChecked(final boolean checked) {
        this.mHander.post(new Runnable() {
            public void run() {
                HsupaSetting.this.mCheckBoxPreference.setChecked(checked);
            }
        });
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int i = 2;
        boolean value = false;
        if (!preference.getKey().equals("hsupa_setting")) {
            return false;
        }
        if (!this.mCheckBoxPreference.isChecked()) {
            value = true;
        }
        this.mEventHandler.removeMessages(2);
        this.mEventHandler.removeMessages(3);
        this.mEventHandler.removeMessages(1);
        EventHandler eventHandler = this.mEventHandler;
        if (!value) {
            i = 3;
        }
        this.mEventHandler.sendMessage(eventHandler.obtainMessage(i));
        return true;
    }
}

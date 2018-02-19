package com.spreadtrum.android.eng;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

public class Misc extends PreferenceActivity {
    private engfetch mEf;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Toast.makeText(Misc.this, R.string.memory_low, 0).show();
        }
    };
    private CheckBoxPreference mmsFillMemoryPref;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d("Misc", "receiver : device storage lower !");
            Misc.this.mHandler.removeMessages(1);
            Misc.this.mHandler.sendEmptyMessage(1);
        }
    };
    private CheckBoxPreference smsFillMemoryPref;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.prefs_misc);
        this.smsFillMemoryPref = (CheckBoxPreference) findPreference("sms_fill_memory");
        this.mmsFillMemoryPref = (CheckBoxPreference) findPreference("mms_fill_memory");
    }

    protected void onStart() {
        registerReceiver(this.receiver, new IntentFilter("android.intent.action.DEVICE_STORAGE_LOW"));
        super.onStart();
    }

    protected void onResume() {
        if (this.smsFillMemoryPref.isChecked()) {
            this.mmsFillMemoryPref.setEnabled(false);
        }
        if (this.mmsFillMemoryPref.isChecked()) {
            this.smsFillMemoryPref.setEnabled(false);
        }
        super.onResume();
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if ("sms_fill_memory".equals(key)) {
            if (((CheckBoxPreference) preference).isChecked()) {
                writeCmd("SMS MEM START");
                this.mmsFillMemoryPref.setEnabled(false);
                preference.setSummary("uncheck to delete memory");
            } else {
                writeCmd("MEM STOP");
                showDialog(1);
                this.mmsFillMemoryPref.setEnabled(true);
                preference.setSummary("");
            }
        } else if ("mms_fill_memory".equals(key)) {
            if (((CheckBoxPreference) preference).isChecked()) {
                writeCmd("MMS MEM START");
                this.smsFillMemoryPref.setEnabled(false);
                this.mHandler.removeMessages(1);
                this.mHandler.sendEmptyMessageDelayed(1, 150000);
                preference.setSummary("uncheck to delete memory");
            } else {
                writeCmd("MEM STOP");
                showDialog(1);
                this.smsFillMemoryPref.setEnabled(true);
                preference.setSummary("");
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    protected Dialog onCreateDialog(int id, Bundle args) {
        Builder builder = new Builder(this);
        switch (id) {
            case 1:
                return builder.setMessage(R.string.fill_memory_stop_msg).setNegativeButton(R.string.fill_memory_alert_ok, null).create();
            default:
                return null;
        }
    }

    protected void onStop() {
        unregisterReceiver(this.receiver);
        super.onStop();
    }

    private void writeCmd(String cmd) {
        if (this.mEf == null) {
            this.mEf = new engfetch();
        }
        this.mEf.writeCmd(cmd);
    }
}

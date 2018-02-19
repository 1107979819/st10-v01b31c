package com.spreadtrum.android.eng;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class EngModeBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("android.provider.Telephony.SECRET_CODE".equals(action)) {
            Intent intent2 = new Intent("android.intent.action.MAIN");
            intent2.setClass(context, engineeringmodel.class);
            intent2.setFlags(268435456);
            context.startActivity(intent2);
        } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (defaultSharedPreferences == null) {
                return;
            }
            if (defaultSharedPreferences.contains("cap_log")) {
                Editor edit = defaultSharedPreferences.edit();
                Log.d("EngModeBroadcastReceiver", "cap_log values : " + defaultSharedPreferences.getBoolean("cap_log", false));
                edit.putBoolean("cap_log", false);
                edit.apply();
                return;
            }
            Log.d("EngModeBroadcastReceiver", "cap_log values not exist !");
        }
    }
}

package com.spreadtrum.android.eng;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.util.Log;

public class AutoAnswerReceiver extends BroadcastReceiver {
    private final String TAG = "AutoAnswerReceiver";

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            boolean z = context.getSharedPreferences("ENGINEERINGMODEL", 0).getBoolean("autoanswer_call", false);
            Log.e("AutoAnswerReceiver", "start AutoAnswerService being" + z);
            if (z) {
                Log.e("AutoAnswerReceiver", "start AutoAnswerService");
                context.startService(new Intent(context, AutoAnswerService.class));
            }
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean z2 = defaultSharedPreferences.getBoolean("test_mode", false);
            z = defaultSharedPreferences.getBoolean("wire_test_mode", false);
            if (z2) {
                SystemProperties.set("ro.hisense.cmcc.test", "1");
            } else {
                SystemProperties.set("ro.hisense.cmcc.test", "0");
            }
            if (z) {
                SystemProperties.set("ro.hisense.cmcc.test.cmmb.wire", "1");
            } else {
                SystemProperties.set("ro.hisense.cmcc.test.cmmb.wire", "0");
            }
            if (SystemProperties.get("persist.sys.sprd.modemreset").isEmpty()) {
                SystemProperties.set("persist.sys.sprd.modemreset", "1");
            }
        }
    }
}

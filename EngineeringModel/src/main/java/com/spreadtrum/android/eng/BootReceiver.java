package com.spreadtrum.android.eng;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
    public static boolean isBoot = false;

    public void onReceive(Context context, Intent intent) {
        isBoot = true;
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("Qos_switch", false)) {
            SystemProperties.set("persist.sys.qosstate", "1");
        } else {
            SystemProperties.set("persist.sys.qosstate", "0");
        }
    }
}

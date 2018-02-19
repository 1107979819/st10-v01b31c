package com.spreadtrum.android.eng;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MMCReceiver extends BroadcastReceiver {
    private final String TAG = "MMCReceiver";

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            int i;
            MmcInfo.setLastValueWhenBootComplete();
            Log.i("TAG", MmcInfo.lastcrc.toString());
            Log.i("TAG", MmcInfo.lastTimeout.toString());
            for (i = 0; i < 8; i += 2) {
                Log.i("MMCReceiver", ((Integer) MmcInfo.lastcrc.get(i / 2)).toString());
            }
            for (i = 1; i < 8; i += 2) {
                Log.i("MMCReceiver", ((Integer) MmcInfo.lastTimeout.get(i / 2)).toString());
            }
        }
        if (intent.getAction().equals("android.intent.action.ACTION_SHUTDOWN")) {
            MmcInfo.getLastValue();
            MmcInfo.saveValue();
        }
    }
}

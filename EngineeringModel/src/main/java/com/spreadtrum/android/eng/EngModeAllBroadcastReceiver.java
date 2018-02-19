package com.spreadtrum.android.eng;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EngModeAllBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SECRET_CODE")) {
            Intent intent2 = new Intent();
            intent2.setClass(context, EngModeAllActivity.class);
            intent2.setFlags(268435456);
            context.startActivity(intent2);
        }
    }
}

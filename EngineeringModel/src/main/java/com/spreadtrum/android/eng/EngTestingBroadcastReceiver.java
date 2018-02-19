package com.spreadtrum.android.eng;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class EngTestingBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.provider.Telephony.SECRET_CODE")) {
            Uri data = intent.getData();
            String host = data.getHost();
            Log.d("abel", "uri=" + data);
            Intent intent2 = new Intent("android.intent.action.MAIN");
            intent2.setFlags(268435456);
            if ("83780".equals(host)) {
                intent2.setClass(context, Eng83780Activity.class);
                context.startActivity(intent2);
            } else if ("83781".equals(host)) {
                intent2.setClass(context, Eng83781Activity.class);
                context.startActivity(intent2);
            }
        }
    }
}

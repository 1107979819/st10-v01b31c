package com.spreadtrum.android.eng;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SlogService extends Service {
    private static final Class[] mStartForegroundSignature = new Class[]{Integer.TYPE, Notification.class};
    private static final Class[] mStopForegroundSignature = new Class[]{Boolean.TYPE};
    private final BroadcastReceiver mLocalChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d("SlogService", "language is change....");
            SlogService.this.setNotification();
        }
    };
    private Method mStartForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Method mStopForeground;
    private Object[] mStopForegroundArgs = new Object[1];
    Notification notification;

    public void onCreate() {
        try {
            this.mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
            this.mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            this.mStopForeground = null;
            this.mStartForeground = null;
        }
        registerReceiver(this.mLocalChangeReceiver, new IntentFilter("android.intent.action.LOCALE_CHANGED"));
    }

    public void onStart(Intent intent, int startId) {
        this.notification = new Notification(17301543, getText(R.string.notification_slogsvc_statusbarprompt), 0);
        setNotification();
    }

    private void setNotification() {
        Intent intent = new Intent(this, LogSettingSlogUITabHostActivity.class);
        intent.setFlags(268435456);
        this.notification.setLatestEventInfo(this, getText(R.string.notification_slogsvc_title), getText(R.string.notification_slogsvc_prompt), PendingIntent.getActivity(this, 0, intent, 0));
        new Thread(null, new Runnable() {
            public void run() {
                if (SlogService.this.mStartForeground != null) {
                    SlogService.this.mStartForegroundArgs[0] = Integer.valueOf(1);
                    SlogService.this.mStartForegroundArgs[1] = SlogService.this.notification;
                    try {
                        SlogService.this.mStartForeground.invoke(SlogService.this, SlogService.this.mStartForegroundArgs);
                    } catch (InvocationTargetException e) {
                        Log.w("Slog", "Unable to invoke startForeground", e);
                    } catch (IllegalAccessException e2) {
                        Log.w("Slog", "Unable to invoke startForeground", e2);
                    }
                }
            }
        }, "SlogService").start();
    }

    public void onDestroy() {
        if (this.mStopForeground != null) {
            this.mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                this.mStopForeground.invoke(this, this.mStopForegroundArgs);
                return;
            } catch (InvocationTargetException e) {
                Log.w("Slog", "Unable to invoke stopForeground", e);
                return;
            } catch (IllegalAccessException e2) {
                Log.w("Slog", "Unable to invoke stopForeground", e2);
                return;
            }
        }
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}

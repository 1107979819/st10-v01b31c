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

public class SlogUISnapService extends Service {
    private static final Class[] mStartSnapSvcSignature = new Class[]{Integer.TYPE, Notification.class};
    private static final Class[] mStopSnapSvcSignature = new Class[]{Boolean.TYPE};
    private final BroadcastReceiver mLocalChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d("SlogUISnapService", "language is change....");
            SlogUISnapService.this.setNotification();
        }
    };
    private Method mStartSnapSvc;
    private Object[] mStartSnapSvcArgs = new Object[2];
    private Object[] mStopSnapArgs = new Object[1];
    private Method mStopSnapSvc;
    private Notification notification;

    public void onCreate() {
        this.notification = new Notification(17301668, getText(R.string.notification_snapsvc_statusbarprompt), 0);
        setNotification();
        registerReceiver(this.mLocalChangeReceiver, new IntentFilter("android.intent.action.LOCALE_CHANGED"));
    }

    private void setNotification() {
        this.notification.setLatestEventInfo(this, getText(R.string.notification_snapsvc_title), getText(R.string.notification_snapsvc_prompt), PendingIntent.getActivity(this, 0, new Intent(this, SlogUISnapAction.class).setFlags(268435456), 134217728));
        try {
            this.mStartSnapSvc = getClass().getMethod("startForeground", mStartSnapSvcSignature);
            this.mStopSnapSvc = getClass().getMethod("stopForeground", mStopSnapSvcSignature);
        } catch (NoSuchMethodException e) {
            this.mStopSnapSvc = null;
            this.mStartSnapSvc = null;
        }
        new Thread(null, new Runnable() {
            public void run() {
                if (SlogUISnapService.this.mStartSnapSvc != null) {
                    SlogUISnapService.this.mStartSnapSvcArgs[0] = Integer.valueOf(2);
                    SlogUISnapService.this.mStartSnapSvcArgs[1] = SlogUISnapService.this.notification;
                    try {
                        SlogUISnapService.this.mStartSnapSvc.invoke(SlogUISnapService.this, SlogUISnapService.this.mStartSnapSvcArgs);
                    } catch (InvocationTargetException e) {
                        Log.w("Slog", "Unable to invoke startForeground", e);
                    } catch (IllegalAccessException e2) {
                        Log.w("Slog", "Unable to invoke startForeground", e2);
                    }
                }
            }
        }, "SnapThread").start();
    }

    public void onStart(Intent intent, int startId) {
    }

    public void onDestroy() {
        if (this.mStopSnapSvc != null) {
            this.mStopSnapArgs[0] = Boolean.TRUE;
            try {
                this.mStopSnapSvc.invoke(this, this.mStopSnapArgs);
                return;
            } catch (InvocationTargetException e) {
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
                return;
            } catch (IllegalAccessException e2) {
                Log.w("ApiDemos", "Unable to invoke stopForeground", e2);
                return;
            }
        }
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}

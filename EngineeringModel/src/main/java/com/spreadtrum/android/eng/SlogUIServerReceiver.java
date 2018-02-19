package com.spreadtrum.android.eng;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.Log;
import android.widget.Toast;

public class SlogUIServerReceiver extends BroadcastReceiver {
    private static boolean isCancelSynchronism = false;
    private static Object mLock = new Object();

    private class PackageSettingRunnable implements Runnable {
        boolean isEnabled;
        final String packageName = "com.android.synchronism";
        Context receiveContext;

        PackageSettingRunnable(Context context, boolean enable) {
            this.receiveContext = context;
            this.isEnabled = enable;
        }

        public void run() {
            try {
                int enable = this.isEnabled ? 1 : 2;
                SystemProperties.set("persist.sys.synchronism.enable", this.isEnabled ? "1" : "0");
                this.receiveContext.getPackageManager().setApplicationEnabledSetting("com.android.synchronism", enable, 0);
            } catch (IllegalArgumentException iae) {
                Log.e("Synchronism", "Package com.android.synchronism not exist." + iae);
            }
        }
    }

    private class SlogServiceRunnable implements Runnable {
        Intent receivedIntent;
        Context receiverContext;

        SlogServiceRunnable(Context tmpReceiverContext, Intent tmpReceivedIntent) {
            this.receiverContext = tmpReceiverContext;
            this.receivedIntent = tmpReceivedIntent;
        }

        public void run() {
            runSlogService();
        }

        private void runSlogService() {
            if (SlogAction.GetState("\n")) {
                SlogAction.sendATCommand(4, SlogAction.GetState("stream\tmodem\t"));
                SlogAction.sendATCommand(58, SlogAction.GetState("stream\ttcp\t"));
            }
            if (this.receivedIntent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                SlogAction.contextMainActivity = this.receiverContext;
                if (SlogAction.isAlwaysRun("slogsvc.conf") && this.receiverContext.startService(new Intent(this.receiverContext, SlogService.class)) == null) {
                    Toast.makeText(this.receiverContext, this.receiverContext.getText(R.string.toast_receiver_failed), 1).show();
                    Log.e("Slog->ServerReceiver", "Start service when BOOT_COMPLETE failed");
                }
                if (SlogAction.isAlwaysRun("snapsvc.conf") && this.receiverContext.startService(new Intent(this.receiverContext, SlogUISnapService.class)) == null) {
                    Toast.makeText(this.receiverContext, this.receiverContext.getText(R.string.toast_receiver_failed), 1).show();
                    Log.e("Slog->ServerReceiver", "Start service when BOOT_COMPLETE failed");
                }
            }
            if (SystemProperties.getBoolean("persist.sys.synchronism.support", false)) {
                this.receiverContext.startService(new Intent(this.receiverContext, EngInstallHelperService.class));
                SlogAction.SlogStart(this.receiverContext);
                return;
            }
            SlogAction.SlogStart();
        }
    }

    public void onReceive(Context receiverContext, Intent receivedIntent) {
        if (receivedIntent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SHandler sHandler = LogSettingSlogUITabHostActivity.mTabHostHandler;
            SHandler.setContext(receiverContext);
            new Thread(null, new SlogServiceRunnable(receiverContext, receivedIntent), "SlogServicePreload").start();
        }
        if (receivedIntent.getAction().equals("android.intent.action.SIM_STATE_CHANGED")) {
            String state = receivedIntent.getStringExtra("ss");
            int phoneId = receivedIntent.getIntExtra("phone_id", 0);
            if ("LOADED".equals(state)) {
                boolean enable = "cn".equals(SystemProperties.get("gsm.sim.operator.iso-country" + phoneId, "nil"));
                synchronized (mLock) {
                    PackageSettingRunnable psr = new PackageSettingRunnable(receiverContext, enable);
                    if (!isCancelSynchronism) {
                        new Thread(null, psr, "EnsuringSyncThread").start();
                    }
                    if (enable) {
                        isCancelSynchronism = true;
                    }
                }
            }
        }
    }
}

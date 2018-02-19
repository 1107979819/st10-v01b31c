package com.yuneec.flightmode15;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import com.yuneec.uartcontroller.UARTController;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";
    private UARTController mController;

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.i(TAG, "Yuneec launch completed");
        } else if (intent.getAction().equals("android.intent.action.ACTION_SHUTDOWN")) {
            Log.i(TAG, "Yuneec will shut down");
            this.mController = UARTController.getInstance();
            this.mController.startReading();
            this.mController.registerReaderHandler(new Handler());
            this.mController.shutDown(true);
        }
    }

    private void bootCompleted() {
        Throwable th;
        FileOutputStream fos = null;
        try {
            FileOutputStream fos2 = new FileOutputStream(new File("/dev/user_2_kernel"));
            try {
                fos2.write("power on now".getBytes());
                if (fos2 != null) {
                    try {
                        fos2.close();
                        fos = fos2;
                        return;
                    } catch (IOException e) {
                        fos = fos2;
                        return;
                    }
                }
            } catch (FileNotFoundException e2) {
                fos = fos2;
                try {
                    Log.e(TAG, "boot status file not found");
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e3) {
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (IOException e5) {
                fos = fos2;
                Log.e(TAG, "cann't not set boot status");
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e6) {
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                fos = fos2;
                if (fos != null) {
                    fos.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            Log.e(TAG, "boot status file not found");
            if (fos != null) {
                fos.close();
            }
        } catch (IOException e8) {
            Log.e(TAG, "cann't not set boot status");
            if (fos != null) {
                fos.close();
            }
        }
    }
}

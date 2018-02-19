package com.hipad.flightlauncher;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.util.Log;
import java.io.File;

public class MainActivity extends Activity {
    private final String TAG = "FlightLauncher";
    private Handler mHandler;
    private WifiManager manager = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.manager = (WifiManager) getSystemService("wifi");
        this.manager.setFrequencyBand(1, true);
        this.manager.setWifiEnabled(true);
        String CHECKFILE = "/productinfo/checkfactory.file";
        if (new File("/productinfo/checkfactory.file").exists()) {
            Secure.putInt(getContentResolver(), "adb_enabled", 0);
        }
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    Log.d("FlightLauncher", "set sys.landscape.ready");
                    SystemProperties.set("sys.landscape.ready", "1");
                    MainActivity.this.finish();
                }
            }
        };
        if (getWindowManager().getDefaultDisplay().getWidth() < getWindowManager().getDefaultDisplay().getHeight()) {
            Log.e("cui", "onCreate portrait");
            return;
        }
        Log.e("cui", "onCreate landscape");
        new Thread() {
            public void run() {
                SystemClock.sleep(10);
                MainActivity.this.mHandler.sendEmptyMessage(0);
            }
        }.start();
    }
}

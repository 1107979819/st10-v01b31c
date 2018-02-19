package com.spreadtrum.android.eng;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class WifiTesting extends Activity {
    private Handler backHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                WifiTesting.this.dialog.dismiss();
                WifiTesting.this.finish();
            }
        }
    };
    private Dialog dialog;
    private boolean isRestartWifi;
    private boolean isTesting;
    private boolean isWifiOff;
    private IntentFilter mIntentFilter;
    private Scanner mScanner;
    private WifiManager mWifiManager;
    private TextView resultTV;
    private StringBuilder sb;
    private TextView statusTV;
    private List<ScanResult> wifiList;
    private TextView wifiListTV;
    private WifiReceiver wifiReceiver;

    private class Scanner extends Handler {
        private int mRetry;

        private Scanner() {
            this.mRetry = 0;
        }

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        void pause() {
            this.mRetry = 0;
            removeMessages(0);
        }

        public void handleMessage(Message message) {
            if (WifiTesting.this.mWifiManager.startScanActive()) {
                this.mRetry = 0;
                return;
            }
            int i = this.mRetry + 1;
            this.mRetry = i;
            if (i > 3) {
                this.mRetry = 0;
                WifiTesting.this.resultTV.setText(R.string.wifi_no_network);
                WifiTesting.this.isTesting = false;
                return;
            }
            sendEmptyMessageDelayed(0, 6000);
        }
    }

    private final class WifiReceiver extends BroadcastReceiver {
        private WifiReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                WifiTesting.this.handleWifiApStateChanged(intent.getIntExtra("wifi_state", 4));
            } else if ("android.net.wifi.SCAN_RESULTS".equals(action)) {
                WifiTesting.this.updateAccessPoints();
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifitesting);
        this.wifiListTV = (TextView) findViewById(R.id.wifi_list);
        this.resultTV = (TextView) findViewById(R.id.result);
        this.statusTV = (TextView) findViewById(R.id.wifi_status);
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        this.wifiReceiver = new WifiReceiver();
        this.mScanner = new Scanner();
        this.isTesting = true;
    }

    protected void onPause() {
        if (this.wifiReceiver != null) {
            unregisterReceiver(this.wifiReceiver);
        }
        if (this.isWifiOff) {
            this.mWifiManager.setWifiEnabled(false);
        }
        this.mScanner.pause();
        super.onPause();
    }

    protected void onResume() {
        this.mIntentFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        registerReceiver(this.wifiReceiver, this.mIntentFilter);
        int wifiApState = this.mWifiManager.getWifiState();
        if (wifiApState == 2) {
            this.isWifiOff = false;
        } else if (wifiApState == 3) {
            this.mWifiManager.setWifiEnabled(false);
            this.isRestartWifi = true;
            this.isWifiOff = false;
        } else if (wifiApState == 0) {
            this.mWifiManager.setWifiEnabled(true);
            this.isWifiOff = false;
        } else {
            this.mWifiManager.setWifiEnabled(true);
            this.isWifiOff = true;
        }
        super.onResume();
    }

    private void handleWifiApStateChanged(int state) {
        switch (state) {
            case 0:
                this.statusTV.setText(R.string.wifi_stopping);
                return;
            case 1:
                this.statusTV.setText(R.string.wifi_stopped);
                if (this.isRestartWifi) {
                    this.mWifiManager.setWifiEnabled(true);
                    this.isRestartWifi = false;
                    return;
                }
                return;
            case 2:
                this.statusTV.setText(R.string.wifi_starting);
                return;
            case 3:
                this.statusTV.setText(R.string.wifi_start_scan);
                this.mScanner.resume();
                return;
            default:
                this.resultTV.setText(R.string.wifi_error);
                this.isTesting = false;
                return;
        }
    }

    private void updateAccessPoints() {
        this.sb = new StringBuilder();
        this.wifiList = this.mWifiManager.getScanResults();
        this.isTesting = false;
        if (this.wifiList == null || this.wifiList.isEmpty()) {
            this.wifiListTV.setText("");
            this.resultTV.setText(R.string.wifi_no_network);
            return;
        }
        ScanResult sr = (ScanResult) this.wifiList.get(0);
        this.sb.append(new Integer(1).toString() + ".");
        this.sb.append("SSID:").append(sr.SSID).append("  level:").append(sr.level);
        this.wifiListTV.setText(this.sb.toString());
        this.resultTV.setText(R.string.wifi_pass);
        this.statusTV.setText("");
        if (this.wifiReceiver != null) {
            unregisterReceiver(this.wifiReceiver);
            this.wifiReceiver = null;
        }
    }

    public void onBackPressed() {
        if (this.isTesting) {
            Toast.makeText(this, R.string.wifi_istesting, 0).show();
            return;
        }
        this.dialog = ProgressDialog.show(this, getString(R.string.wifi_isclosing_tit), getString(R.string.wifi_isclosing));
        if (this.wifiReceiver != null) {
            unregisterReceiver(this.wifiReceiver);
            this.wifiReceiver = null;
        }
        if (this.isWifiOff) {
            this.mWifiManager.setWifiEnabled(false);
        }
        this.mScanner.pause();
        Message msg = this.backHandler.obtainMessage();
        msg.what = 1;
        this.backHandler.sendMessageDelayed(msg, 5000);
    }
}

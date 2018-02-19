package com.yuneec.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import com.yuneec.flightmode15.R;

public class TestMain extends Activity implements OnClickListener {
    private Button mADBTest;
    private Button mControllerTest;
    private Button mGpsTest;
    private Button mSignalTest;
    private Button mTransmitTest;
    private Switch mWifiSwitch;
    private TextView text_wifi_test;
    private WifiManager wifiManager = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_main);
        getWindow().addFlags(128);
        this.mTransmitTest = (Button) findViewById(R.id.transmit_test);
        this.mTransmitTest.setOnClickListener(this);
        this.mControllerTest = (Button) findViewById(R.id.controller_test);
        this.mControllerTest.setOnClickListener(this);
        this.mSignalTest = (Button) findViewById(R.id.signal_test);
        this.mSignalTest.setOnClickListener(this);
        this.mGpsTest = (Button) findViewById(R.id.gps_test);
        this.mGpsTest.setOnClickListener(this);
        this.mADBTest = (Button) findViewById(R.id.adb_test);
        this.mADBTest.setOnClickListener(this);
        this.text_wifi_test = (TextView) findViewById(R.id.text_wifi_test);
        this.wifiManager = (WifiManager) super.getSystemService("wifi");
        this.mWifiSwitch = (Switch) findViewById(R.id.wifi_switch);
        this.mWifiSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    TestMain.this.wifiManager.setWifiEnabled(false);
                    TestMain.this.text_wifi_test.setText("wifi关闭中");
                    return;
                }
                TestMain.this.wifiManager.setWifiEnabled(true);
                TestMain.this.text_wifi_test.setText("wifi开启中");
            }
        });
        wifiSelect();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.transmit_test:
                startActivity(new Intent(this, TransmitterTest.class));
                return;
            case R.id.controller_test:
                startActivity(new Intent(this, ControllerTest.class));
                return;
            case R.id.gps_test:
                startActivity(new Intent(this, GpsTestActivity.class));
                return;
            case R.id.signal_test:
                startActivity(new Intent(this, SignalTest.class));
                return;
            case R.id.adb_test:
                startActivity(new Intent(this, ADBTestActivity.class));
                return;
            default:
                return;
        }
    }

    protected void onResume() {
        wifiSelect();
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
    }

    private void wifiSelect() {
        if (isWiFiActive(this)) {
            this.mWifiSwitch.setChecked(false);
            this.text_wifi_test.setText("wifi开启中");
            return;
        }
        this.mWifiSwitch.setChecked(true);
        this.text_wifi_test.setText("wifi关闭中");
    }

    public boolean isWiFiActive(Context inContext) {
        return ((WifiManager) inContext.getApplicationContext().getSystemService("wifi")).isWifiEnabled();
    }
}

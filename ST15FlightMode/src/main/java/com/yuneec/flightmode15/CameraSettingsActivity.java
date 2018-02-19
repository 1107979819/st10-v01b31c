package com.yuneec.flightmode15;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class CameraSettingsActivity extends PreferenceActivity {
    private String mCurrentCamera;
    private FragmentTransaction transaction;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mCurrentCamera = getIntent().getExtras().getString("currentCamera");
        boolean isWifiConnected = getIntent().getExtras().getBoolean("wifi_connected");
        this.transaction = getFragmentManager().beginTransaction();
        if ((this.mCurrentCamera.equals("C-GO3") || this.mCurrentCamera.equals(MainActivity.CAMERA_TYPE_CGO3_PRO)) && isWifiConnected) {
            this.transaction.replace(16908290, new CameraSettingsFragment());
            this.transaction.commit();
        } else if (this.mCurrentCamera.equals(MainActivity.CAMERA_TYPE_CGO2) && isWifiConnected) {
            this.transaction.replace(16908290, new Cgo2SettingsFragment());
            this.transaction.commit();
        } else {
            this.transaction.replace(16908290, new NormalSettingsFragment());
            this.transaction.commit();
        }
    }

    protected void onResume() {
        super.onResume();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

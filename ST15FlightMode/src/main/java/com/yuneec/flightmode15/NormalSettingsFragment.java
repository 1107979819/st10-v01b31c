package com.yuneec.flightmode15;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.widget.BaseDialog;
import com.yuneec.widget.SwitchWidgetPreference;

public class NormalSettingsFragment extends PreferenceFragment implements OnPreferenceClickListener {
    private static final String KEY_CALIBRATION = "calibration";
    private static final String KEY_GPS = "gps";
    private static final String TAG = "NormalSettingsFragment";
    private Preference mCalibrationPref;
    private UARTController mController = null;
    private SwitchWidgetPreference mGPSPref;
    private Handler mHandler = new Handler();
    private ProgressDialog mProgressDialog;
    private Runnable mProgressTimeOutRunnable = new Runnable() {
        public void run() {
            Toast.makeText(NormalSettingsFragment.this.getActivity(), R.string.str_communicate_failed, 1).show();
            NormalSettingsFragment.this.dismissProgressDialog();
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.normal_settings);
        Intent intent = getActivity().getIntent();
        int fmode = intent.getExtras().getInt("fmodeStatus");
        boolean gpsSwtich = intent.getExtras().getBoolean("gps_switch");
        this.mGPSPref = (SwitchWidgetPreference) findPreference(KEY_GPS);
        this.mGPSPref.setWidgetStates(gpsSwtich);
        this.mCalibrationPref = findPreference(KEY_CALIBRATION);
        this.mCalibrationPref.setOnPreferenceClickListener(this);
        if (16 != fmode) {
            this.mGPSPref.setEnabled(false);
            this.mCalibrationPref.setEnabled(false);
        } else {
            this.mGPSPref.setOnPreferenceClickListener(this);
        }
        this.mController = UARTController.getInstance();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mController != null) {
            this.mController = null;
        }
    }

    private void showProgressDialog() {
        this.mProgressDialog = ProgressDialog.show(getActivity(), null, getResources().getString(R.string.pls_waiting), false, false);
        this.mProgressDialog.setCancelable(true);
        this.mHandler.postDelayed(this.mProgressTimeOutRunnable, 5000);
    }

    private void dismissProgressDialog() {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
            this.mProgressDialog = null;
        }
        removeProgressRunnable();
    }

    private void removeProgressRunnable() {
        if (this.mProgressTimeOutRunnable != null) {
            this.mHandler.removeCallbacks(this.mProgressTimeOutRunnable);
        }
    }

    private void calibrationAction() {
        final Dialog dialog = new BaseDialog(getActivity(), R.style.dialog_style);
        dialog.setContentView(R.layout.calibration_dialog);
        ((Button) dialog.findViewById(R.id.compass)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (NormalSettingsFragment.this.mController != null) {
                    NormalSettingsFragment.this.mController.setTTBState(false, Utilities.HW_VB_BASE + 3, false);
                }
                dialog.dismiss();
            }
        });
        ((Button) dialog.findViewById(R.id.accelerometer)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (NormalSettingsFragment.this.mController != null) {
                    NormalSettingsFragment.this.mController.setTTBState(false, Utilities.HW_VB_BASE + 4, false);
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(KEY_CALIBRATION)) {
            calibrationAction();
        } else if (preference.getKey().equals(KEY_GPS) && this.mController != null) {
            boolean z;
            SwitchWidgetPreference switchWidgetPreference = this.mGPSPref;
            if (this.mGPSPref.getIsOnStates()) {
                z = false;
            } else {
                z = true;
            }
            switchWidgetPreference.setWidgetStates(z);
            if (Boolean.valueOf(this.mGPSPref.getIsOnStates()).booleanValue()) {
                this.mController.setTTBState(false, Utilities.HW_VB_BASE + 2, false);
            } else {
                this.mController.setTTBState(false, Utilities.HW_VB_BASE + 1, false);
            }
        }
        return true;
    }
}

package com.yuneec.flightmode15;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.yuneec.IPCameraManager.IPCameraManager;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.widget.BaseDialog;
import com.yuneec.widget.SwitchWidgetPreference;
import com.yuneec.widget.TextWidgetPreference;

public class Cgo2SettingsFragment extends PreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener {
    private static final String KEY_AUDIO = "audio";
    private static final String KEY_CALIBRATION = "calibration";
    private static final String KEY_GPS = "gps";
    private static final String KEY_RESOLUTION = "resolution";
    private static final String TAG = "Cgo2SettingsFragment";
    private SwitchWidgetPreference mAudioPref;
    private Preference mCalibrationPref;
    private UARTController mController = null;
    private SwitchWidgetPreference mGPSPref;
    private Handler mHandler = new Handler();
    private WeakHandler<PreferenceFragment> mHttpHandler = new WeakHandler<PreferenceFragment>(this) {
        public void handleMessage(Message msg) {
            if (((PreferenceFragment) getOwner()) == null) {
                Log.i(Cgo2SettingsFragment.TAG, "fragment destoryed");
                return;
            }
            switch (msg.what) {
                case 1:
                    switch (msg.arg1) {
                        case 24:
                        case IPCameraManager.REQUEST_SET_PHOTO_FORMAT /*45*/:
                        case IPCameraManager.REQUEST_SET_AE_ENABLE /*47*/:
                        case IPCameraManager.REQUEST_SET_IQ_TYPE /*51*/:
                        case IPCameraManager.REQUEST_SET_VIDEO_MODE /*57*/:
                        case IPCameraManager.REQUEST_SET_AUDIO_STATE /*59*/:
                            try {
                                if (!IPCameraManager.HTTP_RESPONSE_CODE_OK.equals((String) msg.obj)) {
                                    Log.e(Cgo2SettingsFragment.TAG, "Failed to communicate camera");
                                }
                                Cgo2SettingsFragment.this.dismissProgressDialog();
                                return;
                            } catch (Exception e) {
                                Log.e(Cgo2SettingsFragment.TAG, "Exception--Failed to communicate camera");
                                return;
                            }
                        case IPCameraManager.REQUEST_SET_VIDEO_RESOLUTION /*31*/:
                            if (!IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(String.valueOf(msg.obj))) {
                                Log.e(Cgo2SettingsFragment.TAG, "Failed to set resolution");
                                Cgo2SettingsFragment.this.dismissProgressDialog();
                            }
                            Cgo2SettingsFragment.this.dismissProgressDialog();
                            return;
                        case 32:
                            if (msg.obj instanceof String) {
                                Cgo2SettingsFragment.this.setResolutionPreference(msg.obj);
                                Cgo2SettingsFragment.this.dismissProgressDialog();
                                return;
                            }
                            return;
                        case IPCameraManager.REQUEST_SET_VIDEO_STANDARD /*33*/:
                            if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(String.valueOf(msg.obj))) {
                                Cgo2SettingsFragment.this.setVideoResolution(Cgo2SettingsFragment.this.mResolution);
                                return;
                            }
                            Log.e(Cgo2SettingsFragment.TAG, "Failed to set standard");
                            Cgo2SettingsFragment.this.dismissProgressDialog();
                            return;
                        case IPCameraManager.REQUEST_GET_VIDEO_MODE /*58*/:
                            try {
                                if (msg.obj instanceof String) {
                                    String videoMode = msg.obj;
                                    Cgo2SettingsFragment.this.mResolutionPref.setValue(videoMode);
                                    Cgo2SettingsFragment.this.mResolutionPref.setWidgetText(videoMode);
                                    Cgo2SettingsFragment.this.dismissProgressDialog();
                                    return;
                                }
                                Log.e(Cgo2SettingsFragment.TAG, "Failed to get video mode");
                                return;
                            } catch (Exception e2) {
                                Log.e(Cgo2SettingsFragment.TAG, "Exception--Failed to get video mode");
                                return;
                            }
                        case IPCameraManager.REQUEST_GET_AUDIO_STATE /*60*/:
                            try {
                                if (msg.obj instanceof Integer) {
                                    boolean checked;
                                    if (msg.obj.intValue() == 0) {
                                        checked = true;
                                    } else {
                                        checked = false;
                                    }
                                    Cgo2SettingsFragment.this.mAudioPref.setWidgetStates(checked);
                                } else {
                                    Log.e(Cgo2SettingsFragment.TAG, "Failed to get audio state");
                                }
                                Cgo2SettingsFragment.this.dismissProgressDialog();
                                return;
                            } catch (Exception e3) {
                                Log.e(Cgo2SettingsFragment.TAG, "Exception--Failed to get audio state");
                                return;
                            }
                        default:
                            return;
                    }
                default:
                    return;
            }
        }
    };
    private Messenger mHttpMessenger = new Messenger(this.mHttpHandler);
    private IPCameraManager mIPCameraManager;
    private ProgressDialog mProgressDialog;
    private Runnable mProgressTimeOutRunnable = new Runnable() {
        public void run() {
            Toast.makeText(Cgo2SettingsFragment.this.getActivity(), R.string.str_communicate_failed, 1).show();
            Cgo2SettingsFragment.this.dismissProgressDialog();
        }
    };
    private String mResolution;
    private TextWidgetPreference mResolutionPref;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.cgo2_settings);
        Intent intent = getActivity().getIntent();
        int fmode = intent.getExtras().getInt("fmodeStatus");
        boolean gpsSwtich = intent.getExtras().getBoolean("gps_switch");
        this.mResolutionPref = (TextWidgetPreference) findPreference(KEY_RESOLUTION);
        this.mResolutionPref.setOnPreferenceChangeListener(this);
        this.mAudioPref = (SwitchWidgetPreference) findPreference(KEY_AUDIO);
        this.mAudioPref.setOnPreferenceClickListener(this);
        this.mGPSPref = (SwitchWidgetPreference) findPreference(KEY_GPS);
        this.mCalibrationPref = findPreference(KEY_CALIBRATION);
        this.mCalibrationPref.setOnPreferenceClickListener(this);
        this.mGPSPref.setWidgetStates(gpsSwtich);
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
        this.mIPCameraManager = IPCameraManager.getIPCameraManager(getActivity(), 102);
        this.mIPCameraManager.getVideoResolution(this.mHttpMessenger);
        this.mIPCameraManager.getAudioState(this.mHttpMessenger);
        showProgressDialog();
    }

    public void onPause() {
        super.onPause();
        this.mIPCameraManager.finish();
        this.mIPCameraManager = null;
        this.mHandler.removeCallbacks(this.mProgressTimeOutRunnable);
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
                if (Cgo2SettingsFragment.this.mController != null) {
                    Cgo2SettingsFragment.this.mController.setTTBState(false, Utilities.HW_VB_BASE + 3, false);
                }
                dialog.dismiss();
            }
        });
        ((Button) dialog.findViewById(R.id.accelerometer)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (Cgo2SettingsFragment.this.mController != null) {
                    Cgo2SettingsFragment.this.mController.setTTBState(false, Utilities.HW_VB_BASE + 4, false);
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(KEY_AUDIO)) {
            int state;
            this.mAudioPref.setWidgetStates(!this.mAudioPref.getIsOnStates());
            if (this.mAudioPref.getIsOnStates()) {
                state = 0;
            } else {
                state = 1;
            }
            this.mIPCameraManager.setAudioState(this.mHttpMessenger, state);
        } else if (preference.getKey().equals(KEY_GPS)) {
            if (this.mController != null) {
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
        } else if (preference.getKey().equals(KEY_CALIBRATION)) {
            calibrationAction();
        }
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(KEY_RESOLUTION)) {
            this.mResolution = (String) newValue;
            setVideoStandard(this.mResolution);
            showProgressDialog();
        }
        return true;
    }

    private void setVideoStandard(String selectedRln) {
        if (selectedRln == null) {
            return;
        }
        if ("48P".equals(selectedRln) || "60P".equals(selectedRln)) {
            this.mIPCameraManager.setVideoStandard(this.mHttpMessenger, 1);
        } else if ("50P".equals(selectedRln)) {
            this.mIPCameraManager.setVideoStandard(this.mHttpMessenger, 2);
        }
    }

    private void setVideoResolution(String selectedRln) {
        if (selectedRln == null) {
            return;
        }
        if ("48P".equals(selectedRln)) {
            this.mIPCameraManager.setVideoResolution(this.mHttpMessenger, 5);
        } else if ("50P".equals(selectedRln)) {
            this.mIPCameraManager.setVideoResolution(this.mHttpMessenger, 3);
        } else if ("60P".equals(selectedRln)) {
            this.mIPCameraManager.setVideoResolution(this.mHttpMessenger, 1);
        }
    }

    private void setResolutionPreference(String responseValue) {
        this.mResolutionPref.setEnabled(true);
        if (responseValue.contains("60P")) {
            this.mResolutionPref.setValue("60P");
            this.mResolutionPref.setWidgetText(responseValue);
        } else if (responseValue.contains("50P")) {
            this.mResolutionPref.setValue("50P");
            this.mResolutionPref.setWidgetText(responseValue);
        } else if (responseValue.contains("48P")) {
            this.mResolutionPref.setValue("48P");
            this.mResolutionPref.setWidgetText(responseValue);
        } else {
            this.mResolutionPref.setEnabled(false);
        }
    }
}

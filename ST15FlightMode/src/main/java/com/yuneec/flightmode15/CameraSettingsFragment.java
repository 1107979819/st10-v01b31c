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
import android.preference.PreferenceGroup;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.yuneec.IPCameraManager.Amba2;
import com.yuneec.IPCameraManager.CameraParams;
import com.yuneec.IPCameraManager.IPCameraManager;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.widget.BaseDialog;
import com.yuneec.widget.SwitchWidgetPreference;
import com.yuneec.widget.TextWidgetPreference;

public class CameraSettingsFragment extends PreferenceFragment implements OnPreferenceClickListener, OnPreferenceChangeListener {
    public static final String CAMERA_TYPE_CGO3 = "C-GO3";
    private static final String KEY_AUDIO = "audio";
    private static final String KEY_CALIBRATION = "calibration";
    private static final String KEY_GPS = "gps";
    private static final String KEY_IQ_TYPE = "iq_type";
    private static final String KEY_PHOTO_FORMAT = "photo_format";
    private static final String KEY_RESET = "reset";
    private static final String KEY_RESOLUTION = "resolution";
    private static final String TAG = "SettingsFragment";
    private SwitchWidgetPreference mAudioPref;
    private Preference mCalibrationPref;
    private CameraParams mCameraParams;
    private UARTController mController = null;
    private String mCurrentCamera;
    private SwitchWidgetPreference mGPSPref;
    private Handler mHandler = new Handler();
    private WeakHandler<PreferenceFragment> mHttpHandler = new WeakHandler<PreferenceFragment>(this) {
        public void handleMessage(Message msg) {
            if (((PreferenceFragment) getOwner()) == null) {
                Log.i(CameraSettingsFragment.TAG, "fragment destoryed");
                return;
            }
            switch (msg.what) {
                case 1:
                    switch (msg.arg1) {
                        case 24:
                        case IPCameraManager.REQUEST_RESET_DEFAULT /*43*/:
                        case IPCameraManager.REQUEST_SET_PHOTO_FORMAT /*45*/:
                        case IPCameraManager.REQUEST_SET_AE_ENABLE /*47*/:
                        case IPCameraManager.REQUEST_SET_IQ_TYPE /*51*/:
                        case IPCameraManager.REQUEST_SET_VIDEO_MODE /*57*/:
                        case IPCameraManager.REQUEST_SET_AUDIO_STATE /*59*/:
                            try {
                                if (!IPCameraManager.HTTP_RESPONSE_CODE_OK.equals((String) msg.obj)) {
                                    Log.e(CameraSettingsFragment.TAG, "Failed to communicate camera");
                                }
                                CameraSettingsFragment.this.dismissProgressDialog();
                                return;
                            } catch (Exception e) {
                                Log.e(CameraSettingsFragment.TAG, "Exception--Failed to communicate camera");
                                return;
                            }
                        case IPCameraManager.REQUEST_GET_WORK_STATUS /*37*/:
                            if (msg.obj instanceof CameraParams) {
                                CameraSettingsFragment.this.mCameraParams = (CameraParams) msg.obj;
                                CameraSettingsFragment.this.initPerferences();
                                return;
                            }
                            return;
                        case IPCameraManager.REQUEST_GET_PHOTO_FORMAT /*46*/:
                            try {
                                if (msg.obj instanceof String) {
                                    String photoFormat = msg.obj;
                                    CameraSettingsFragment.this.mPhotoFormatPref.setValue(photoFormat);
                                    CameraSettingsFragment.this.mPhotoFormatPref.setWidgetText(photoFormat);
                                } else {
                                    Log.e(CameraSettingsFragment.TAG, "Failed to get photo format");
                                }
                                CameraSettingsFragment.this.dismissProgressDialog();
                                return;
                            } catch (Exception e2) {
                                Log.e(CameraSettingsFragment.TAG, "Exception--Failed to get photo format");
                                return;
                            }
                        case IPCameraManager.REQUEST_GET_IQ_TYPE /*52*/:
                            try {
                                if (msg.obj instanceof Integer) {
                                    Integer iqType = msg.obj;
                                    CameraSettingsFragment.this.mIQPref.setValue(String.valueOf(iqType));
                                    CameraSettingsFragment.this.mIQPref.setWidgetText(CameraSettingsFragment.this.getResources().getStringArray(R.array.iq_type)[iqType.intValue()]);
                                } else {
                                    Log.e(CameraSettingsFragment.TAG, "Failed to get IQ type");
                                }
                                CameraSettingsFragment.this.dismissProgressDialog();
                                return;
                            } catch (Exception e3) {
                                Log.e(CameraSettingsFragment.TAG, "Exception--Failed to get IQ type");
                                return;
                            }
                        case IPCameraManager.REQUEST_GET_VIDEO_MODE /*58*/:
                            try {
                                if (msg.obj instanceof String) {
                                    String videoMode = msg.obj;
                                    CameraSettingsFragment.this.mResolutionPref.setValue(videoMode);
                                    CameraSettingsFragment.this.mResolutionPref.setWidgetText(videoMode);
                                    CameraSettingsFragment.this.dismissProgressDialog();
                                    return;
                                }
                                Log.e(CameraSettingsFragment.TAG, "Failed to get video mode");
                                return;
                            } catch (Exception e4) {
                                Log.e(CameraSettingsFragment.TAG, "Exception--Failed to get video mode");
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
                                    CameraSettingsFragment.this.mAudioPref.setWidgetStates(checked);
                                } else {
                                    Log.e(CameraSettingsFragment.TAG, "Failed to get audio state");
                                }
                                CameraSettingsFragment.this.dismissProgressDialog();
                                return;
                            } catch (Exception e5) {
                                Log.e(CameraSettingsFragment.TAG, "Exception--Failed to get audio state");
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
    private Amba2 mIPCameraManager;
    private TextWidgetPreference mIQPref;
    private TextWidgetPreference mPhotoFormatPref;
    private ProgressDialog mProgressDialog;
    private Runnable mProgressTimeOutRunnable = new Runnable() {
        public void run() {
            Toast.makeText(CameraSettingsFragment.this.getActivity(), R.string.str_communicate_failed, 1).show();
            CameraSettingsFragment.this.dismissProgressDialog();
        }
    };
    private CameraResetDialogPreference mResetPref;
    private TextWidgetPreference mResolutionPref;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        int fmode = intent.getExtras().getInt("fmodeStatus");
        this.mCameraParams = (CameraParams) intent.getExtras().getParcelable("camera_params");
        this.mCurrentCamera = intent.getExtras().getString("currentCamera");
        addPreferencesFromResource(R.xml.camera_settings);
        this.mPhotoFormatPref = (TextWidgetPreference) findPreference(KEY_PHOTO_FORMAT);
        this.mPhotoFormatPref.setOnPreferenceChangeListener(this);
        this.mResolutionPref = (TextWidgetPreference) findPreference(KEY_RESOLUTION);
        if (this.mCurrentCamera.equals("C-GO3")) {
            this.mResolutionPref.setEntries(R.array.cgo3_resolution);
            this.mResolutionPref.setEntryValues(R.array.cgo3_resolution);
            ((PreferenceGroup) findPreference("camera_category")).removePreference(this.mPhotoFormatPref);
        } else {
            this.mResolutionPref.setEntries(R.array.cgo3_pro_resolution);
            this.mResolutionPref.setEntryValues(R.array.cgo3_pro_resolution);
        }
        this.mResolutionPref.setOnPreferenceChangeListener(this);
        this.mAudioPref = (SwitchWidgetPreference) findPreference(KEY_AUDIO);
        this.mAudioPref.setOnPreferenceClickListener(this);
        this.mIQPref = (TextWidgetPreference) findPreference(KEY_IQ_TYPE);
        this.mIQPref.setOnPreferenceChangeListener(this);
        this.mResetPref = (CameraResetDialogPreference) findPreference(KEY_RESET);
        this.mResetPref.setOnPreferenceChangeListener(this);
        this.mGPSPref = (SwitchWidgetPreference) findPreference(KEY_GPS);
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

    public void onStart() {
        super.onStart();
        this.mIPCameraManager = (Amba2) IPCameraManager.getIPCameraManager(getActivity(), 104);
        this.mIPCameraManager.getWorkStatus(this.mHttpMessenger);
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void onStop() {
        super.onStop();
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

    private void initPerferences() {
        boolean z;
        this.mResolutionPref.setWidgetText(this.mCameraParams.video_mode);
        SwitchWidgetPreference switchWidgetPreference = this.mAudioPref;
        if (this.mCameraParams.audio_sw == 0) {
            z = true;
        } else {
            z = false;
        }
        switchWidgetPreference.setWidgetStates(z);
        this.mPhotoFormatPref.setWidgetText(this.mCameraParams.photo_format);
        String[] iqTypes = getResources().getStringArray(R.array.iq_type);
        if (this.mCameraParams.iq_type >= iqTypes.length) {
            this.mCameraParams.iq_type = 0;
        }
        this.mIQPref.setWidgetText(iqTypes[this.mCameraParams.iq_type]);
        this.mGPSPref.setWidgetStates(getActivity().getIntent().getExtras().getBoolean("gps_switch"));
        this.mResolutionPref.setValue(this.mCameraParams.video_mode);
        this.mPhotoFormatPref.setValue(this.mCameraParams.photo_format);
        this.mIQPref.setValue(String.valueOf(this.mCameraParams.iq_type));
        if (this.mCameraParams.status.equals("record")) {
            this.mResolutionPref.setEnabled(false);
            this.mAudioPref.setEnabled(false);
            this.mPhotoFormatPref.setEnabled(false);
            this.mIQPref.setEnabled(false);
            this.mResetPref.setEnabled(false);
        }
        if (this.mCameraParams.cam_mode == 2) {
            this.mResolutionPref.setEnabled(false);
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
                if (CameraSettingsFragment.this.mController != null) {
                    CameraSettingsFragment.this.mController.setTTBState(false, Utilities.HW_VB_BASE + 3, false);
                }
                dialog.dismiss();
            }
        });
        ((Button) dialog.findViewById(R.id.accelerometer)).setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                if (CameraSettingsFragment.this.mController != null) {
                    CameraSettingsFragment.this.mController.setTTBState(false, Utilities.HW_VB_BASE + 4, false);
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
            String mode = (String) newValue;
            Log.i(TAG, "set Video mode to " + mode);
            this.mIPCameraManager.setVideoMode(this.mHttpMessenger, mode);
            this.mResolutionPref.setWidgetText(mode);
            showProgressDialog();
        } else if (preference.getKey().equals(KEY_PHOTO_FORMAT)) {
            String format = (String) newValue;
            Log.i(TAG, "set photo format to " + format);
            this.mIPCameraManager.setPhotoFormat(this.mHttpMessenger, format);
            this.mPhotoFormatPref.setWidgetText(format);
            showProgressDialog();
        } else if (preference.getKey().equals(KEY_IQ_TYPE)) {
            int type = Integer.parseInt((String) newValue);
            Log.i(TAG, "set IQ type to " + type);
            this.mIPCameraManager.setIQtype(this.mHttpMessenger, type);
            this.mIQPref.setWidgetText(getResources().getStringArray(R.array.iq_type)[type]);
            showProgressDialog();
        } else if (preference.getKey().equals(KEY_RESET)) {
            if (((Boolean) newValue).booleanValue()) {
                this.mIPCameraManager.resetDefault(this.mHttpMessenger);
                showProgressDialog();
            }
            Log.d(TAG, "Camera reset:" + newValue);
        }
        return true;
    }
}

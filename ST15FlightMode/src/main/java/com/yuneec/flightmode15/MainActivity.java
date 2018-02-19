package com.yuneec.flightmode15;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.media.SoundPool;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Vibrator;
import android.support.v4.view.MotionEventCompat;
import android.text.format.Formatter;
import android.text.format.Time;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.yuneec.IPCameraManager.Amba2;
import com.yuneec.IPCameraManager.CameraParams;
import com.yuneec.IPCameraManager.IPCameraManager;
import com.yuneec.IPCameraManager.IPCameraManager.RecordStatus;
import com.yuneec.IPCameraManager.IPCameraManager.RecordTime;
import com.yuneec.IPCameraManager.IPCameraManager.SDCardStatus;
import com.yuneec.IPCameraManager.IPCameraManager.ShutterTimeISO;
import com.yuneec.database.DBOpenHelper;
import com.yuneec.database.DataProvider;
import com.yuneec.database.DataProviderHelper;
import com.yuneec.flight_settings.BindWifiManage;
import com.yuneec.flight_settings.FlightSettings;
import com.yuneec.flightmode15.ChannelDataForward.ForwardCallback;
import com.yuneec.flightmode15.SpeciallyChannelSender.onButtonClickListener;
import com.yuneec.flightmode15.SyncModelDataTask.SyncModelDataCompletedAction;
import com.yuneec.mission.MissionPresentation.MissionState;
import com.yuneec.model_select.ModelSelectMain;
import com.yuneec.rtvplayer.RTVPlayer;
import com.yuneec.rtvplayer.RTVPlayer.VideoEventCallback;
import com.yuneec.uartcontroller.FCSensorData;
import com.yuneec.uartcontroller.FModeData;
import com.yuneec.uartcontroller.GPSUpLinkData;
import com.yuneec.uartcontroller.MixedData;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.uartcontroller.UARTInfoMessage;
import com.yuneec.uartcontroller.UARTInfoMessage.Channel;
import com.yuneec.uartcontroller.UARTInfoMessage.MissionReply;
import com.yuneec.uartcontroller.UARTInfoMessage.SwitchChanged;
import com.yuneec.uartcontroller.UARTInfoMessage.Telemetry;
import com.yuneec.uartcontroller.UARTInfoMessage.Trim;
import com.yuneec.widget.CounterView;
import com.yuneec.widget.CounterView.OnTickListener;
import com.yuneec.widget.HomeCompassView;
import com.yuneec.widget.IndicatorView;
import com.yuneec.widget.LeftTrimView;
import com.yuneec.widget.MissionView;
import com.yuneec.widget.MissionView.MissionViewCallback;
import com.yuneec.widget.MyProgressDialog;
import com.yuneec.widget.MyToast;
import com.yuneec.widget.OneButtonPopDialog;
import com.yuneec.widget.RightTrimView;
import com.yuneec.widget.StatusbarView;
import com.yuneec.widget.StyledNumberPicker;
import com.yuneec.widget.StyledNumberPicker.OnButtonClickedListener;
import com.yuneec.widget.StyledNumberPicker.OnScrollListener;
import com.yuneec.widget.StyledNumberPicker.OnValueChangeListener;
import com.yuneec.widget.SyncToggleButton;
import com.yuneec.widget.SyncToggleButton.OnUpdateChangeListener;
import com.yuneec.widget.ToggleButtonWithColorText;
import com.yuneec.widget.ToggleFrameView;
import com.yuneec.widget.ToggleFrameView.ToggleOnClickListener;
import com.yuneec.widget.TwoButtonPopDialog;
import com.yuneec.widget.VerticalSeekBar;
import com.yuneec.widget.WhiteBalanceScrollView;
import com.yuneec.widget.WhiteBalanceScrollView.OnItemSelectedListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity implements OnClickListener, OnCheckedChangeListener, AnimationListener, OnSeekBarChangeListener {
    public static final String CAMERA_TYPE_CGO2 = "C-GO2";
    public static final String CAMERA_TYPE_CGO3 = "C-GO3";
    public static final String CAMERA_TYPE_CGO3_PRO = "C-GO3-Pro";
    private static final boolean DEBUG_CH = false;
    private static final boolean DEBUG_WIFI = false;
    private static final int DEFAULT_COLOR = -16724737;
    private static final int LOW_CAMERA_STORAGE_THRESHOLD = 420;
    private static final int POSTANIM_DELAY_MILLIS = 630;
    private static final int PRIORITY_BATTERY_WARNING1 = 1;
    private static final int PRIORITY_BATTERY_WARNING2 = 4;
    private static final int PRIORITY_GPS_WARNING = 2;
    protected static final int RECONNECT_INTERVAL = 3000;
    public static final String SAVED_LOCAL_VIDEO = "/sdcard/FPV-Video/Local";
    private static final String TAG = "FlightModeMainActivity";
    private static final int TIME_SETTING_REQ = 1003;
    private static final String VIDEO_LOCATION = "rtsp://192.168.73.254:8556/PSIA/Streaming/channels/2?videoCodecType=H.264";
    private static final String VIDEO_LOCATION2 = "rtsp://192.168.73.254:8557/PSIA/Streaming/channels/2?videoCodecType=H.264";
    private static final String VIDEO_LOCATION3 = "rtsp://192.168.42.1/live";
    private static final String VIDEO_LOCATION4 = "rtsp://192.168.110.1/cam1/h264";
    private static int VOL_INDEX_MAX = 15;
    private static int mIndex = 0;
    private static final String mInvalidValueString = "N/A";
    private static float mTmpVOLFloatValue = 0.0f;
    private static float[] mVOLValue = new float[VOL_INDEX_MAX];
    private static final ArrayList<Integer> mWBList = new ArrayList();
    private static final float sCatBatteyValue = 10.4f;
    private static final float sLowBatteyValue = 10.6f;
    private float VolValue = 160.0f;
    private AlertDialog bindStatePrompt;
    private RelativeLayout cameraControlCombine;
    private int cameraControlCombineVisibility;
    private int cameraModeCache = 0;
    private int countTime = 0;
    private int currentPriority = 0;
    private FlightLog flightLog;
    private HandlerThread handlerThread;
    private int initLcdHeight;
    private int initLcdTopMargin;
    private int initLcdWidth;
    private boolean isChangeAndStartRecord = false;
    private boolean isCheckedBindState = false;
    private boolean isFSKConneted = false;
    private int isMasterControl;
    private int isMultiControl;
    private boolean isVibrating = false;
    private boolean isWIFIConneted = false;
    private OneButtonPopDialog mAirportWarningDialog;
    private OneButtonPopDialog mAltitudeWarningDialog;
    private Button mAutoBtn;
    private LinearLayout mAutoModeFrame;
    private Animation mBottomFadeIn;
    private Animation mBottomFadeOut;
    private Button mBtnFlightSetting;
    private Button mBtnModelSelect;
    private ToggleButton mBtnRecord;
    private Button mBtnSnapshot;
    private Button mBtnSystemSetting;
    private CameraDaemon mCameraDaemon;
    private Handler mCameraDaemonHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    MainActivity.this.onSDcardChanged((SDCardStatus) msg.obj);
                    return;
                case 102:
                    if (msg.obj instanceof RecordStatus) {
                        RecordStatus rs = msg.obj;
                        Log.i(MainActivity.TAG, "is recording:" + rs.isRecording);
                        MainActivity.this.setRecordBtnState(rs.isRecording);
                        return;
                    } else if (msg.obj instanceof CameraParams) {
                        MainActivity.this.mCameraParams = (CameraParams) msg.obj;
                        if (MainActivity.this.mCameraParams.status.equals("record")) {
                            if (!MainActivity.this.mHasStoppedRecord) {
                                MainActivity.this.setRecordBtnState(true);
                            }
                            MainActivity.this.mHasStartedRecord = false;
                        } else {
                            if (MainActivity.this.mCameraParams.status.equals("write err")) {
                                MainActivity.this.mStatusBarView.setInfoText(MainActivity.this.getResources().getString(R.string.hint_sd_exception), 0);
                                ((Amba2) MainActivity.this.mIPCameraManager).resetStatus(MainActivity.this.mHttpResponseMessenger);
                            }
                            if (!MainActivity.this.mHasStartedRecord) {
                                MainActivity.this.setRecordBtnState(false);
                            }
                            MainActivity.this.mHasStoppedRecord = false;
                        }
                        if (MainActivity.this.mCameraParams.cam_mode != 2) {
                            MainActivity.this.setRlnText(MainActivity.this.mCameraParams.video_mode);
                        } else {
                            MainActivity.this.setRlnText(null);
                        }
                        if (MainActivity.this.cameraModeCache != 0) {
                            Log.i(MainActivity.TAG, "CameraMode--cameraModeFlash != 0");
                            if (MainActivity.this.cameraModeCache == MainActivity.this.mCameraParams.cam_mode) {
                                Log.i(MainActivity.TAG, "CameraMode--cameraModeCache == mCameraParams.cam_mode, " + MainActivity.this.mCameraParams.cam_mode);
                                if (MainActivity.this.mSRswitchCacheRunnable != null) {
                                    Log.i(MainActivity.TAG, "CameraMode--remove SRswitch Runnable");
                                    MainActivity.this.mHandler.removeCallbacks(MainActivity.this.mSRswitchCacheRunnable);
                                }
                                MainActivity.this.cameraModeCache = 0;
                            } else {
                                Log.i(MainActivity.TAG, "CameraMode--cameraModeCache=" + MainActivity.this.cameraModeCache + " mCameraParams.cam_mode=" + MainActivity.this.mCameraParams.cam_mode);
                            }
                        } else {
                            Log.i(MainActivity.TAG, "CameraMode--cameraModeFlash == 0");
                            MainActivity.this.mSRswitch.syncState(MainActivity.this.mCameraParams.cam_mode != 2);
                        }
                        MainActivity.this.onSDcardChanged(MainActivity.this.mCameraParams.sdFree);
                        return;
                    } else {
                        return;
                    }
                case 105:
                    MainActivity.this.setRlnBtnText((String) msg.obj);
                    return;
                default:
                    return;
            }
        }
    };
    private String mCameraInfo;
    private CameraParams mCameraParams = new CameraParams();
    private Button mCameraSettingsBtn;
    private int mCameraShutterSoundId;
    private Runnable mChangeCameraModeRunnable = new Runnable() {
        public void run() {
            if (MainActivity.this.mProgressDialog != null && MainActivity.this.mProgressDialog.isShowing()) {
                MainActivity.this.mProgressDialog.dismiss();
            }
            Log.d(MainActivity.TAG, "CameraMode--get camera mode that change camera mode after 5 seconds");
            ((Amba2) MainActivity.this.mIPCameraManager).getCameraMode(MainActivity.this.mHttpResponseMessenger);
        }
    };
    private ProgressDialog mCommunicating;
    private Runnable mCommunicatingRunnable = new Runnable() {
        public void run() {
            MainActivity.this.dismissCommunicatingDialog();
            Toast.makeText(MainActivity.this, R.string.str_communicate_failed, 1).show();
        }
    };
    private CompassUpdater mCompassUpdater;
    private MyProgressDialog mConnectingDialog;
    private UARTController mController;
    private String mCurrentCameraName;
    private int mCurrentExposureIndex = 0;
    private int mCurrentFmode;
    private long mCurrentModelId;
    private long mCurrentModelType;
    private String mCurrentVideoLocation = VIDEO_LOCATION;
    private List<String> mEVListValue;
    private ImageView mEVdecrement;
    private ImageView mEVincrement;
    private StyledNumberPicker mEVpicker;
    private Runnable mExhibitionRunnable = new Runnable() {
        private int i = 0;

        public void run() {
            if (this.i == 0) {
                this.i++;
            } else if (this.i == 1) {
                this.i++;
            } else if (this.i == 2) {
                this.i++;
            } else if (this.i == 3) {
                this.i++;
            } else if (this.i == 4) {
                this.i = 0;
            }
            MainActivity.this.mHandler.postDelayed(MainActivity.this.mExhibitionRunnable, 1000);
        }
    };
    private Runnable mExhibitionRunnable2 = new Runnable() {
        int IntValue = 0;
        int IntValue2 = 0;
        float floatValue = 0.0f;
        float floatValue2 = 0.0f;
        boolean reverse = false;
        boolean reverse2 = false;
        boolean reverse3 = false;
        boolean reverse4;

        public void run() {
            if (this.reverse) {
                this.IntValue--;
            } else {
                this.IntValue++;
            }
            if (this.reverse2) {
                this.IntValue2--;
            } else {
                this.IntValue2++;
            }
            if (this.reverse3) {
                this.floatValue = (float) (((double) this.floatValue) - 0.1d);
            } else {
                this.floatValue = (float) (((double) this.floatValue) + 0.1d);
            }
            if (this.reverse4) {
                this.floatValue2 -= 1.0f;
            } else {
                this.floatValue2 += 1.0f;
            }
            MainActivity.this.mHomeCompassView.setDirection(this.IntValue);
            if (this.IntValue >= 360) {
                this.reverse = true;
            } else if (this.IntValue <= 0) {
                this.reverse = false;
            }
            if (this.IntValue2 >= 90) {
                this.reverse2 = true;
            } else if (this.IntValue2 <= -90) {
                this.reverse2 = false;
            }
            if (this.floatValue >= 90.0f) {
                this.reverse3 = true;
            } else if (this.floatValue <= 0.0f) {
                this.reverse3 = false;
            }
            if (this.floatValue2 >= 60.0f) {
                this.reverse4 = true;
            } else if (this.floatValue2 <= -60.0f) {
                this.reverse4 = false;
            }
            MainActivity.this.mHandler.postDelayed(MainActivity.this.mExhibitionRunnable2, 100);
        }
    };
    private ListView mFModeList;
    private AlertDialog mFModeOptions;
    private ToggleButtonWithColorText mFlightToggle;
    private OnCheckedChangeListener mFlightToggleListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (MainActivity.this.mController != null) {
                if (isChecked) {
                    MainActivity.this.enableButtonBar(false);
                    if (MainActivity.this.mCurrentModelType != 406) {
                        Utilities.setRunningMode(true);
                        Utilities.ensureRunState(MainActivity.this.mController);
                        MainActivity.this.mTimerHelper.startFlight();
                        MainActivity.this.mGPSUpdater.start(MainActivity.this.mHandler);
                        MainActivity.this.mCompassUpdater.start(MainActivity.this.mHandler);
                    } else {
                        MainActivity.this.mController.enterSim(true);
                        Utilities.ensureSimState(MainActivity.this.mController);
                    }
                    MainActivity.this.mIsPlayFPV = true;
                    if (MainActivity.this.isWIFIConneted) {
                        MainActivity.this.mBtnSnapshot.setEnabled(true);
                        MainActivity.this.mBtnRecord.setEnabled(true);
                        if (MainActivity.this.mIPCameraManager != null) {
                            MainActivity.this.mIPCameraManager.initCamera(MainActivity.this.mHttpResponseMessenger);
                            return;
                        }
                        return;
                    }
                    return;
                }
                if (MainActivity.this.mCurrentModelType != 406) {
                    Utilities.setRunningMode(false);
                    MainActivity.this.mController.exitRun(true);
                    MainActivity.this.mGPSUpdater.stop();
                    MainActivity.this.mCompassUpdater.stop();
                    MainActivity.this.mTimerHelper.endFlight();
                } else {
                    MainActivity.this.mController.exitSim(true);
                    Utilities.ensureAwaitState(MainActivity.this.mController);
                }
                MainActivity.this.enableButtonBar(true);
                MainActivity.this.mBtnSnapshot.setEnabled(false);
                MainActivity.this.mBtnRecord.setEnabled(false);
                MainActivity.this.mIsPlayFPV = false;
            }
        }
    };
    private OnTouchListener mFlightToggleTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            ToggleButton tb = (ToggleButton) v;
            if (!v.isEnabled()) {
                return true;
            }
            if (tb.isChecked()) {
                return false;
            }
            if (event.getAction() == 0) {
                if (MainActivity.this.checkModelStatus() == 0) {
                    return false;
                }
                v.setPressed(true);
                return true;
            } else if (event.getAction() != 1) {
                return false;
            } else {
                int check_status = MainActivity.this.checkModelStatus();
                if (check_status == 0) {
                    MainActivity.this.mIsPlayFPV = true;
                    return false;
                }
                if (check_status == R.string.connect_camera_hint) {
                    final TwoButtonPopDialog dialog = new TwoButtonPopDialog(MainActivity.this);
                    dialog.adjustHeight(380);
                    dialog.setTitle((int) R.string.camera_not_connected);
                    dialog.setMessage((int) R.string.camera_not_connected_prompt);
                    dialog.setPositiveButton(17039379, new OnClickListener() {
                        public void onClick(View v) {
                            MainActivity.this.mIsPlayFPV = false;
                            MainActivity.this.mFlightToggle.setChecked(true);
                            dialog.dismiss();
                        }
                    });
                    dialog.setNegativeButton(17039369, new OnClickListener() {
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });
                    dialog.show();
                } else {
                    MainActivity.this.showToastDontStack(check_status, 0);
                }
                v.setPressed(false);
                return true;
            }
        }
    };
    private boolean mFmodeBtnChecked = false;
    private Button mFmodeButton;
    private TextView mFmodeCH5;
    private TextView mFmodeCH6;
    private OneButtonPopDialog mFmodeFailDialog = null;
    private OneButtonPopDialog mGPSDisabledWarningDialog;
    private GPSUpdater mGPSUpdater;
    private boolean mGPSswtich = true;
    private GestureDetector mGesture;
    private Dialog mGuardDialog;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    if (MainActivity.this.mController != null && (msg.obj instanceof GPSUpLinkData)) {
                        GPSUpLinkData location = msg.obj;
                        MainActivity.this.mController.updateGps(false, location);
                        try {
                            MainActivity.this.flightLog.saveRemoteGpsFos(location.toString().getBytes());
                            return;
                        } catch (Exception e) {
                            Log.e(MainActivity.TAG, "write remote gps data error: " + e);
                            return;
                        }
                    }
                    return;
                case 200:
                    if (MainActivity.this.mController != null) {
                        MainActivity.this.mController.updateCompass(false, (float) msg.arg1);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHasStartedRecord = false;
    private boolean mHasStoppedRecord = false;
    private HomeCompassView mHomeCompassView;
    private HttpRequestHandler mHttpHandler = new HttpRequestHandler(this);
    private Messenger mHttpResponseMessenger = new Messenger(this.mHttpHandler);
    private IPCameraManager mIPCameraManager;
    private List<String> mISOListValue;
    private ImageView mISOdecrement;
    private ImageView mISOincrement;
    private StyledNumberPicker mISOpicker;
    private IndicatorView mIndicatorALT;
    private IndicatorView mIndicatorDIS;
    private IndicatorView mIndicatorGPS;
    private IndicatorView mIndicatorGPSStatus;
    private IndicatorView mIndicatorMODE;
    private IndicatorView mIndicatorPOS;
    private IndicatorView mIndicatorTAS;
    private IndicatorView mIndicatorVOL;
    private boolean mInitiailDataTranfered = false;
    private boolean mInitiailized = false;
    private IntentFilter mIntentFilter = new IntentFilter();
    private boolean mIsLocalRecording;
    private boolean mIsPaused = false;
    private boolean mIsPlayFPV = false;
    private boolean mIsRecording = false;
    private boolean mIsThrottleSafe = false;
    private boolean mIsWifiDelayConnecting;
    private SurfaceView mLCDDislpayView;
    private boolean mLastAirportWarning = false;
    private boolean mLastAltitudeWarning = false;
    private boolean mLastGPSDisabledWarning = true;
    private SDCardStatus mLastSDcardStatus = null;
    private boolean mLastVoltageLowWarning1 = false;
    private boolean mLastVoltageLowWarning2 = false;
    private Animation mLeftFadeIn;
    private Animation mLeftFadeOut;
    private LeftTrimView mLeftTrimView;
    private TextView mLinkSpeed;
    private RelativeLayout mMainscreenLcdFrame;
    private Button mManualBtn;
    private LinearLayout mManualModeFrame;
    private MissionView mMissionView;
    private OnClickListener mModeSwitchListener = new OnClickListener() {
        public void onClick(View arg0) {
            boolean z;
            if (arg0.equals(MainActivity.this.mAutoBtn)) {
                MainActivity.this.mAutoModeFrame.setVisibility(8);
                MainActivity.this.mManualModeFrame.setVisibility(0);
                MainActivity.this.mCameraParams.ae_enable = 0;
            } else if (arg0.equals(MainActivity.this.mManualBtn)) {
                MainActivity.this.mAutoModeFrame.setVisibility(0);
                MainActivity.this.mManualModeFrame.setVisibility(8);
                MainActivity.this.mCameraParams.ae_enable = 1;
            }
            ((Amba2) MainActivity.this.mIPCameraManager).setAEenable(MainActivity.this.mHttpResponseMessenger, MainActivity.this.mCameraParams.ae_enable);
            WbIsoChangedListener access$84 = MainActivity.this.wbIsoChangedListener;
            if (MainActivity.this.mCameraParams.ae_enable == 0) {
                z = true;
            } else {
                z = false;
            }
            access$84.onIsoModeChanged(z);
        }
    };
    private CounterView mOSDTime;
    private OnItemSelectedListener mOnItemSelectedListener = new OnItemSelectedListener() {
        public void onItemSelected(int position) {
            MainActivity.this.mWhitBalanceList.setItemSelect(position);
            MainActivity.this.wbIsoChangedListener.onWbModeChanged(position, true);
            int mode = ((Integer) MainActivity.mWBList.get(position)).intValue();
            MainActivity.this.showCommunicatingDialog();
            ((Amba2) MainActivity.this.mIPCameraManager).setWhiteBalance(MainActivity.this.mHttpResponseMessenger, mode);
        }
    };
    private OnButtonClickedListener mPickerButtonListener = new OnButtonClickedListener() {
        public void OnButtonClicked(StyledNumberPicker picker, int value) {
            int newVal;
            if (picker.equals(MainActivity.this.mEVpicker)) {
                newVal = MainActivity.this.mEVpicker.getValue();
                if (newVal == MainActivity.this.mEVpicker.getMaxValue()) {
                    MainActivity.this.mEVdecrement.setVisibility(4);
                } else if (newVal == MainActivity.this.mEVpicker.getMinValue()) {
                    MainActivity.this.mEVincrement.setVisibility(4);
                } else {
                    MainActivity.this.mEVincrement.setVisibility(0);
                    MainActivity.this.mEVdecrement.setVisibility(0);
                }
                MainActivity.this.mCameraParams.exposure_value = (String) MainActivity.this.mEVListValue.get(newVal);
                Log.d(MainActivity.TAG, "Set EV to: " + MainActivity.this.mCameraParams.exposure_value);
                ((Amba2) MainActivity.this.mIPCameraManager).setExposure(MainActivity.this.mHttpResponseMessenger, MainActivity.this.mCameraParams.exposure_value);
            } else if (picker.equals(MainActivity.this.mISOpicker)) {
                newVal = MainActivity.this.mISOpicker.getValue();
                if (newVal == MainActivity.this.mISOpicker.getMaxValue()) {
                    MainActivity.this.mISOdecrement.setVisibility(4);
                } else if (newVal == MainActivity.this.mEVpicker.getMinValue()) {
                    MainActivity.this.mISOincrement.setVisibility(4);
                } else {
                    MainActivity.this.mISOincrement.setVisibility(0);
                    MainActivity.this.mISOdecrement.setVisibility(0);
                }
                MainActivity.this.mCameraParams.iso = (String) MainActivity.this.mISOListValue.get(newVal);
                Log.d(MainActivity.TAG, "Set ISO to: " + MainActivity.this.mCameraParams.iso);
                ((Amba2) MainActivity.this.mIPCameraManager).setShutterTimeAndISO(MainActivity.this.mHttpResponseMessenger, MainActivity.this.mCameraParams.shutter_time, MainActivity.this.mCameraParams.iso);
            } else if (picker.equals(MainActivity.this.mSTpicker)) {
                newVal = MainActivity.this.mSTpicker.getValue();
                if (newVal == MainActivity.this.mSTpicker.getMaxValue()) {
                    MainActivity.this.mSTdecrement.setVisibility(4);
                } else if (newVal == MainActivity.this.mEVpicker.getMinValue()) {
                    MainActivity.this.mSTincrement.setVisibility(4);
                } else {
                    MainActivity.this.mSTincrement.setVisibility(0);
                    MainActivity.this.mSTdecrement.setVisibility(0);
                }
                MainActivity.this.mCameraParams.shutter_time = Integer.parseInt((String) MainActivity.this.mSHTimeListValue.get(MainActivity.this.mSTpicker.getValue()));
                Log.d(MainActivity.TAG, "Set shutter time to: " + MainActivity.this.mCameraParams.shutter_time);
                ((Amba2) MainActivity.this.mIPCameraManager).setShutterTimeAndISO(MainActivity.this.mHttpResponseMessenger, MainActivity.this.mCameraParams.shutter_time, MainActivity.this.mCameraParams.iso);
            }
        }
    };
    private OnValueChangeListener mPickerChangedListener = new OnValueChangeListener() {
        public void onValueChange(StyledNumberPicker picker, int oldVal, int newVal) {
            if (picker.equals(MainActivity.this.mEVpicker)) {
                if (newVal == MainActivity.this.mEVpicker.getMaxValue()) {
                    MainActivity.this.mEVdecrement.setVisibility(4);
                } else if (newVal == MainActivity.this.mEVpicker.getMinValue()) {
                    MainActivity.this.mEVincrement.setVisibility(4);
                } else {
                    MainActivity.this.mEVincrement.setVisibility(0);
                    MainActivity.this.mEVdecrement.setVisibility(0);
                }
            } else if (picker.equals(MainActivity.this.mISOpicker)) {
                if (newVal == MainActivity.this.mISOpicker.getMaxValue()) {
                    MainActivity.this.mISOdecrement.setVisibility(4);
                } else if (newVal == MainActivity.this.mEVpicker.getMinValue()) {
                    MainActivity.this.mISOincrement.setVisibility(4);
                } else {
                    MainActivity.this.mISOincrement.setVisibility(0);
                    MainActivity.this.mISOdecrement.setVisibility(0);
                }
            } else if (!picker.equals(MainActivity.this.mSTpicker)) {
            } else {
                if (newVal == MainActivity.this.mSTpicker.getMaxValue()) {
                    MainActivity.this.mSTdecrement.setVisibility(4);
                } else if (newVal == MainActivity.this.mEVpicker.getMinValue()) {
                    MainActivity.this.mSTincrement.setVisibility(4);
                } else {
                    MainActivity.this.mSTincrement.setVisibility(0);
                    MainActivity.this.mSTdecrement.setVisibility(0);
                }
            }
        }
    };
    private OnScrollListener mPickerScrollListener = new OnScrollListener() {
        public void onScrollStateChange(StyledNumberPicker view, int scrollState) {
            if (scrollState != 0) {
                return;
            }
            int newVal;
            if (view.equals(MainActivity.this.mEVpicker)) {
                newVal = MainActivity.this.mEVpicker.getValue();
                MainActivity.this.mCameraParams.exposure_value = (String) MainActivity.this.mEVListValue.get(newVal);
                Log.d(MainActivity.TAG, "Set EV to: " + MainActivity.this.mCameraParams.exposure_value);
                ((Amba2) MainActivity.this.mIPCameraManager).setExposure(MainActivity.this.mHttpResponseMessenger, MainActivity.this.mCameraParams.exposure_value);
            } else if (view.equals(MainActivity.this.mISOpicker)) {
                newVal = MainActivity.this.mISOpicker.getValue();
                MainActivity.this.mCameraParams.iso = (String) MainActivity.this.mISOListValue.get(newVal);
                Log.d(MainActivity.TAG, "Set ISO to: " + MainActivity.this.mCameraParams.iso);
                ((Amba2) MainActivity.this.mIPCameraManager).setShutterTimeAndISO(MainActivity.this.mHttpResponseMessenger, MainActivity.this.mCameraParams.shutter_time, MainActivity.this.mCameraParams.iso);
            } else if (view.equals(MainActivity.this.mSTpicker)) {
                newVal = MainActivity.this.mSTpicker.getValue();
                MainActivity.this.mCameraParams.shutter_time = Integer.parseInt((String) MainActivity.this.mSHTimeListValue.get(newVal));
                Log.d(MainActivity.TAG, "Set shutter time to: " + MainActivity.this.mCameraParams.shutter_time);
                ((Amba2) MainActivity.this.mIPCameraManager).setShutterTimeAndISO(MainActivity.this.mHttpResponseMessenger, MainActivity.this.mCameraParams.shutter_time, MainActivity.this.mCameraParams.iso);
            }
        }
    };
    private boolean mPostSendCommand = false;
    private SharedPreferences mPref;
    private MyProgressDialog mProgressDialog;
    private RTVPlayer mRTVPlayer;
    private OnTickListener mRecTickListener = new OnTickListener() {
        public void onTick(int seconds) {
        }

        public void onEnd() {
            MainActivity.this.mBtnRecord.setChecked(false);
        }

        public void onAlmostEnd() {
        }
    };
    private CounterView mRecTime;
    private Runnable mReconnectCameraRunnable = new Runnable() {
        public void run() {
            if (MainActivity.this.mIsPaused) {
                Log.d(MainActivity.TAG, "activity paused,reconnecting ignored");
            } else {
                MainActivity.this.mRTVPlayer.play(MainActivity.this.mCurrentVideoLocation);
            }
        }
    };
    private boolean mRecordProcessing = false;
    private OnCheckedChangeListener mRecordStateListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Log.i(MainActivity.TAG, "#### Rec mRecordStateListener->isChecked:[" + isChecked + "], mIsRecording:[" + MainActivity.this.mIsRecording + "] ,mRecTime.isStarted():[" + MainActivity.this.mRecTime.isStarted() + "]");
            if (isChecked && !MainActivity.this.mIsRecording) {
                MainActivity.this.mIPCameraManager.startRecord(MainActivity.this.mHttpResponseMessenger, MainActivity.this.mCameraInfo);
                MainActivity.this.startWaitingAnimation(MainActivity.this.mBtnRecord);
                MainActivity.this.mStatusBarView.setInfoText(MainActivity.this.getResources().getString(R.string.hint_start_record), 0);
            } else if (!isChecked && MainActivity.this.mIsRecording) {
                MainActivity.this.mIPCameraManager.stopRecord(MainActivity.this.mHttpResponseMessenger, MainActivity.this.mCameraInfo);
                MainActivity.this.startWaitingAnimation(MainActivity.this.mBtnRecord);
                MainActivity.this.mStatusBarView.setInfoText(MainActivity.this.getResources().getString(R.string.hint_stop_record), 0);
            } else if (isChecked || MainActivity.this.mIsRecording) {
                if (isChecked && MainActivity.this.mIsRecording && !MainActivity.this.mRecTime.isStarted()) {
                    MainActivity.this.mIPCameraManager.getRecordTime(MainActivity.this.mHttpResponseMessenger);
                }
            } else if (MainActivity.this.mRecTime.isStarted()) {
                MainActivity.this.mRecTime.stop();
                MainActivity.this.mRecTime.setVisibility(4);
                if (MainActivity.this.mCurrentCameraName.equals(MainActivity.CAMERA_TYPE_CGO3_PRO) || MainActivity.this.mCurrentCameraName.equals("C-GO3")) {
                    MainActivity.this.mSRswitch.setVisibility(0);
                }
            }
        }
    };
    private int mRecorderShutterSoundId;
    private TextView mRemoteGPSdata;
    private String mResolution;
    private Animation mRightFadeIn;
    private Animation mRightFadeOut;
    private RightTrimView mRightTrimView;
    private boolean mRlnBtnPressed = false;
    private Button mRlnButton;
    private Runnable mRlnProgressRunnable = new Runnable() {
        public void run() {
            if (MainActivity.this.mProgressDialog != null && MainActivity.this.mProgressDialog.isShowing()) {
                MainActivity.this.mProgressDialog.dismiss();
            }
            MainActivity.this.mStatusBarView.setInfoText(MainActivity.this.getResources().getString(R.string.str_connect_failed), 0);
        }
    };
    private ResolutionSelection mRlnSelection;
    private TextView mRlnText;
    private TextView mRssi;
    private boolean mSDErrDialogShown = false;
    private List<String> mSHTimeListValue;
    private SyncToggleButton mSRswitch;
    private Runnable mSRswitchCacheRunnable = new Runnable() {
        public void run() {
            MainActivity.this.mSRswitch.syncState(MainActivity.this.mCameraParams.cam_mode != 2);
            MainActivity.this.cameraModeCache = 0;
            Log.i(MainActivity.TAG, "CameraMode--switch camera mode timeout");
        }
    };
    private TextView mSSID;
    private ImageView mSTdecrement;
    private ImageView mSTincrement;
    private StyledNumberPicker mSTpicker;
    private boolean mSnapShotProcessing = false;
    private SoundPool mSoundPool;
    private StatusbarView mStatusBarView;
    private TimerHelper mTimerHelper;
    private Toast mToast;
    private Animation mTopFadeIn;
    private Animation mTopFadeOut;
    private Animation mTrimLeftFadeIn;
    private Animation mTrimLeftFadeOut;
    private Animation mTrimRightFadeIn;
    private Animation mTrimRightFadeOut;
    private Handler mUartHandler = new Handler() {
        private final int MAX_TIME_GAP_THRESHOLD = 5000;
        long currentFeedbackTimestamp = System.currentTimeMillis();
        private boolean isChangeStatus = true;
        boolean one_flight = true;

        public void handleMessage(Message msg) {
            if (MainActivity.this.mController == null) {
                Log.i(MainActivity.TAG, "the activity was paused");
                return;
            }
            if (msg.obj instanceof UARTInfoMessage) {
                UARTInfoMessage umsg = msg.obj;
                switch (umsg.what) {
                    case 2:
                        MainActivity.this.handleZoomAction((Channel) umsg);
                        break;
                    case 4:
                        Trim tmsg = (Trim) umsg;
                        MainActivity.this.mRightTrimView.setValue(MainActivity.this.trimDataConversion(tmsg.t1), MainActivity.this.trimDataConversion(tmsg.t2));
                        MainActivity.this.mLeftTrimView.setValue(MainActivity.this.trimDataConversion(tmsg.t3), MainActivity.this.trimDataConversion(tmsg.t4));
                        break;
                    case 5:
                        Channel cmsg = (Channel) umsg;
                        MainActivity.this.handleTimerTirgger(cmsg);
                        if (cmsg.channels.size() > 0) {
                            MainActivity.this.mIsThrottleSafe = ((Float) cmsg.channels.get(0)).floatValue() <= 100.0f;
                            ChannelDataForward.getInstance().forwardMixChannelData(cmsg);
                            try {
                                MainActivity.this.flightLog.saveRemoteFos(cmsg.toString().getBytes());
                                break;
                            } catch (Exception e) {
                                Log.e(MainActivity.TAG, "write remote data error: " + e);
                                break;
                            }
                        }
                        break;
                    case 13:
                        Telemetry info = (Telemetry) umsg;
                        MainActivity.this.handleTelemetryInfoChanged(info);
                        if (info != null) {
                            try {
                                byte[] bytes = info.toString().getBytes();
                                if (bytes.length > 0) {
                                    if (this.one_flight) {
                                        MainActivity.this.flightLog.createFlightNote(MainActivity.this);
                                        this.one_flight = false;
                                    }
                                    MainActivity.this.flightLog.saveTelemetryFos(bytes);
                                }
                            } catch (Exception e2) {
                                Log.e(MainActivity.TAG, "write error: " + e2);
                            }
                        }
                        this.currentFeedbackTimestamp = System.currentTimeMillis();
                        break;
                    case 14:
                        MainActivity.this.handleSwitchChanged((SwitchChanged) umsg);
                        break;
                    case 23:
                        if (Utilities.PROJECT_TAG.equals("ST12")) {
                            MainActivity.this.mMissionView.updateFeedback((MissionReply) umsg);
                            break;
                        }
                        break;
                }
            }
            if (this.isChangeStatus && MainActivity.this.mConnectingDialog != null) {
                if (MainActivity.this.isWIFIConneted) {
                    MainActivity.this.mConnectingDialog.setMessage(MainActivity.this.getResources().getText(R.string.str_connect_wifi_completed));
                } else {
                    MainActivity.this.mConnectingDialog.setMessage(MainActivity.this.getResources().getText(R.string.str_connect_status));
                }
            }
            long feedbackGapTime = System.currentTimeMillis() - this.currentFeedbackTimestamp;
            if (feedbackGapTime > 5000 && MainActivity.this.isFSKConneted) {
                Log.i(MainActivity.TAG, "FSK may disconnected!");
                this.isChangeStatus = true;
                MainActivity.this.isFSKConneted = false;
                this.one_flight = true;
                MainActivity.this.mCameraSettingsBtn.setVisibility(0);
                if (!MainActivity.this.isWIFIConneted) {
                    MainActivity.this.mCameraSettingsBtn.setEnabled(false);
                }
                MainActivity.this.initIndicatorValue();
                MainActivity.this.resetWarnings();
                MainActivity.this.flightLog.closedFlightNote(false);
            }
            if (feedbackGapTime <= 5000 && !MainActivity.this.isFSKConneted) {
                this.isChangeStatus = false;
                MainActivity.this.isFSKConneted = true;
                MainActivity.this.mCameraSettingsBtn.setEnabled(true);
                if (MainActivity.this.mConnectingDialog == null) {
                    return;
                }
                if (MainActivity.this.isWIFIConneted) {
                    MainActivity.this.mConnectingDialog.setMessage(MainActivity.this.getResources().getText(R.string.str_connect_all_completed));
                } else {
                    MainActivity.this.mConnectingDialog.setMessage(MainActivity.this.getResources().getText(R.string.str_connect_fsk_completed));
                }
            }
        }
    };
    private int mValueForZoom = 1;
    private int mVehicleType;
    private Vibrator mVibrator;
    private VideoEventCallback mVideoEventCallback = new VideoEventCallback() {
        public void onPlayerSurfaceDestroyed(SurfaceHolder holder) {
        }

        public void onPlayerSurfaceCreated(SurfaceHolder holder) {
        }

        public void onPlayerSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        public void onPlayerStopped() {
        }

        public void onPlayerSnapshotTaken() {
        }

        public void onPlayerRecordableChanged() {
        }

        public void onPlayerPlaying() {
            Log.d(MainActivity.TAG, "#### Rec RTV playing...");
            if (MainActivity.this.mIsLocalRecording) {
                MainActivity.this.mRTVPlayer.startRecord("/sdcard/FPV-Video/Local/" + MainActivity.this.prepareFilename());
                MainActivity.this.mIPCameraManager.getRecordTime(MainActivity.this.mHttpResponseMessenger);
            }
            MainActivity.this.dismissProgressDialog(null);
        }

        public void onPlayerPlayerRecordingFinished() {
        }

        public void onPlayerEndReached() {
            MainActivity.this.reconnectCamera();
        }

        public void onPlayerEncoutneredError() {
            MainActivity.this.reconnectCamera();
        }
    };
    private Dialog mVoltageLowWarning1Dialog;
    private Dialog mVoltageLowWarning2Dialog;
    private WhiteBalanceScrollView mWhitBalanceList;
    private Runnable mWifiEnabledRunnable = new Runnable() {
        public void run() {
            Log.d(MainActivity.TAG, "mWifiEnabledRunnable run!");
            MainActivity.this.isWIFIConneted = false;
            if (MainActivity.this.isBindWifi(MainActivity.this, MainActivity.this.mCurrentModelId)) {
                MainActivity.this.showConnectingDialog();
                MainActivity.this.connectWifi();
            }
            MainActivity.this.mIsWifiDelayConnecting = false;
        }
    };
    private TextView mWifiState;
    private WifiThread mWifiThread;
    private Runnable mZoomRunnable = new Runnable() {
        public void run() {
            if (MainActivity.this.mIPCameraManager != null) {
                MainActivity.this.mIPCameraManager.zoom(MainActivity.this.mValueForZoom, MainActivity.this.mHttpResponseMessenger);
            }
        }
    };
    private VerticalSeekBar mZoombar;
    private View mZoombarContainer;
    private boolean motorStatusErr = false;
    private BroadcastReceiver receiverWifi = new BroadcastReceiver() {
        private Handler heartBeatHandler;

        class HeartbeatEventHandler extends Handler {
            private static final int PING_ROUTER = 2;
            public static final int START_PING = 1;
            public static final int STOP_PING = 3;
            private int missPingCount;
            private String routerAddress;
            private boolean stopPing = true;

            public HeartbeatEventHandler(Looper looper) {
                super(looper);
            }

            private String executeCmd(String cmd, boolean sudo) {
                Process p;
                if (sudo) {
                    p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
                } else {
                    try {
                        p = Runtime.getRuntime().exec(cmd);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "";
                    }
                }
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String res = "";
                while (true) {
                    String s = stdInput.readLine();
                    if (s == null) {
                        return res;
                    }
                    res = new StringBuilder(String.valueOf(res)).append(s).append("\n").toString();
                }
            }

            public void handleMessage(Message message) {
                switch (message.what) {
                    case 1:
                        this.routerAddress = Formatter.formatIpAddress(((WifiManager) MainActivity.this.getSystemService("wifi")).getDhcpInfo().gateway);
                        if (this.routerAddress != null) {
                            Log.d(MainActivity.TAG, "Get gateway ip: " + this.routerAddress);
                            this.stopPing = false;
                            this.missPingCount = 0;
                            sendEmptyMessageDelayed(2, 1000);
                            return;
                        }
                        return;
                    case 2:
                        if (!this.stopPing) {
                            boolean isPingPassed = executeCmd("ping -c 1 -w 1 " + this.routerAddress, false).contains(" 0% packet loss,");
                            Log.d(MainActivity.TAG, "Ping result, isPass: " + isPingPassed);
                            if (isPingPassed) {
                                this.missPingCount = 0;
                            } else {
                                int i = this.missPingCount + 1;
                                this.missPingCount = i;
                                if (i > 5) {
                                    executeCmd("ifconfig wlan0 down", false);
                                    executeCmd("ifconfig wlan0 up", false);
                                    Log.d(MainActivity.TAG, "restart wlan0");
                                    this.missPingCount = 0;
                                }
                            }
                            sendEmptyMessageDelayed(2, 1000);
                            return;
                        }
                        return;
                    case 3:
                        this.stopPing = true;
                        return;
                    default:
                        return;
                }
            }
        }

        public void onReceive(Context context, Intent intent) {
            if (MainActivity.this.wifiManager.isWifiEnabled()) {
                String action = intent.getAction();
                Log.i(MainActivity.TAG, action);
                if (action.equals("android.net.wifi.SCAN_RESULTS")) {
                    Log.v(MainActivity.TAG, " ----- connecting I---" + action);
                } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED") || action.equals("android.net.wifi.supplicant.STATE_CHANGE")) {
                    Log.v(MainActivity.TAG, " ----- connecting II---" + action);
                    if (intent.getIntExtra("wifi_state", 4) == 3) {
                        enableWifiDelay();
                    }
                } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    State state = ((NetworkInfo) intent.getParcelableExtra("networkInfo")).getState();
                    Log.d(MainActivity.TAG, "NETWORK_STATE_CHANGED_ACTION:" + state.name() + ", mIsWifiDelayConnecting=" + MainActivity.this.mIsWifiDelayConnecting);
                    if (State.CONNECTED == state) {
                        MainActivity.this.isWIFIConneted = true;
                        MainActivity.this.mCameraSettingsBtn.setEnabled(true);
                        if (MainActivity.this.mCurrentModelType == 406) {
                            MainActivity.this.mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    ChannelDataForward.getInstance().bind();
                                }
                            }, 1000);
                        }
                        if (MainActivity.this.mConnectingDialog != null) {
                            if (MainActivity.this.isFSKConneted) {
                                MainActivity.this.mConnectingDialog.setMessage(MainActivity.this.getResources().getText(R.string.str_connect_all_completed));
                            } else {
                                MainActivity.this.mConnectingDialog.setMessage(MainActivity.this.getResources().getText(R.string.str_connect_wifi_completed));
                            }
                        }
                        MainActivity.this.dismissConnectingDialog();
                        disableWifiDelay();
                        if (Utilities.isValidWifi(context, MainActivity.this.mCurrentModelId)) {
                            MainActivity.this.mIPCameraManager.initCamera(MainActivity.this.mHttpResponseMessenger);
                            Log.i(MainActivity.TAG, "Valid wifi connected ,init Camera");
                        } else {
                            enableWifiDelay();
                        }
                        if (MainActivity.this.handlerThread == null) {
                            MainActivity.this.handlerThread = new HandlerThread("Heartbeat");
                            MainActivity.this.handlerThread.start();
                            this.heartBeatHandler = new HeartbeatEventHandler(MainActivity.this.handlerThread.getLooper());
                        }
                        if (this.heartBeatHandler != null) {
                            this.heartBeatHandler.sendEmptyMessage(1);
                        }
                    } else if (State.DISCONNECTED == state) {
                        enableWifiDelay();
                        MainActivity.this.onWiFiConntectionLost();
                        if (!MainActivity.this.isFSKConneted) {
                            MainActivity.this.mCameraSettingsBtn.setEnabled(false);
                        }
                        if (this.heartBeatHandler != null) {
                            this.heartBeatHandler.sendEmptyMessage(3);
                        }
                        if (MainActivity.this.handlerThread != null) {
                            MainActivity.this.handlerThread.quit();
                            MainActivity.this.handlerThread.interrupt();
                            MainActivity.this.handlerThread = null;
                        }
                    } else if (State.CONNECTING == state) {
                        disableWifiDelay();
                    }
                }
            }
        }

        private void enableWifiDelay() {
            if (MainActivity.this.mWifiEnabledRunnable != null && !MainActivity.this.mIsWifiDelayConnecting) {
                MainActivity.this.mIsWifiDelayConnecting = true;
                MainActivity.this.mHandler.removeCallbacks(MainActivity.this.mWifiEnabledRunnable);
                MainActivity.this.mHandler.postDelayed(MainActivity.this.mWifiEnabledRunnable, 300);
            }
        }

        private void disableWifiDelay() {
            if (MainActivity.this.mWifiEnabledRunnable != null && MainActivity.this.mIsWifiDelayConnecting) {
                MainActivity.this.mIsWifiDelayConnecting = false;
                MainActivity.this.mHandler.removeCallbacks(MainActivity.this.mWifiEnabledRunnable);
            }
        }
    };
    private OnSeekBarChangeListener seekBarListener = new OnSeekBarChangeListener() {
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.i(MainActivity.TAG, "****BAR****, STOP:" + seekBar.getProgress());
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.i(MainActivity.TAG, "****BAR****, START:" + seekBar.getProgress());
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.i(MainActivity.TAG, "****BAR****, CHG:" + progress);
            int tmp = progress + 1;
            if (MainActivity.this.mValueForZoom != tmp) {
                MainActivity.this.mHandler.removeCallbacks(MainActivity.this.mZoomRunnable);
                MainActivity.this.mValueForZoom = tmp;
                MainActivity.this.mHandler.postDelayed(MainActivity.this.mZoomRunnable, 200);
            }
        }
    };
    private ToggleButton sonarButton;
    private boolean validVehicle = false;
    private WbIsoChangedListener wbIsoChangedListener;
    private FrameLayout wbIsoFrame;
    private WifiManager wifiManager;
    private ToggleButton yawModeButton;

    class GestureListener extends SimpleOnGestureListener {
        GestureListener() {
        }

        public boolean onDoubleTap(MotionEvent e) {
            if (MainActivity.this.mMainscreenLcdFrame != null && MainActivity.this.mLCDDislpayView.getVisibility() == 0) {
                MarginLayoutParams mlp = (MarginLayoutParams) MainActivity.this.mMainscreenLcdFrame.getLayoutParams();
                int width = mlp.width;
                int height = mlp.height;
                if (width == MainActivity.this.initLcdWidth && height == MainActivity.this.initLcdHeight) {
                    if (MainActivity.this.mStatusBarView != null) {
                        MainActivity.this.mStatusBarView.setVisibility(8);
                    }
                    if (MainActivity.this.mCurrentCameraName.equals(MainActivity.CAMERA_TYPE_CGO3_PRO)) {
                        MainActivity.this.wbIsoFrame.setVisibility(8);
                    }
                    MainActivity.this.cameraControlCombine.setVisibility(8);
                    if (Utilities.PROJECT_TAG.equals("ST12")) {
                        MainActivity.this.mMissionView.setVisibility(8);
                    }
                    mlp.width = -1;
                    mlp.height = -1;
                    mlp.setMargins(0, 0, 0, 0);
                    MainActivity.this.mMainscreenLcdFrame.setLayoutParams(mlp);
                } else {
                    if (MainActivity.this.mStatusBarView != null) {
                        MainActivity.this.mStatusBarView.setVisibility(0);
                    }
                    if (MainActivity.this.mCurrentCameraName.equals(MainActivity.CAMERA_TYPE_CGO3_PRO) || MainActivity.this.mCurrentCameraName.equals("C-GO3")) {
                        MainActivity.this.mSRswitch.setVisibility(0);
                        if (MainActivity.this.mCurrentCameraName.equals(MainActivity.CAMERA_TYPE_CGO3_PRO)) {
                            MainActivity.this.wbIsoFrame.setVisibility(MainActivity.this.cameraControlCombineVisibility);
                        } else {
                            MainActivity.this.wbIsoFrame.setVisibility(8);
                        }
                    } else {
                        MainActivity.this.mSRswitch.setVisibility(8);
                        MainActivity.this.wbIsoFrame.setVisibility(8);
                    }
                    MainActivity.this.cameraControlCombine.setVisibility(MainActivity.this.cameraControlCombineVisibility);
                    if (Utilities.PROJECT_TAG.equals("ST12")) {
                        if (MainActivity.this.cameraControlCombineVisibility == 0) {
                            MainActivity.this.mMissionView.setVisibility(8);
                        } else {
                            MainActivity.this.mMissionView.setVisibility(0);
                        }
                    }
                    mlp.width = MainActivity.this.initLcdWidth;
                    mlp.height = MainActivity.this.initLcdHeight;
                    mlp.setMargins(0, MainActivity.this.initLcdTopMargin, 0, 0);
                    MainActivity.this.mMainscreenLcdFrame.setLayoutParams(mlp);
                }
            }
            return true;
        }

        public boolean onDown(MotionEvent e) {
            return true;
        }

        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (MainActivity.this.mStatusBarView.getVisibility() == 8) {
                return false;
            }
            if (MainActivity.this.mCurrentCameraName.equals(MainActivity.CAMERA_TYPE_CGO3_PRO)) {
                if (MainActivity.this.wbIsoFrame.getVisibility() == 0) {
                    MainActivity.this.wbIsoFrame.setVisibility(8);
                } else if (MainActivity.this.wbIsoFrame.getVisibility() == 8) {
                    MainActivity.this.wbIsoFrame.setVisibility(0);
                }
            }
            if (MainActivity.this.cameraControlCombine.getVisibility() == 0) {
                MainActivity.this.cameraControlCombine.setVisibility(8);
                MainActivity.this.cameraControlCombineVisibility = 8;
                if (Utilities.PROJECT_TAG.equals("ST12")) {
                    MainActivity.this.mMissionView.setVisibility(0);
                }
            } else if (MainActivity.this.cameraControlCombine.getVisibility() == 8) {
                MainActivity.this.cameraControlCombine.setVisibility(0);
                MainActivity.this.cameraControlCombineVisibility = 0;
                if (Utilities.PROJECT_TAG.equals("ST12")) {
                    MainActivity.this.mMissionView.setVisibility(8);
                }
            }
            return true;
        }
    }

    private class WifiThread extends Thread {
        private WifiManager mWifi;

        public WifiThread(WifiManager mWifi) {
            this.mWifi = mWifi;
            setName("Wifi State");
        }

        public void run() {
            while (true) {
                try {
                    String ssid;
                    String rssi;
                    String speed;
                    String state;
                    WifiInfo connectionInfo = this.mWifi.getConnectionInfo();
                    if (connectionInfo != null) {
                        ssid = connectionInfo.getSSID();
                        rssi = String.valueOf(connectionInfo.getRssi());
                        speed = String.valueOf(connectionInfo.getLinkSpeed());
                        state = connectionInfo.getSupplicantState().name();
                    } else {
                        ssid = "null";
                        rssi = "null";
                        speed = " null";
                        state = IPCameraManager.HTTP_RESPONSE_CODE_UNKNOWN;
                    }
                    MainActivity.this.mHandler.post(new Runnable() {
                        public void run() {
                            MainActivity.this.mSSID.setText(ssid);
                            MainActivity.this.mRssi.setText("RSSI: " + rssi);
                            MainActivity.this.mLinkSpeed.setText("Speed: " + speed + " Mbps");
                            MainActivity.this.mWifiState.setText("State: " + state);
                        }
                    });
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    MainActivity.this.mHandler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this, "WifiThread stopped", 0).show();
                        }
                    });
                    return;
                }
            }
        }
    }

    private class HttpRequestHandler extends WeakHandler<MainActivity> {
        public HttpRequestHandler(MainActivity owner) {
            super(owner);
        }

        public void handleMessage(Message msg) {
            MainActivity activity = (MainActivity) getOwner();
            if (activity != null) {
                if (activity.mIsPaused) {
                    Log.d(MainActivity.TAG, "the activity is paused");
                    return;
                }
                switch (msg.what) {
                    case 1:
                        switch (msg.arg1) {
                            case 0:
                            case 1:
                                return;
                            case 2:
                                Log.i(MainActivity.TAG, "#### Rec Start=" + msg.obj);
                                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(msg.obj)) {
                                    activity.mHasStartedRecord = true;
                                    activity.mIsRecording = true;
                                    activity.mBtnRecord.setChecked(true);
                                    activity.mRecTime.setVisibility(0);
                                    activity.mRecTime.setStartTime(0);
                                    activity.mRecTime.start();
                                    if (activity.mCurrentCameraName.equals(MainActivity.CAMERA_TYPE_CGO3_PRO) || activity.mCurrentCameraName.equals("C-GO3")) {
                                        activity.mSRswitch.setVisibility(4);
                                    }
                                } else {
                                    activity.mIsRecording = false;
                                    activity.mBtnRecord.setChecked(false);
                                    activity.mStatusBarView.setInfoText(activity.getResources().getString(R.string.hint_start_record_failed), -65536);
                                    activity.mHasStoppedRecord = false;
                                }
                                activity.mRecordProcessing = false;
                                activity.stopWaitingAnimation(activity.mBtnRecord);
                                return;
                            case 3:
                                Log.i(MainActivity.TAG, "#### Rec Stop=" + msg.obj);
                                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(msg.obj)) {
                                    activity.mHasStoppedRecord = true;
                                    activity.mIsRecording = false;
                                    activity.mBtnRecord.setChecked(false);
                                    activity.mRecTime.setVisibility(4);
                                    activity.mRecTime.stop();
                                    if (activity.mCurrentCameraName.equals(MainActivity.CAMERA_TYPE_CGO3_PRO) || activity.mCurrentCameraName.equals("C-GO3")) {
                                        activity.mSRswitch.setVisibility(0);
                                    }
                                } else {
                                    activity.mIsRecording = true;
                                    activity.mBtnRecord.setChecked(true);
                                    activity.mStatusBarView.setInfoText(activity.getResources().getString(R.string.hint_stop_record_failed), -65536);
                                    activity.mHasStartedRecord = false;
                                }
                                activity.mRecordProcessing = false;
                                activity.stopWaitingAnimation(activity.mBtnRecord);
                                return;
                            case 4:
                                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(msg.obj)) {
                                    activity.mStatusBarView.setInfoText(activity.getResources().getString(R.string.hint_capture_complete), 0);
                                } else {
                                    activity.mStatusBarView.setInfoText(activity.getResources().getString(R.string.hint_capture_failed), -65536);
                                }
                                activity.mSnapShotProcessing = false;
                                activity.stopWaitingAnimation(activity.mBtnSnapshot);
                                return;
                            case 10:
                                if (msg.obj instanceof SDCardStatus) {
                                    SDCardStatus sd = msg.obj;
                                    activity.dismissCommunicatingDialog();
                                    activity.onSDcardChanged(sd);
                                    return;
                                } else if (IPCameraManager.HTTP_RESPONSE_CODE_INTERNAL_ERROR.equals(msg.obj)) {
                                    Log.i(MainActivity.TAG, "get sdcard status error, try again");
                                    if (activity.mIPCameraManager != null) {
                                        activity.mIPCameraManager.getSDCardStatus(activity.mHttpResponseMessenger);
                                        return;
                                    }
                                    return;
                                } else {
                                    Log.i(MainActivity.TAG, "request sdcard status error:" + msg.obj);
                                    return;
                                }
                            case 23:
                                if (msg.obj instanceof String) {
                                    Log.i(MainActivity.TAG, "camera version is " + msg.obj);
                                    return;
                                }
                                return;
                            case 24:
                                if (msg.obj instanceof String) {
                                    if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(String.valueOf(msg.obj))) {
                                        Log.i(MainActivity.TAG, "Init camera complete");
                                        activity.mIPCameraManager.syncTime(activity.mHttpResponseMessenger);
                                        activity.refreshScreen();
                                        if (activity.mIsPlayFPV) {
                                            activity.playFPV();
                                            return;
                                        }
                                        return;
                                    }
                                    activity.mStatusBarView.setInfoText(activity.getResources().getString(R.string.init_camera_failed), 0);
                                    return;
                                } else if (msg.obj instanceof CameraParams) {
                                    CameraParams cps = msg.obj;
                                    if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(cps.response)) {
                                        Log.i(MainActivity.TAG, "Init camera complete");
                                        activity.mCameraParams = cps;
                                        activity.mIPCameraManager.syncTime(activity.mHttpResponseMessenger);
                                        activity.refreshScreen();
                                        if (activity.mIsPlayFPV) {
                                            activity.playFPV();
                                            return;
                                        }
                                        return;
                                    }
                                    activity.mStatusBarView.setInfoText(activity.getResources().getString(R.string.init_camera_failed), 0);
                                    return;
                                } else {
                                    return;
                                }
                            case IPCameraManager.REQUEST_GET_REC_TIME /*25*/:
                                Log.i(MainActivity.TAG, "#### Rec REQUEST_GET_REC_TIME=" + msg.obj);
                                if (msg.obj instanceof RecordTime) {
                                    RecordTime rt = msg.obj;
                                    Log.d(MainActivity.TAG, "#### Rec get record time: " + rt.recTime);
                                    activity.mRecTime.setVisibility(0);
                                    activity.mRecTime.setStartTime(rt.recTime * 1000);
                                    activity.mRecTime.start();
                                    if (activity.mCurrentCameraName.equals(MainActivity.CAMERA_TYPE_CGO3_PRO) || activity.mCurrentCameraName.equals("C-GO3")) {
                                        activity.mSRswitch.setVisibility(4);
                                        return;
                                    }
                                    return;
                                }
                                return;
                            case IPCameraManager.REQUEST_REST_VF /*26*/:
                                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(String.valueOf(msg.obj))) {
                                    activity.playFPV();
                                }
                                activity.dismissProgressDialog(null);
                                return;
                            case IPCameraManager.REQUEST_STOP_VF /*27*/:
                                if (!IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(String.valueOf(msg.obj))) {
                                    activity.refreshScreen();
                                } else if (activity.mRlnBtnPressed) {
                                    activity.mRlnBtnPressed = false;
                                }
                                activity.dismissProgressDialog(null);
                                return;
                            case IPCameraManager.REQUEST_SET_VIDEO_RESOLUTION /*31*/:
                                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(String.valueOf(msg.obj))) {
                                    activity.setRlnBtnText(activity.mResolution);
                                } else {
                                    Log.e(MainActivity.TAG, "Failed to set resolution");
                                    activity.dismissProgressDialog(activity.mRlnProgressRunnable);
                                    activity.mStatusBarView.setInfoText(activity.getResources().getString(R.string.set_rln_failed), -65536);
                                }
                                activity.refreshScreen();
                                return;
                            case 32:
                                if (msg.obj instanceof String) {
                                    activity.setRlnBtnText((String) msg.obj);
                                    return;
                                }
                                return;
                            case IPCameraManager.REQUEST_SET_VIDEO_STANDARD /*33*/:
                                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(String.valueOf(msg.obj))) {
                                    activity.setVideoResolution(activity.mResolution);
                                    return;
                                }
                                Log.e(MainActivity.TAG, "Failed to set standard");
                                activity.dismissProgressDialog(activity.mRlnProgressRunnable);
                                activity.mStatusBarView.setInfoText(activity.getResources().getString(R.string.set_rln_failed), -65536);
                                return;
                            case IPCameraManager.REQUEST_GET_AE_ENABLE /*48*/:
                                if (msg.obj instanceof Integer) {
                                    activity.mCameraParams.ae_enable = ((Integer) msg.obj).intValue();
                                    return;
                                }
                                return;
                            case IPCameraManager.REQUEST_SET_SH_TM_ISO /*49*/:
                            case IPCameraManager.REQUEST_SET_WHITEBLANCE_MODE /*53*/:
                            case IPCameraManager.REQUEST_SET_EXPOSURE_VALUE /*55*/:
                                if (IPCameraManager.HTTP_RESPONSE_CODE_OK.equals(String.valueOf(msg.obj))) {
                                    activity.dismissCommunicatingDialog();
                                    return;
                                } else {
                                    Log.e(MainActivity.TAG, "Failed to communicate camera");
                                    return;
                                }
                            case IPCameraManager.REQUEST_GET_SH_TM_ISO /*50*/:
                                if (msg.obj instanceof ShutterTimeISO) {
                                    ShutterTimeISO timeIso = msg.obj;
                                    activity.mCameraParams.iso = timeIso.iso;
                                    activity.mCameraParams.shutter_time = timeIso.time;
                                    return;
                                }
                                return;
                            case IPCameraManager.REQUEST_GET_WHITEBALANCE_MODE /*54*/:
                                if (msg.obj instanceof Integer) {
                                    activity.mCameraParams.white_balance = ((Integer) msg.obj).intValue();
                                    return;
                                }
                                return;
                            case IPCameraManager.REQUEST_GET_EXPOSURE_VALUE /*56*/:
                                if (msg.obj instanceof String) {
                                    activity.mCameraParams.exposure_value = (String) msg.obj;
                                    return;
                                }
                                return;
                            case IPCameraManager.REQUEST_GET_CAMERA_MODE /*62*/:
                                if (msg.obj instanceof Integer) {
                                    int mode = ((Integer) msg.obj).intValue();
                                    if (mode == 1 && activity.isChangeAndStartRecord) {
                                        activity.mBtnRecord.performClick();
                                        activity.isChangeAndStartRecord = false;
                                    }
                                    activity.mCameraParams.cam_mode = mode;
                                    Log.d(MainActivity.TAG, "CameraMode--get camera mode: " + mode);
                                    return;
                                }
                                return;
                            default:
                                return;
                        }
                    default:
                        return;
                }
            }
        }
    }

    static {
        mWBList.add(0, Integer.valueOf(0));
        mWBList.add(1, Integer.valueOf(99));
        mWBList.add(2, Integer.valueOf(4));
        mWBList.add(3, Integer.valueOf(5));
        mWBList.add(4, Integer.valueOf(7));
        mWBList.add(5, Integer.valueOf(1));
        mWBList.add(6, Integer.valueOf(3));
    }

    private void setRecordBtnState(boolean on) {
        Log.i(TAG, "setRecordBtnState:" + on);
        if (on) {
            this.mIsRecording = true;
            this.mBtnRecord.setChecked(true);
            this.mRecTime.setVisibility(0);
            this.mRlnButton.setEnabled(false);
            if (this.mCurrentCameraName.equals(CAMERA_TYPE_CGO3_PRO) || this.mCurrentCameraName.equals("C-GO3")) {
                this.mSRswitch.setVisibility(4);
                return;
            }
            return;
        }
        this.mIsRecording = false;
        this.mBtnRecord.setChecked(false);
        this.mRecTime.setVisibility(4);
        this.mRlnButton.setEnabled(true);
        if (this.mCurrentCameraName.equals(CAMERA_TYPE_CGO3_PRO) || this.mCurrentCameraName.equals("C-GO3")) {
            this.mSRswitch.setVisibility(0);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utilities.killBackgroundApps(this);
        getWindow().addFlags(Utilities.FLAG_HOMEKEY_DISPATCHED);
        getWindow().addFlags(128);
        Utilities.setCurrentMode(this, 1);
        setContentView(R.layout.mainscreen_layout);
        mIndex = 0;
        for (int i = 0; i < VOL_INDEX_MAX; i++) {
            mVOLValue[i] = 0.0f;
        }
        this.mIntentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mIntentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.wifiManager = (WifiManager) getSystemService("wifi");
        setupViews();
        loadAnimation();
        Utilities.checkFirstStart(this);
        preparePlayer();
        prepareVideoFolder();
        prepareSoundPool();
        setupDrawerLayout();
        setVolumeControlStream(3);
        this.mPref = getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0);
        this.mCurrentModelId = this.mPref.getLong("current_model_id", -2);
        this.isMasterControl = this.mPref.getInt(FlightSettings.MASTER_UNIT, 2);
        this.mController = UARTController.getInstance();
        if (this.mController == null) {
            CharSequence package_name = UARTController.getPackagenameByProcess(this);
            Builder ab = new Builder(this);
            if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG)) {
                this.mGuardDialog = ab.setTitle(R.string.peripheral_occupied_title_st10).setMessage(getResources().getString(R.string.peripheral_occupied_message_st10, new Object[]{package_name})).setNeutralButton(17039370, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MainActivity.this.finish();
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        MainActivity.this.finish();
                    }
                }).create();
            } else if (Utilities.PROJECT_TAG.equals("ST12")) {
                this.mGuardDialog = ab.setTitle(R.string.peripheral_occupied_title_st12).setMessage(getResources().getString(R.string.peripheral_occupied_message_st12, new Object[]{package_name})).setNeutralButton(17039370, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MainActivity.this.finish();
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        MainActivity.this.finish();
                    }
                }).create();
            } else if (Utilities.PROJECT_TAG.equals("ST15")) {
                this.mGuardDialog = ab.setTitle(R.string.peripheral_occupied_title).setMessage(getResources().getString(R.string.peripheral_occupied_message, new Object[]{package_name})).setNeutralButton(17039370, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MainActivity.this.finish();
                    }
                }).setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        MainActivity.this.finish();
                    }
                }).create();
            }
            this.mGuardDialog.show();
            return;
        }
        if (this.mController != null) {
            this.mController.registerReaderHandler(this.mUartHandler);
            this.mController.startReading();
            this.mController.setBindKeyFunction(true, 1, 3);
        }
        this.mGPSUpdater = new GPSUpdater(this);
        this.mCompassUpdater = new CompassUpdater(this);
        this.mVibrator = (Vibrator) getSystemService("vibrator");
        this.mGesture = new GestureDetector(this, new GestureListener());
        this.mInitiailized = true;
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mRTVPlayer.deinit();
        if (this.mController != null) {
            this.mController.startReading();
            this.mController.setBindKeyFunction(true, 0, 3);
            this.mController.clearRadioInfo();
            if (!Utilities.ensureAwaitState(this.mController)) {
                Log.e(TAG, "fail to change to await");
            }
            this.mController.destory();
            this.mController = null;
        }
        this.mTimerHelper.releaseResource();
        this.mSoundPool.release();
        this.mSoundPool = null;
        this.mInitiailDataTranfered = false;
        this.mInitiailized = false;
        this.mConnectingDialog = null;
        this.mGPSDisabledWarningDialog = null;
        this.mVoltageLowWarning1Dialog = null;
        this.mVoltageLowWarning2Dialog = null;
        this.mAirportWarningDialog = null;
        this.mAltitudeWarningDialog = null;
        this.mRlnBtnPressed = false;
        this.flightLog.closedFlightNote(true);
        ChannelDataForward.getInstance().exit();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public void onClick(View v) {
        final TwoButtonPopDialog dialog = new TwoButtonPopDialog(this);
        if (v.equals(this.mBtnModelSelect)) {
            if ((isBindRX(this, this.mCurrentModelId) || isBindWifi(this, this.mCurrentModelId)) && this.mFlightToggle.isChecked()) {
                dialog.adjustHeight(380);
                dialog.setTitle((int) R.string.str_exit_warning_title);
                dialog.setMessage((int) R.string.str_exit_warning_message);
                dialog.setPositiveButton(17039379, new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        MainActivity.this.mFlightToggle.setChecked(false);
                        MainActivity.this.startActivity(new Intent(MainActivity.this, ModelSelectMain.class));
                    }
                });
                dialog.setNegativeButton(17039369, new OnClickListener() {
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                dialog.show();
                return;
            }
            this.mFlightToggle.setChecked(false);
            startActivity(new Intent(this, ModelSelectMain.class));
        } else if (v.equals(this.mBtnFlightSetting)) {
            if ((isBindRX(this, this.mCurrentModelId) || isBindWifi(this, this.mCurrentModelId)) && this.mFlightToggle.isChecked()) {
                dialog.adjustHeight(380);
                dialog.setTitle((int) R.string.str_exit_warning_title);
                dialog.setMessage((int) R.string.str_exit_warning_message);
                dialog.setPositiveButton(17039379, new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        MainActivity.this.mFlightToggle.setChecked(false);
                        MainActivity.this.startActivity(new Intent(MainActivity.this, FlightSettings.class));
                    }
                });
                dialog.setNegativeButton(17039369, new OnClickListener() {
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                dialog.show();
                return;
            }
            this.mFlightToggle.setChecked(false);
            startActivity(new Intent(this, FlightSettings.class));
        } else if (v.equals(this.mBtnSystemSetting)) {
            if ((isBindRX(this, this.mCurrentModelId) || isBindWifi(this, this.mCurrentModelId)) && this.mFlightToggle.isChecked()) {
                dialog.adjustHeight(380);
                dialog.setTitle((int) R.string.str_exit_warning_title);
                dialog.setMessage((int) R.string.str_exit_warning_message);
                dialog.setPositiveButton(17039379, new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        MainActivity.this.mFlightToggle.setChecked(false);
                        MainActivity.this.startActivity(new Intent("android.settings.SETTINGS"));
                    }
                });
                dialog.setNegativeButton(17039369, new OnClickListener() {
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                dialog.show();
                return;
            }
            this.mFlightToggle.setChecked(false);
            startActivity(new Intent("android.settings.SETTINGS"));
        } else if (v.equals(this.mBtnSnapshot)) {
            startWaitingAnimation(v);
            this.mIPCameraManager.snapShot(prepareFilename(), this.mHttpResponseMessenger, this.mCameraInfo);
            this.mStatusBarView.setInfoText(getResources().getString(R.string.hint_start_capture), 0);
        } else if (!v.equals(this.mFmodeButton)) {
        } else {
            if (this.mVehicleType == 4) {
                showSpeciallyChannelSender();
            } else if ((this.mVehicleType == 2 || this.mVehicleType == 5) && this.mController != null) {
                if (this.mFmodeBtnChecked) {
                    this.mController.setTTBState(false, Utilities.HW_VB_BASE + 8, false);
                    this.mFmodeBtnChecked = false;
                } else {
                    this.mController.setTTBState(false, Utilities.HW_VB_BASE + 9, false);
                    this.mFmodeBtnChecked = true;
                }
                setFmodeButtonChecked(this.mFmodeBtnChecked);
            }
        }
    }

    private void showSpeciallyChannelSender() {
        final SpeciallyChannelSender chSender = new SpeciallyChannelSender(this);
        chSender.setOnButtonClicked(new onButtonClickListener() {
            public void onButtonClick(int btnNo) {
                if (MainActivity.this.mController != null) {
                    MainActivity.this.mController.setTTBState(false, (Utilities.HW_VB_BASE + btnNo) + 1, false);
                }
                chSender.dismiss();
            }
        });
        chSender.show();
    }

    private boolean checkIsFPVModel() {
        if (Utilities.isFPVModel(this, this.mCurrentModelId) == 1) {
            return true;
        }
        return false;
    }

    private void setupViews() {
        this.mMainscreenLcdFrame = (RelativeLayout) findViewById(R.id.wholeScreen);
        MarginLayoutParams mlp = (MarginLayoutParams) this.mMainscreenLcdFrame.getLayoutParams();
        this.initLcdWidth = mlp.width;
        this.initLcdHeight = mlp.height;
        this.initLcdTopMargin = mlp.topMargin;
        this.mBtnSystemSetting = (Button) findViewById(R.id.system_setting);
        this.mBtnSystemSetting.setOnClickListener(this);
        this.mBtnFlightSetting = (Button) findViewById(R.id.flight_setting);
        this.mBtnFlightSetting.setOnClickListener(this);
        this.mBtnModelSelect = (Button) findViewById(R.id.model_select);
        this.mBtnModelSelect.setOnClickListener(this);
        this.mLCDDislpayView = (SurfaceView) findViewById(R.id.lcd_display);
        this.mZoombarContainer = findViewById(R.id.fpv_zoombar_frm);
        this.mZoombar = (VerticalSeekBar) findViewById(R.id.fpv_zoombar);
        this.mZoombar.setOnSeekBarChangeListener(this.seekBarListener);
        this.mZoombar.setEnabled(true);
        this.mZoombar.setMax(17);
        this.mBtnSnapshot = (Button) findViewById(R.id.fpv_snapshot);
        this.mBtnSnapshot.setOnClickListener(this);
        this.mBtnRecord = (ToggleButton) findViewById(R.id.fpv_record);
        this.mBtnRecord.setOnCheckedChangeListener(this.mRecordStateListener);
        this.mRecTime = (CounterView) findViewById(R.id.fpc_rec_time);
        this.mRecTime.setDuration(3600);
        this.mRecTime.setStyle(1);
        this.mRecTime.setTickListener(this.mRecTickListener);
        this.mBtnSnapshot.setEnabled(false);
        this.mBtnRecord.setEnabled(false);
        this.mFlightToggle = (ToggleButtonWithColorText) findViewById(R.id.takeoff_landing);
        this.mFlightToggle.setOnCheckedChangeListener(this.mFlightToggleListener);
        this.mFlightToggle.setOnTouchListener(this.mFlightToggleTouchListener);
        this.mOSDTime = (CounterView) findViewById(R.id.osd_time);
        this.mTimerHelper = new TimerHelper(this.mOSDTime);
        this.mTimerHelper.setupDefault();
        this.mOSDTime.setVisibility(8);
        this.mHomeCompassView = (HomeCompassView) findViewById(R.id.homeCompassView);
        this.mLeftTrimView = (LeftTrimView) findViewById(R.id.left_trim_view);
        this.mRightTrimView = (RightTrimView) findViewById(R.id.right_trim_view);
        this.mLeftTrimView.setVisibility(8);
        this.mRightTrimView.setVisibility(8);
        this.mIndicatorMODE = (IndicatorView) findViewById(R.id.indicator_mode);
        this.mIndicatorGPSStatus = (IndicatorView) findViewById(R.id.indicator_gps_status);
        this.mIndicatorGPS = (IndicatorView) findViewById(R.id.indicator_gps);
        this.mIndicatorVOL = (IndicatorView) findViewById(R.id.indicator_vol);
        this.mIndicatorALT = (IndicatorView) findViewById(R.id.indicator_alt);
        this.mIndicatorTAS = (IndicatorView) findViewById(R.id.indicator_tas);
        this.mIndicatorPOS = (IndicatorView) findViewById(R.id.indicator_pos);
        this.mIndicatorDIS = (IndicatorView) findViewById(R.id.indicator_dis);
        this.mStatusBarView = (StatusbarView) findViewById(R.id.statusbar);
        this.mSSID = (TextView) findViewById(R.id.ssid);
        this.mRssi = (TextView) findViewById(R.id.rssi);
        this.mLinkSpeed = (TextView) findViewById(R.id.speed);
        this.mWifiState = (TextView) findViewById(R.id.state);
        this.mFmodeCH5 = (TextView) findViewById(R.id.fmode_ch5);
        this.mFmodeCH6 = (TextView) findViewById(R.id.fmode_ch6);
        this.mRlnButton = (Button) findViewById(R.id.rln_btn);
        this.mRlnButton.setOnClickListener(this);
        this.mRlnButton.setEnabled(false);
        this.mRlnText = (TextView) findViewById(R.id.resolution_txt);
        this.mFmodeButton = (Button) findViewById(R.id.fmode_btn);
        this.mFmodeButton.setOnClickListener(this);
        this.mFmodeButton.setVisibility(0);
        setFmodeButtonEnable(false);
        if (Utilities.PROJECT_TAG.equals("ST12")) {
            this.sonarButton = (ToggleButton) findViewById(R.id.sonar_switch);
            this.yawModeButton = (ToggleButton) findViewById(R.id.yawmode_btn);
            this.yawModeButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (MainActivity.this.mController != null) {
                        MainActivity.this.mController.setTTBState(false, Utilities.HW_VS_BASE + 1, !isChecked);
                    }
                }
            });
            this.sonarButton = (ToggleButton) findViewById(R.id.sonar_switch);
            this.sonarButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    UARTController.getInstance().sonarSwitch(MainActivity.this.sonarButton.isChecked());
                }
            });
            this.mMissionView = (MissionView) findViewById(R.id.mission_view);
            this.mMissionView.setMissionViewCallback(new MissionViewCallback() {
                public void updateError(int resourceId) {
                    MainActivity.this.mStatusBarView.setInfoText(MainActivity.this.getResources().getString(resourceId), -65536);
                }

                public GPSUpLinkData getControllerGps() {
                    if (MainActivity.this.mGPSUpdater != null) {
                        return MainActivity.this.mGPSUpdater.getCurrentLocation();
                    }
                    return null;
                }

                public void updateStep(MissionState state) {
                }
            });
            return;
        }
        this.sonarButton = (ToggleButton) findViewById(R.id.sonar_switch);
        this.sonarButton.setVisibility(4);
        this.yawModeButton = (ToggleButton) findViewById(R.id.yawmode_btn);
        this.yawModeButton.setVisibility(4);
    }

    private void setFmodeButtonEnable(boolean enabled) {
        this.mFmodeButton.setEnabled(enabled);
        if (!enabled) {
            this.mFmodeBtnChecked = false;
            this.mFmodeButton.getBackground().setLevel(0);
        } else if (this.mFmodeBtnChecked) {
            this.mFmodeButton.getBackground().setLevel(2);
        } else {
            this.mFmodeButton.getBackground().setLevel(1);
        }
    }

    private void setFmodeButtonChecked(boolean isTracking) {
        this.mFmodeButton.setEnabled(true);
        if (isTracking) {
            this.mFmodeButton.getBackground().setLevel(2);
            this.mFmodeBtnChecked = true;
            return;
        }
        this.mFmodeButton.getBackground().setLevel(1);
        this.mFmodeBtnChecked = false;
    }

    private void initIndicatorValue() {
        if (this.mIndicatorMODE != null) {
            this.mIndicatorMODE.setValueText(mInvalidValueString);
            this.mIndicatorMODE.setValueColor(DEFAULT_COLOR);
        }
        if (this.mIndicatorGPSStatus != null) {
            this.mIndicatorGPSStatus.setValueText(mInvalidValueString);
            this.mIndicatorGPSStatus.setValueColor(DEFAULT_COLOR);
            setFmodeButtonEnable(false);
        }
        if (this.mIndicatorGPS != null) {
            this.mIndicatorGPS.setValueText(mInvalidValueString);
        }
        if (this.mIndicatorVOL != null) {
            this.mIndicatorVOL.setValueText(getResources().getString(R.string.str_value, new Object[]{mInvalidValueString, " V"}));
            this.mIndicatorVOL.setValueTextSize(10.0f);
            this.mIndicatorVOL.setValueTextDrawable(getResources().getDrawable(R.drawable.osd_battery));
            this.mIndicatorVOL.setValueColor(DEFAULT_COLOR);
            updateVolDrawable(0);
        }
        if (this.mIndicatorALT != null) {
            this.mIndicatorALT.setValueText(getResources().getString(R.string.str_value, new Object[]{mInvalidValueString, Utilities.getDisplayLengthUnit(this)}));
        }
        if (this.mIndicatorTAS != null) {
            this.mIndicatorTAS.setValueText(getResources().getString(R.string.str_value, new Object[]{mInvalidValueString, Utilities.getDisplayVelocityUnit(this)}));
        }
        if (this.mIndicatorPOS != null) {
            this.mIndicatorPOS.setValueText(new StringBuilder(String.valueOf(getResources().getString(R.string.str_value, new Object[]{mInvalidValueString, "E"}))).append("\n").append(getResources().getString(R.string.str_value, new Object[]{mInvalidValueString, "N"})).toString());
            this.mIndicatorPOS.setValueTextSize(10.0f);
        }
        if (this.mIndicatorDIS != null) {
            this.mIndicatorDIS.setValueText(getResources().getString(R.string.str_value, new Object[]{mInvalidValueString, Utilities.getDisplayLengthUnit(this)}));
        }
    }

    private void loadAnimation() {
        this.mLeftFadeIn = AnimationUtils.loadAnimation(this, R.anim.left_fade_in);
        this.mLeftFadeIn.setAnimationListener(this);
        this.mLeftFadeOut = AnimationUtils.loadAnimation(this, R.anim.left_fade_out);
        this.mLeftFadeOut.setAnimationListener(this);
        this.mRightFadeIn = AnimationUtils.loadAnimation(this, R.anim.right_fade_in);
        this.mRightFadeIn.setAnimationListener(this);
        this.mRightFadeOut = AnimationUtils.loadAnimation(this, R.anim.right_fade_out);
        this.mRightFadeOut.setAnimationListener(this);
        this.mTrimLeftFadeIn = AnimationUtils.loadAnimation(this, R.anim.left_fade_in);
        this.mTrimLeftFadeIn.setAnimationListener(this);
        this.mTrimLeftFadeOut = AnimationUtils.loadAnimation(this, R.anim.left_fade_out);
        this.mTrimLeftFadeOut.setAnimationListener(this);
        this.mTrimRightFadeIn = AnimationUtils.loadAnimation(this, R.anim.right_fade_in);
        this.mTrimRightFadeIn.setAnimationListener(this);
        this.mTrimRightFadeOut = AnimationUtils.loadAnimation(this, R.anim.right_fade_out);
        this.mTrimRightFadeOut.setAnimationListener(this);
        this.mTopFadeIn = AnimationUtils.loadAnimation(this, R.anim.top_fade_in);
        this.mTopFadeIn.setAnimationListener(this);
        this.mTopFadeOut = AnimationUtils.loadAnimation(this, R.anim.top_fade_out);
        this.mTopFadeOut.setAnimationListener(this);
        this.mBottomFadeIn = AnimationUtils.loadAnimation(this, R.anim.bottom_fade_in);
        this.mBottomFadeIn.setAnimationListener(this);
        this.mBottomFadeOut = AnimationUtils.loadAnimation(this, R.anim.bottom_fade_out);
        this.mBottomFadeOut.setAnimationListener(this);
    }

    protected void onResume() {
        super.onResume();
        this.flightLog = FlightLog.getInstance();
        Utilities.setStatusBarLeftText(this, this.mStatusBarView);
        initIndicatorValue();
        BindWifiManage bwm = new BindWifiManage(this.wifiManager);
        this.mLCDDislpayView.setVisibility(4);
        if (Utilities.isWIFIConnected) {
            this.isWIFIConneted = true;
            this.mCameraSettingsBtn.setEnabled(true);
            this.mLCDDislpayView.setVisibility(0);
        }
        this.mCurrentModelId = this.mPref.getLong("current_model_id", -2);
        this.mCurrentCameraName = this.mPref.getString(FlightSettings.CAMERA_CURRENT_SELECTED, getResources().getString(R.string.def_camera_name));
        this.isMasterControl = this.mPref.getInt(FlightSettings.MASTER_UNIT, 2);
        if (this.mCurrentCameraName.equals(CAMERA_TYPE_CGO3_PRO) || this.mCurrentCameraName.equals("C-GO3")) {
            this.mSRswitch.setVisibility(0);
            if (this.mCurrentCameraName.equals(CAMERA_TYPE_CGO3_PRO)) {
                this.wbIsoFrame.setVisibility(0);
            } else {
                this.wbIsoFrame.setVisibility(8);
            }
        } else {
            this.wbIsoFrame.setVisibility(8);
            this.mSRswitch.setVisibility(8);
        }
        this.cameraControlCombine.setVisibility(0);
        this.cameraControlCombineVisibility = 0;
        if (Utilities.PROJECT_TAG.equals("ST12")) {
            if (this.cameraControlCombineVisibility == 0) {
                this.mMissionView.setVisibility(8);
            } else {
                this.mMissionView.setVisibility(0);
            }
        }
        if (this.mCurrentModelId != -2) {
            if (!isBindWifi(this, this.mCurrentModelId)) {
                this.wifiManager.disableNetwork(bwm.getCurrentNetId());
            }
            if (isBindWifi(this, this.mCurrentModelId) || isBindRX(this, this.mCurrentModelId)) {
                showConnectingDialog();
                connectWifi();
                Utilities.setRunningMode(true);
                this.mIsPlayFPV = true;
            }
        }
        if (this.mController != null) {
            this.mController.registerReaderHandler(this.mUartHandler);
            this.mController.startReading();
            if (this.mInitiailDataTranfered) {
                this.mPostSendCommand = false;
            } else {
                this.mPostSendCommand = true;
            }
            if (checkIsFPVModel()) {
                enableButtonBar(false);
                this.mBtnFlightSetting.setEnabled(false);
                this.mFlightToggle.setEnabled(false);
                this.mStatusBarView.setInfoText(getResources().getString(R.string.hint_initializing_mixing_data), 0);
                new SyncModelDataTask(this, this.mController, new SyncModelDataCompletedAction() {
                    public void SyncModelDataCompleted() {
                        MainActivity.this.enableButtonBar(true);
                        MainActivity.this.mBtnFlightSetting.setEnabled(true);
                        MainActivity.this.mFlightToggle.setEnabled(true);
                        if (!(MainActivity.this.mCurrentModelId == -2 || Utilities.getCurrentRxAddrFromDB(MainActivity.this, MainActivity.this.mCurrentModelId) == null)) {
                            MainActivity.this.mIsPlayFPV = true;
                            MainActivity.this.mFlightToggle.setChecked(true);
                            MainActivity.this.enableButtonBar(false);
                        }
                        if (MainActivity.this.mCurrentModelType == 406) {
                            MainActivity.this.mController.enterSim(true);
                            Utilities.ensureSimState(MainActivity.this.mController);
                        }
                        MainActivity.this.mInitiailDataTranfered = true;
                        Log.i(MainActivity.TAG, "Data Sync Completed");
                        MainActivity.this.mController.receiveBothChannel(true, 3);
                    }
                }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Long[]{Long.valueOf(this.mCurrentModelId)});
            } else {
                this.mInitiailDataTranfered = true;
            }
            Utilities.showFmodeState(this, this.mCurrentModelId, this.mController, this.mStatusBarView);
        }
        this.mSSID.setVisibility(4);
        this.mRssi.setVisibility(4);
        this.mLinkSpeed.setVisibility(4);
        this.mWifiState.setVisibility(4);
        this.mFmodeCH5.setVisibility(8);
        this.mFmodeCH6.setVisibility(8);
        registerReceiver(this.receiverWifi, this.mIntentFilter);
        this.mCameraDaemon = new CameraDaemon(this.mCameraDaemonHandler);
        this.mCameraDaemon.start(this);
        this.mIsPaused = false;
    }

    protected void onStart() {
        super.onStart();
        int camera_type = this.mPref.getInt(FlightSettings.CAMERA_TYPE_VALUE, getResources().getInteger(R.integer.def_camera_type_value));
        this.mCurrentModelType = this.mPref.getLong("current_model_type", (long) getResources().getInteger(R.integer.def_mode_type_value));
        if ((camera_type & 1) == 1) {
            this.mIPCameraManager = IPCameraManager.getIPCameraManager(this, 101);
            this.mCurrentVideoLocation = VIDEO_LOCATION2;
        } else if ((camera_type & 4) == 4) {
            this.mIPCameraManager = IPCameraManager.getIPCameraManager(this, 102);
            this.mCurrentVideoLocation = VIDEO_LOCATION3;
        } else if ((camera_type & 8) == 8 || (camera_type & 32) == 32) {
            this.mIPCameraManager = IPCameraManager.getIPCameraManager(this, 104);
            this.mCurrentVideoLocation = VIDEO_LOCATION3;
        } else if ((camera_type & 16) == 16) {
            this.mIPCameraManager = IPCameraManager.getIPCameraManager(this, 105);
            this.mCurrentVideoLocation = VIDEO_LOCATION4;
        } else {
            this.mIPCameraManager = IPCameraManager.getIPCameraManager(this, 100);
            this.mCurrentVideoLocation = VIDEO_LOCATION;
            this.mRlnButton.setVisibility(4);
        }
        this.isCheckedBindState = false;
        ChannelDataForward.getInstance().setCallback(new ForwardCallback() {
            public void onBindResult(boolean isOk) {
                if (!MainActivity.this.isCheckedBindState) {
                    int title;
                    if (isOk) {
                        title = R.string.bind_camera_ok;
                    } else {
                        title = R.string.bind_camera_fail;
                    }
                    Builder builder = new Builder(MainActivity.this).setTitle(title).setPositiveButton(R.string.str_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    if (MainActivity.this.bindStatePrompt != null && MainActivity.this.bindStatePrompt.isShowing()) {
                        MainActivity.this.bindStatePrompt.dismiss();
                    }
                    MainActivity.this.bindStatePrompt = builder.create();
                    MainActivity.this.bindStatePrompt.show();
                    MainActivity.this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            if (MainActivity.this.bindStatePrompt != null && MainActivity.this.bindStatePrompt.isShowing()) {
                                MainActivity.this.bindStatePrompt.dismiss();
                            }
                        }
                    }, 10000);
                }
                MainActivity.this.isCheckedBindState = true;
            }
        });
        if (this.mIPCameraManager instanceof Amba2) {
            ChannelDataForward.getInstance().setBindCamera((Amba2) this.mIPCameraManager);
        } else {
            ChannelDataForward.getInstance().exit();
        }
        if ((camera_type & 2) == 2) {
            this.mZoombarContainer.setVisibility(0);
        } else {
            this.mZoombarContainer.setVisibility(4);
        }
    }

    protected void onStop() {
        super.onStop();
        this.mIPCameraManager.finish();
        this.mIPCameraManager = null;
        if (this.mGuardDialog != null) {
            this.mGuardDialog.dismiss();
        }
    }

    protected void onPause() {
        super.onPause();
        Log.i(TAG, "Main Screen onPause");
        this.mHandler.removeCallbacks(this.mWifiEnabledRunnable);
        this.isFSKConneted = false;
        this.isWIFIConneted = false;
        this.mCameraSettingsBtn.setEnabled(false);
        Utilities.setWIFIConnectFlag(false);
        if (this.mController != null) {
            this.mPostSendCommand = false;
            Utilities.UartControllerStandBy(this.mController);
        }
        if (this.mWifiThread != null) {
            this.mWifiThread.interrupt();
            this.mWifiThread = null;
        }
        dismissConnectingDialog();
        resetWarnings();
        unregisterReceiver(this.receiverWifi);
        this.mCameraDaemon.stop(this);
        this.mCameraDaemon = null;
        this.mRecordProcessing = false;
        this.mHasStartedRecord = false;
        this.mHasStoppedRecord = false;
        this.mLastSDcardStatus = null;
        this.mSDErrDialogShown = false;
        this.mIsPaused = true;
        this.validVehicle = false;
        this.isChangeAndStartRecord = false;
        if (this.handlerThread != null) {
            this.handlerThread.quit();
            this.handlerThread.interrupt();
            this.handlerThread = null;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("menu");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    private void createFmodeMenu() {
        View view = View.inflate(this, R.layout.fmode_menu_layout, null);
        this.mFModeList = (ListView) view.findViewById(R.id.fmode_list);
        int[] nameId = new int[]{R.string.fmode_option_1, R.string.fmode_option_2, R.string.fmode_option_3};
        int[] status1 = new int[]{R.string.fmode_smart, R.string.fmode_smart, R.string.fmode_smart};
        int[] status2 = new int[]{R.string.fmode_6axis, R.string.fmode_speed, R.string.fmode_pressure};
        int[] status3 = new int[]{R.string.fmode_gohome, R.string.fmode_gohome, R.string.fmode_gohome};
        ArrayList<HashMap<String, Object>> menuItem = new ArrayList();
        for (int i = 0; i < 3; i++) {
            HashMap<String, Object> map = new HashMap();
            map.put(DBOpenHelper.KEY_NAME, getString(nameId[i]));
            map.put("status1", getString(status1[i]));
            map.put("status2", getString(status2[i]));
            map.put("status3", getString(status3[i]));
            menuItem.add(map);
        }
        this.mFModeList.setAdapter(new SimpleAdapter(this, menuItem, R.layout.fmode_item_layout, new String[]{DBOpenHelper.KEY_NAME, "status1", "status2", "status3"}, new int[]{R.id.option, R.id.status1, R.id.status2, R.id.status3}));
        this.mFModeList.setChoiceMode(1);
        this.mFModeList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                MainActivity.this.mPref.edit().putInt("fmode_option", position).commit();
                MainActivity.this.sendFmodeData(position);
                MainActivity.this.mFModeOptions.dismiss();
            }
        });
        this.mFModeOptions = new Builder(this).setView(view).create();
        this.mFModeList.setSelection(this.mPref.getInt("fmode_option", 1));
    }

    private void sendFmodeData(int opt) {
        Utilities.getFmodeChannelValues(this);
        MixedData data1 = new MixedData();
        data1.mFmode = 0;
        data1.mChannel = 5;
        data1.mhardware = 21;
        data1.mHardwareType = 2;
        data1.mPriority = 1;
        data1.mMixedType = 3;
        data1.mSwitchStatus.add(0, Boolean.valueOf(true));
        data1.mSwitchStatus.add(1, Boolean.valueOf(true));
        data1.mSwitchStatus.add(2, Boolean.valueOf(true));
        data1.mSwitchValue.add(0, Integer.valueOf(Utilities.value_ch5[0]));
        data1.mSwitchValue.add(1, Integer.valueOf(Utilities.value_ch5[1]));
        data1.mSwitchValue.add(2, Integer.valueOf(Utilities.value_ch5[2]));
        data1.mSpeed = 10;
        data1.mReverse = false;
        this.mController.syncMixingData(true, data1, 1);
        MixedData data2 = new MixedData();
        data2.mFmode = 0;
        data2.mChannel = 6;
        data2.mhardware = 21;
        data2.mHardwareType = 2;
        data2.mPriority = 1;
        data2.mMixedType = 3;
        data2.mSwitchStatus.add(0, Boolean.valueOf(true));
        data2.mSwitchStatus.add(1, Boolean.valueOf(true));
        data2.mSwitchStatus.add(2, Boolean.valueOf(true));
        data2.mSwitchValue.add(0, Integer.valueOf(Utilities.value_ch6[0]));
        data2.mSwitchValue.add(1, Integer.valueOf(Utilities.value_ch6[1]));
        data2.mSwitchValue.add(2, Integer.valueOf(Utilities.value_ch6[2]));
        data2.mSpeed = 10;
        data2.mReverse = false;
        this.mController.syncMixingData(true, data2, 1);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    }

    public void onAnimationStart(Animation animation) {
    }

    public void onAnimationEnd(Animation animation) {
        if (animation.equals(this.mBottomFadeIn)) {
            this.mHomeCompassView.setVisibility(0);
        } else if (animation.equals(this.mBottomFadeOut)) {
            this.mHomeCompassView.setVisibility(4);
        } else if (animation.equals(this.mTrimLeftFadeIn)) {
            this.mLeftTrimView.setVisibility(0);
        } else if (animation.equals(this.mTrimLeftFadeOut)) {
            this.mLeftTrimView.setVisibility(4);
        } else if (animation.equals(this.mTrimRightFadeIn)) {
            this.mRightTrimView.setVisibility(0);
        } else if (animation.equals(this.mTrimRightFadeOut)) {
            this.mRightTrimView.setVisibility(4);
        }
    }

    public void onAnimationRepeat(Animation animation) {
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode != 3 || event.isCanceled()) {
            return super.onKeyUp(keyCode, event);
        }
        onBackPressed();
        return true;
    }

    public void onBackPressed() {
    }

    private void dismissFModeFailDialog() {
        if (this.mFmodeFailDialog != null && this.mFmodeFailDialog.isShowing()) {
            this.mFmodeFailDialog.dismiss();
        }
    }

    private void showFModeFailDialog() {
        this.mFmodeFailDialog = new OneButtonPopDialog(this);
        this.mFmodeFailDialog.setTitle((int) R.string.str_exit_warning_title);
        this.mFmodeFailDialog.adjustHeight(380);
        this.mFmodeFailDialog.setMessage(getResources().getString(R.string.str_motor_status_is_error));
        this.mFmodeFailDialog.setPositiveButton(R.string.str_ok, new OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.mFmodeFailDialog.dismiss();
                MainActivity.this.stopVibrator(1);
            }
        });
        if (!this.mFmodeFailDialog.isShowing()) {
            this.mFmodeFailDialog.show();
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    MainActivity.this.dismissFModeFailDialog();
                    MainActivity.this.stopVibrator(1);
                }
            }, 10000);
        }
    }

    private void showToastDontStack(int resId, int duration) {
        if (this.mToast != null) {
            this.mToast.cancel();
        }
        this.mToast = MyToast.makeText((Context) this, resId, 0, duration);
        this.mToast.show();
    }

    private int checkModelStatus() {
        if (!checkIsFPVModel()) {
            return R.string.select_fpv_model;
        }
        WifiManager wifi = (WifiManager) getSystemService("wifi");
        if (!wifi.isWifiEnabled()) {
            Log.i(TAG, "Wifi is not opened");
            return R.string.connect_camera_hint;
        } else if (wifi.getConnectionInfo().getSupplicantState().equals(SupplicantState.COMPLETED)) {
            return 0;
        } else {
            return R.string.connect_camera_hint;
        }
    }

    private void enableButtonBar(boolean enabled) {
        this.mBtnFlightSetting.setEnabled(true);
        this.mBtnModelSelect.setEnabled(true);
        this.mBtnSystemSetting.setEnabled(true);
    }

    private boolean preparePlayer() {
        this.mRTVPlayer = RTVPlayer.getPlayer(2);
        this.mRTVPlayer.init(this, 1, false);
        this.mRTVPlayer.setSurfaceView(this.mLCDDislpayView);
        this.mRTVPlayer.setVideoEventCallback(this.mVideoEventCallback);
        return true;
    }

    private String prepareFilename() {
        Time now = new Time();
        now.setToNow();
        return now.format("%Y%m%d_%H-%M-%S");
    }

    private boolean playFPV() {
        if (this.mRTVPlayer.isPlaying()) {
            Log.i(TAG, "Player has already been played");
            return false;
        } else if (((WifiManager) getSystemService("wifi")).isWifiEnabled()) {
            this.mLCDDislpayView.setVisibility(0);
            return this.mRTVPlayer.play(this.mCurrentVideoLocation);
        } else {
            Log.i(TAG, "Wifi is not opened");
            return false;
        }
    }

    private void stopFPV() {
        if (this.mRTVPlayer.isPlaying()) {
            this.mRTVPlayer.stop();
        } else {
            Log.i(TAG, "Player has already been stopped");
        }
        MarginLayoutParams mlp = (MarginLayoutParams) this.mMainscreenLcdFrame.getLayoutParams();
        int width = mlp.width;
        int height = mlp.height;
        if (!(width == this.initLcdWidth && height == this.initLcdHeight)) {
            this.mStatusBarView.setVisibility(0);
            mlp.width = this.initLcdWidth;
            mlp.height = this.initLcdHeight;
            mlp.setMargins(0, this.initLcdTopMargin, 0, 0);
            this.mMainscreenLcdFrame.setLayoutParams(mlp);
        }
        this.mLCDDislpayView.setVisibility(4);
    }

    private void startWaitingAnimation(View v) {
        if (v.equals(this.mBtnSnapshot)) {
            this.mBtnSnapshot.setEnabled(false);
            this.mBtnSnapshot.setBackgroundResource(R.drawable.fpv_request_waiting);
            ((AnimationDrawable) this.mBtnSnapshot.getBackground()).start();
        } else if (v.equals(this.mBtnRecord)) {
            this.mBtnRecord.setEnabled(false);
            this.mBtnRecord.setBackgroundResource(R.drawable.fpv_request_waiting_another);
            ((AnimationDrawable) this.mBtnRecord.getBackground()).start();
        } else {
            Log.w(TAG, "Unknown Button to perform waiting animation:" + v.toString());
        }
    }

    private void stopWaitingAnimation(View v) {
        if (v.equals(this.mBtnSnapshot)) {
            if (this.mFlightToggle.isChecked()) {
                this.mBtnSnapshot.setEnabled(true);
            } else {
                this.mBtnSnapshot.setEnabled(false);
            }
            if (this.mBtnSnapshot.getBackground() instanceof AnimationDrawable) {
                ((AnimationDrawable) this.mBtnSnapshot.getBackground()).stop();
            }
            this.mBtnSnapshot.setBackgroundResource(R.drawable.fpv_camera);
        } else if (v.equals(this.mBtnRecord)) {
            if (this.mFlightToggle.isChecked()) {
                this.mBtnRecord.setEnabled(true);
            } else {
                this.mBtnRecord.setEnabled(false);
            }
            if (this.mBtnRecord.getBackground() instanceof AnimationDrawable) {
                ((AnimationDrawable) this.mBtnRecord.getBackground()).stop();
            }
            this.mBtnRecord.setBackgroundResource(R.drawable.fpv_record);
            if (this.mIsRecording) {
                this.mBtnRecord.setEnabled(true);
            }
        } else {
            Log.w(TAG, "Unknown Button to stop waiting animation:" + v.toString());
        }
    }

    private void onSDcardChanged(SDCardStatus sd) {
        boolean prompt = false;
        CharSequence title = null;
        CharSequence prompt_msg = null;
        if (!this.mIsPaused) {
            if (sd.equals(this.mLastSDcardStatus)) {
                this.mLastSDcardStatus = sd;
                return;
            }
            if (!sd.isInsert) {
                title = getResources().getString(R.string.camera_sdcard_not_inserted_title);
                prompt_msg = getResources().getString(R.string.camera_sdcard_not_inserted);
                prompt = true;
            } else if (sd.free_space < 420) {
                title = getResources().getString(R.string.storage_almost_full_title);
                prompt_msg = getResources().getString(R.string.camera_storage_almost_full);
                prompt = true;
            } else {
                this.mSDErrDialogShown = false;
            }
            if (prompt && !this.mSDErrDialogShown) {
                final OneButtonPopDialog dialog = new OneButtonPopDialog(this);
                dialog.setCancelable(false);
                dialog.setTitle(title);
                dialog.setMessage(prompt_msg);
                dialog.setPositiveButton(17039370, new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                this.mSDErrDialogShown = true;
            }
            this.mLastSDcardStatus = sd;
        }
    }

    private void onSDcardChanged(int freeSpace) {
        boolean prompt = false;
        CharSequence title = null;
        CharSequence prompt_msg = null;
        if (!this.mIsPaused) {
            if ((freeSpace >> 10) <= 30) {
                title = getResources().getString(R.string.camera_sdcard_not_inserted_title);
                prompt_msg = getResources().getString(R.string.camera_sdcard_not_inserted);
                prompt = true;
            } else if (freeSpace < LOW_CAMERA_STORAGE_THRESHOLD) {
                title = getResources().getString(R.string.storage_almost_full_title);
                prompt_msg = getResources().getString(R.string.camera_storage_almost_full);
                prompt = true;
            } else {
                this.mSDErrDialogShown = false;
            }
            if (prompt && !this.mSDErrDialogShown) {
                final OneButtonPopDialog dialog = new OneButtonPopDialog(this);
                dialog.setCancelable(false);
                dialog.setTitle(title);
                dialog.setMessage(prompt_msg);
                dialog.setPositiveButton(17039370, new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
                this.mSDErrDialogShown = true;
            }
        }
    }

    private void setRlnText(String res) {
        if (res != null) {
            this.mRlnText.setText(res);
            this.mRlnText.setVisibility(0);
            return;
        }
        this.mRlnText.setVisibility(4);
    }

    private void setRlnBtnText(String res) {
        if (res != null) {
            if (!this.mRlnButton.isEnabled()) {
                this.mRlnButton.setEnabled(true);
            }
            if (res.contains("60P")) {
                this.mResolution = "60P";
                this.mRlnButton.setText(R.string.str_rln_60f_icon);
                this.mRlnButton.setVisibility(0);
                return;
            } else if (res.contains("50P")) {
                this.mResolution = "50P";
                this.mRlnButton.setText(R.string.str_rln_50f_icon);
                this.mRlnButton.setVisibility(0);
                return;
            } else if (res.contains("48P")) {
                this.mResolution = "48P";
                this.mRlnButton.setText(R.string.str_rln_48f_icon);
                this.mRlnButton.setVisibility(0);
                return;
            } else {
                this.mResolution = null;
                this.mRlnButton.setVisibility(4);
                this.mRlnButton.setEnabled(false);
                return;
            }
        }
        this.mRlnButton.setVisibility(4);
        this.mRlnButton.setEnabled(false);
    }

    private void setVideoStandard(String selectedRln) {
        if (selectedRln == null) {
            return;
        }
        if ("48P".equals(selectedRln) || "60P".equals(selectedRln)) {
            this.mIPCameraManager.setVideoStandard(this.mHttpResponseMessenger, 1);
        } else if ("50P".equals(selectedRln)) {
            this.mIPCameraManager.setVideoStandard(this.mHttpResponseMessenger, 2);
        }
    }

    private void setVideoResolution(String selectedRln) {
        if (selectedRln == null) {
            return;
        }
        if ("48P".equals(selectedRln)) {
            this.mIPCameraManager.setVideoResolution(this.mHttpResponseMessenger, 5);
        } else if ("50P".equals(selectedRln)) {
            this.mIPCameraManager.setVideoResolution(this.mHttpResponseMessenger, 3);
        } else if ("60P".equals(selectedRln)) {
            this.mIPCameraManager.setVideoResolution(this.mHttpResponseMessenger, 1);
        }
    }

    private void refreshScreen() {
        if (this.mCurrentCameraName.equals(CAMERA_TYPE_CGO2)) {
            this.mIPCameraManager.getSDCardStatus(this.mHttpResponseMessenger);
            this.mIPCameraManager.restartVF(this.mHttpResponseMessenger);
        }
        if (this.mCurrentCameraName.equals("C-GO3") || this.mCurrentCameraName.equals(CAMERA_TYPE_CGO3_PRO)) {
            updateDrawerLayout();
            if (this.mCurrentCameraName.equals("C-GO3")) {
                this.mSRswitch.syncState(this.mCameraParams.cam_mode != 2);
            }
            if (this.mCameraParams.cam_mode != 2) {
                setRlnText(this.mCameraParams.video_mode);
            } else {
                setRlnText(null);
            }
        }
    }

    private void showProgressDialog(Runnable progressRunnable, long delayMillis) {
        if (this.mProgressDialog == null) {
            this.mProgressDialog = MyProgressDialog.show(this, null, getResources().getText(R.string.str_dialog_waiting), false, false);
            this.mProgressDialog.setCanceledOnTouchOutside(true);
            this.mProgressDialog.setCancelable(false);
            this.mHandler.postDelayed(progressRunnable, delayMillis);
        } else if (!this.mProgressDialog.isShowing()) {
            this.mProgressDialog.show();
            this.mHandler.postDelayed(progressRunnable, delayMillis);
        }
    }

    private void dismissProgressDialog(Runnable progressRunnable) {
        if (this.mProgressDialog != null && this.mProgressDialog.isShowing()) {
            this.mProgressDialog.dismiss();
        }
        if (progressRunnable != null) {
            this.mHandler.removeCallbacks(progressRunnable);
        }
    }

    private void updateFmodeButton(Telemetry info) {
        Log.d(TAG, "Vehicle type is:" + info.vehicle_type + ", fmode is:" + info.f_mode + "info.gps_status:" + info.gps_status);
        if (!this.validVehicle) {
            switch (info.vehicle_type) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    this.mVehicleType = info.vehicle_type;
                    this.validVehicle = true;
                    break;
                default:
                    this.validVehicle = false;
                    setFmodeButtonEnable(false);
                    return;
            }
        }
        if (info.vehicle_type == 4) {
            if (info.gps_status != 1) {
                setFmodeButtonEnable(false);
            } else {
                setFmodeButtonEnable(true);
            }
        } else if (info.vehicle_type != 2 && info.vehicle_type != 5) {
            setFmodeButtonEnable(false);
        } else if (info.gps_status == 1 && (info.cgps_status & 2) == 2) {
            if (!this.mFmodeButton.isEnabled()) {
                setFmodeButtonEnable(true);
            }
            switch (info.f_mode) {
                case 21:
                case 22:
                    setFmodeButtonChecked(false);
                    return;
                case 23:
                case 24:
                    setFmodeButtonChecked(true);
                    return;
                default:
                    return;
            }
        } else {
            setFmodeButtonEnable(false);
        }
    }

    private int getVoltageLevel(float voltage, int vehicle_type) {
        if (vehicle_type == 5) {
            if (voltage >= 15.4f) {
                return 100;
            }
            if (voltage >= 14.9f && voltage < 15.4f) {
                return Math.round((((voltage - 14.9f) * 50.0f) / (15.4f - 14.9f)) + 50.0f);
            }
            if (voltage > 14.2f && voltage < 14.9f) {
                return Math.round((((voltage - 14.2f) * 25.0f) / (14.9f - 14.2f)) + 25.0f);
            }
            if (voltage > 14.0f && voltage <= 14.2f) {
                return Math.round((((voltage - 14.0f) * 20.0f) / (14.2f - 14.0f)) + 5.0f);
            }
            if (voltage > 13.8f && voltage <= 14.0f) {
                return Math.round(((voltage - 13.8f) * 5.0f) / (14.2f - 14.0f));
            }
            if (voltage <= 13.8f) {
                return 0;
            }
            return 0;
        } else if (vehicle_type == 2 || vehicle_type == 4) {
            if (voltage >= 12.6f) {
                return 100;
            }
            if (voltage >= 10.7f && voltage < 12.6f) {
                return Math.round((((voltage - 10.7f) * 75.0f) / (12.6f - 10.7f)) + 25.0f);
            }
            if (voltage > 10.5f && voltage < 10.7f) {
                return Math.round((((voltage - 10.5f) * 20.0f) / (10.7f - 10.5f)) + 5.0f);
            }
            if (voltage > 10.3f && voltage <= 10.5f) {
                return Math.round(((voltage - 10.3f) * 5.0f) / (10.5f - 10.3f));
            }
            if (voltage <= 10.3f) {
                return 0;
            }
            return 0;
        } else if (vehicle_type == 1) {
            if (voltage >= 25.2f) {
                return 100;
            }
            if (voltage >= 23.9f && voltage < 25.2f) {
                return Math.round((((voltage - 23.9f) * 5.0f) / (25.2f - 23.9f)) + 95.0f);
            }
            if (voltage >= 21.7f && voltage < 23.9f) {
                return Math.round((((voltage - 21.7f) * 75.0f) / (23.9f - 21.7f)) + 20.0f);
            }
            if (voltage >= 21.3f && voltage < 21.7f) {
                return Math.round((((voltage - 21.3f) * 5.0f) / (21.7f - 21.3f)) + 5.0f);
            }
            if (voltage >= 21.1f && voltage < 21.3f) {
                return Math.round(((voltage - 21.1f) * 5.0f) / (21.3f - 21.1f));
            }
            if (voltage < 21.1f) {
                return 0;
            }
            return 0;
        } else if (voltage >= 15.4f) {
            return 100;
        } else {
            if (voltage >= 14.9f && voltage < 15.4f) {
                return Math.round((((voltage - 14.9f) * 50.0f) / (15.4f - 14.9f)) + 50.0f);
            }
            if (voltage > 14.2f && voltage < 14.9f) {
                return Math.round((((voltage - 14.2f) * 25.0f) / (14.9f - 14.2f)) + 25.0f);
            }
            if (voltage > 14.0f && voltage <= 14.2f) {
                return Math.round((((voltage - 14.0f) * 20.0f) / (14.2f - 14.0f)) + 5.0f);
            }
            if (voltage > 13.8f && voltage <= 14.0f) {
                return Math.round(((voltage - 13.8f) * 5.0f) / (14.2f - 14.0f));
            }
            if (voltage <= 13.8f) {
                return 0;
            }
            return 0;
        }
    }

    private boolean isValidTelemetryData(Telemetry info) {
        if (info.vehicle_type < 0 || info.vehicle_type > 10) {
            Log.e(TAG, "info.vehicle_type:" + info.vehicle_type);
            return false;
        } else if (info.f_mode < 0 || info.f_mode > 25) {
            Log.e(TAG, "info.f_mode:" + info.f_mode);
            return false;
        } else if (info.altitude >= -100.0f || info.altitude <= 200.0f) {
            return true;
        } else {
            Log.e(TAG, "info.altitude:" + info.altitude);
            return false;
        }
    }

    private void handleTelemetryInfoChanged(Telemetry info) {
        Log.e("lifei", "--->" + info.toString());
        Log.e("lifei", "--->" + info.gps_status + "-----" + info.gps_used);
        updateFmodeButton(info);
        this.mCurrentFmode = info.f_mode;
        if (Utilities.PROJECT_TAG.equals("ST12")) {
            this.mMissionView.updateDroneGps(info.f_mode, info.latitude, info.longitude, info.altitude);
        }
        if (info.gps_status == 1) {
            this.mGPSswtich = true;
        } else if (info.gps_used) {
            this.mGPSswtich = true;
        } else {
            this.mGPSswtich = false;
        }
        if (isValidTelemetryData(info)) {
            int i;
            boolean warning;
            if (this.mIndicatorMODE != null) {
                boolean showFMode = true;
                if (info.vehicle_type != 1) {
                    FCSensorData fcSensorData = new FCSensorData();
                    fcSensorData.setData(info.imu_status, info.press_compass_status);
                    if (fcSensorData.mError != null) {
                        this.mIndicatorMODE.setValueText(fcSensorData.mError);
                        this.mIndicatorMODE.setValueColor(-65536);
                        showFMode = false;
                    }
                    this.mCameraSettingsBtn.setVisibility(0);
                } else {
                    this.mCameraSettingsBtn.setVisibility(4);
                }
                if (showFMode) {
                    FModeData fModeData = new FModeData();
                    fModeData.setData(info.f_mode, info.vehicle_type);
                    this.mIndicatorMODE.setValueText(fModeData.fModeString);
                    this.mIndicatorMODE.setValueColor(fModeData.fModeColor);
                }
            }
            if (this.mIndicatorGPSStatus != null) {
                String str_value;
                int color;
                if (info.gps_status != 1) {
                    str_value = getResources().getString(R.string.str_status_disabled);
                    color = -65536;
                } else if (info.gps_used) {
                    str_value = getResources().getString(R.string.str_status_good);
                    color = -16711936;
                } else {
                    str_value = getResources().getString(R.string.str_status_acquiring);
                    color = -256;
                }
                this.mIndicatorGPSStatus.setValueText(str_value);
                this.mIndicatorGPSStatus.setValueColor(color);
            }
            boolean sonarSwitchOn = (info.imu_status & 16) != 0;
            if (Utilities.PROJECT_TAG.equals("ST12")) {
                this.sonarButton.setChecked(sonarSwitchOn);
            }
            if (this.mIndicatorGPS != null) {
                this.mIndicatorGPS.setValueText(String.valueOf(info.satellites_num));
            }
            if (this.mIndicatorVOL != null) {
                if (mIndex >= VOL_INDEX_MAX) {
                    mIndex = 0;
                }
                float[] fArr = mVOLValue;
                int i2 = mIndex;
                mIndex = i2 + 1;
                fArr[i2] = info.voltage;
                mTmpVOLFloatValue = 0.0f;
                i = 0;
                while (i < VOL_INDEX_MAX && mVOLValue[i] > 0.0f) {
                    mTmpVOLFloatValue += mVOLValue[i];
                    i++;
                }
                mTmpVOLFloatValue /= (float) i;
                mTmpVOLFloatValue = ((float) Math.round(10.0f * mTmpVOLFloatValue)) / 10.0f;
                int TmpLevelValue = getVoltageLevel(mTmpVOLFloatValue, info.vehicle_type);
                this.mIndicatorVOL.setValueText(mTmpVOLFloatValue + " V");
                updateVolDrawable(TmpLevelValue);
                if ((info.error_flags1 & 2) != 0) {
                    this.mIndicatorVOL.setValueColor(-65536);
                } else if ((info.error_flags1 & 1) != 0) {
                    this.mIndicatorVOL.setValueColor(-256);
                } else if (TmpLevelValue > 25) {
                    this.mIndicatorVOL.setValueColor(DEFAULT_COLOR);
                } else if (TmpLevelValue > 5 && TmpLevelValue <= 25) {
                    this.mIndicatorVOL.setValueColor(-256);
                } else if (TmpLevelValue <= 5) {
                    this.mIndicatorVOL.setValueColor(-65536);
                }
            }
            if (this.mIndicatorALT != null) {
                this.mIndicatorALT.setValueText(Utilities.FormatLengthDisplayString(this, info.altitude));
            }
            if (this.mIndicatorTAS != null) {
                this.mIndicatorTAS.setValueText(Utilities.FormatVelocityDisplayString(this, info.tas));
            }
            if (this.mIndicatorPOS != null) {
                this.mIndicatorPOS.setValueText(Utilities.FormatPositionDisplayString(this, info.longitude, info.latitude));
            }
            GPSUpLinkData my_location = this.mGPSUpdater.getCurrentLocation();
            if ((info.latitude == 0.0f && info.longitude == 0.0f) || (my_location.lat == 0.0f && my_location.lon == 0.0f)) {
                this.mIndicatorDIS.setValueText(getResources().getString(R.string.str_value, new Object[]{mInvalidValueString, Utilities.getDisplayLengthUnit(this)}));
                this.mHomeCompassView.setHomeEnabled(false);
            } else {
                float[] distance_bearing = Utilities.calculateDistanceAndBearing(info.latitude, info.longitude, info.altitude, my_location.lat, my_location.lon, 0.0f);
                if (!(this.mIndicatorDIS == null || distance_bearing[0] == Utilities.B_SWITCH_MIN)) {
                    this.mIndicatorDIS.setValueText(Utilities.FormatHomeDistanceDisplayString(this, distance_bearing[0]));
                }
                if (!(this.mHomeCompassView == null || distance_bearing[1] == Utilities.B_SWITCH_MIN)) {
                    if (info.f_mode == 6 || info.f_mode == 23 || info.f_mode == 24 || info.f_mode == 21 || info.f_mode == 22 || info.f_mode == 16) {
                        this.mHomeCompassView.setHomeEnabled(false);
                    } else {
                        this.mHomeCompassView.setHomeEnabled(true);
                        this.mHomeCompassView.setDirection(Math.round(Utilities.getRelativeDegree(distance_bearing[1], info.yaw)));
                    }
                }
            }
            int countMotors = getMotorCount(info.vehicle_type);
            for (i = 0; i < countMotors; i++) {
                warning = ((info.motor_status >> i) & 1) == 0;
                if (info.motor_status != 12 && warning && !this.motorStatusErr) {
                    this.motorStatusErr = true;
                    showFModeFailDialog();
                    startVibrator(1);
                    break;
                }
            }
            warning = (info.error_flags1 & 1) != 0 && (info.error_flags1 & 2) == 0 && FModeData.isMotorWorking(info.f_mode);
            if (warning ^ this.mLastVoltageLowWarning1) {
                if (warning) {
                    showVoltageLowWarning1();
                    startVibrator(1);
                } else {
                    dismissVoltageLowWarning1();
                    stopVibrator(1);
                }
            }
            this.mLastVoltageLowWarning1 = warning;
            warning = (info.error_flags1 & 2) != 0 && FModeData.isMotorWorking(info.f_mode);
            if (warning ^ this.mLastVoltageLowWarning2) {
                if (warning) {
                    showVoltageLowWarning2();
                    startVibrator(4);
                } else {
                    dismissVoltageLowWarning2();
                    stopVibrator(4);
                }
            }
            this.mLastVoltageLowWarning2 = warning;
            int warningType = 0;
            if (info.gps_status == 1) {
                if (!info.gps_used) {
                    warningType = 1;
                }
            } else if (!info.gps_used) {
                warningType = 2;
            }
            warning = !info.gps_used;
            if (warning ^ this.mLastGPSDisabledWarning) {
                if (warning) {
                    showGPSDisabledWarning(warningType);
                    startVibrator(2);
                } else {
                    dismissGPSDisabledWarning();
                    stopVibrator(2);
                }
            }
            this.mLastGPSDisabledWarning = warning;
            warning = (info.error_flags1 & 128) != 0;
            if ((warning ^ this.mLastAirportWarning) && warning) {
                showAirportWarning();
            }
            this.mLastAirportWarning = warning;
            warning = (info.error_flags1 & 4) != 0 && info.vehicle_type == 2;
            if ((warning ^ this.mLastAltitudeWarning) && warning) {
                showAltitudeWarning();
            }
            this.mLastAltitudeWarning = warning;
        }
    }

    public int getMotorCount(int vehicleType) {
        if (vehicleType == 1 || vehicleType == 5) {
            return 6;
        }
        if (vehicleType == 3) {
            return 4;
        }
        if (vehicleType == 2) {
            return 4;
        }
        if (vehicleType == 4) {
            return 4;
        }
        if (vehicleType == MotionEventCompat.ACTION_MASK) {
            return 8;
        }
        return 6;
    }

    private void updateVolDrawable(int voltageLevel) {
        int level;
        if (voltageLevel <= 0) {
            level = 0;
        } else if (voltageLevel <= 5) {
            level = 10;
        } else if (voltageLevel <= 25) {
            level = 25;
        } else if (voltageLevel <= 50) {
            level = 50;
        } else if (voltageLevel <= 75) {
            level = 75;
        } else {
            level = 100;
        }
        this.mIndicatorVOL.setValueTextDrawableLevel(level);
    }

    private void showVoltageLowWarning1() {
        this.mVoltageLowWarning1Dialog = new Dialog(this, R.style.warning_dialog_style);
        this.mVoltageLowWarning1Dialog.setContentView(R.layout.voltage_low_warning1_dialog);
        this.mVoltageLowWarning1Dialog.show();
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                MainActivity.this.dismissVoltageLowWarning1();
            }
        }, 11000);
    }

    private void dismissVoltageLowWarning1() {
        if (this.mVoltageLowWarning1Dialog != null && this.mVoltageLowWarning1Dialog.isShowing()) {
            this.mVoltageLowWarning1Dialog.dismiss();
        }
    }

    private void showVoltageLowWarning2() {
        this.mVoltageLowWarning2Dialog = new Dialog(this, R.style.warning_dialog_style);
        this.mVoltageLowWarning2Dialog.setContentView(R.layout.voltage_low_warning2_dialog);
        this.mVoltageLowWarning2Dialog.show();
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                MainActivity.this.dismissVoltageLowWarning2();
            }
        }, 11000);
    }

    private void dismissVoltageLowWarning2() {
        if (this.mVoltageLowWarning2Dialog != null && this.mVoltageLowWarning2Dialog.isShowing()) {
            this.mVoltageLowWarning2Dialog.dismiss();
        }
    }

    private void showGPSDisabledWarning(int warningType) {
        if (this.mGPSDisabledWarningDialog == null) {
            this.mGPSDisabledWarningDialog = new OneButtonPopDialog(this);
            this.mGPSDisabledWarningDialog.adjustHeight(DataProviderHelper.MODEL_TYPE_GLIDER_BASE);
            this.mGPSDisabledWarningDialog.setCancelable(true);
            this.mGPSDisabledWarningDialog.setTitle((int) R.string.str_exit_warning_title);
            this.mGPSDisabledWarningDialog.setTitleCompoundDrawable(getResources().getDrawable(17301642));
            if (warningType == 1) {
                this.mGPSDisabledWarningDialog.setMessage((int) R.string.str_warning_gps_lost);
            } else {
                this.mGPSDisabledWarningDialog.setMessage((int) R.string.str_warning_gps_disabled);
            }
            this.mGPSDisabledWarningDialog.setButtonVisble(false);
        }
        if (!this.mGPSDisabledWarningDialog.isShowing()) {
            if (warningType == 1) {
                this.mGPSDisabledWarningDialog.setMessage((int) R.string.str_warning_gps_lost);
            } else {
                this.mGPSDisabledWarningDialog.setMessage((int) R.string.str_warning_gps_disabled);
            }
            this.mGPSDisabledWarningDialog.show();
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    MainActivity.this.dismissGPSDisabledWarning();
                    MainActivity.this.stopVibrator(2);
                }
            }, 11000);
        }
    }

    private void dismissGPSDisabledWarning() {
        if (this.mGPSDisabledWarningDialog != null && this.mGPSDisabledWarningDialog.isShowing()) {
            this.mGPSDisabledWarningDialog.dismiss();
        }
    }

    private void startVibrator(int priority) {
        if (priority == 4) {
            if ((this.currentPriority & 4) != 4) {
                this.mVibrator.cancel();
                this.mVibrator.vibrate(new long[]{1000, 1000, 1000, 1000}, 0);
            }
            this.currentPriority |= 4;
        } else if (priority == 2) {
            if (!((this.currentPriority & 4) == 4 || (this.currentPriority & 2) == 2)) {
                this.mVibrator.cancel();
                this.mVibrator.vibrate(new long[]{1000, 1000, 1000, 1000}, 0);
            }
            this.currentPriority |= 2;
        } else {
            if (!((this.currentPriority & 4) == 4 || (this.currentPriority & 2) == 2 || (this.currentPriority & 1) == 1)) {
                this.mVibrator.cancel();
                this.mVibrator.vibrate(new long[]{1000, 1000, 5000, 1000}, 0);
            }
            this.currentPriority |= 1;
        }
    }

    private void stopVibrator(int priority) {
        if ((this.currentPriority & priority) != 0) {
            this.mVibrator.cancel();
            this.currentPriority &= priority ^ -1;
            if ((this.currentPriority & 4) == 4) {
                this.mVibrator.vibrate(new long[]{1000, 1000, 1000, 1000}, 0);
            } else if ((this.currentPriority & 2) == 2) {
                this.mVibrator.vibrate(new long[]{1000, 1000, 1000, 1000}, 0);
            } else if ((this.currentPriority & 1) == 1) {
                this.mVibrator.vibrate(new long[]{1000, 1000, 5000, 1000}, 0);
            }
        }
    }

    private void showAirportWarning() {
        if (this.mAirportWarningDialog == null) {
            this.mAirportWarningDialog = new OneButtonPopDialog(this);
            this.mAirportWarningDialog.adjustHeight(DataProviderHelper.MODEL_TYPE_GLIDER_BASE);
            this.mAirportWarningDialog.setCancelable(true);
            this.mAirportWarningDialog.setTitle((int) R.string.str_exit_warning_title);
            this.mAirportWarningDialog.setTitleCompoundDrawable(getResources().getDrawable(17301642));
            this.mAirportWarningDialog.setMessage((int) R.string.str_warning_airport);
            this.mAirportWarningDialog.setButtonVisble(false);
        }
        if (!this.mAirportWarningDialog.isShowing()) {
            this.mAirportWarningDialog.show();
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    MainActivity.this.dismissAirportWarning();
                }
            }, 11000);
        }
    }

    private void dismissAirportWarning() {
        if (this.mAirportWarningDialog != null && this.mAirportWarningDialog.isShowing()) {
            this.mAirportWarningDialog.dismiss();
        }
    }

    private void showAltitudeWarning() {
        if (this.mAltitudeWarningDialog == null) {
            this.mAltitudeWarningDialog = new OneButtonPopDialog(this);
            this.mAltitudeWarningDialog.adjustHeight(DataProviderHelper.MODEL_TYPE_GLIDER_BASE);
            this.mAltitudeWarningDialog.setCancelable(true);
            this.mAltitudeWarningDialog.setTitle((int) R.string.str_exit_warning_title);
            this.mAltitudeWarningDialog.setTitleCompoundDrawable(getResources().getDrawable(17301642));
            this.mAltitudeWarningDialog.setMessage((int) R.string.str_warning_altitude);
            this.mAltitudeWarningDialog.setButtonVisble(false);
        }
        if (!this.mAltitudeWarningDialog.isShowing()) {
            this.mAltitudeWarningDialog.show();
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    MainActivity.this.dismissAltitudeWarning();
                }
            }, 11000);
        }
    }

    private void dismissAltitudeWarning() {
        if (this.mAltitudeWarningDialog != null && this.mAltitudeWarningDialog.isShowing()) {
            this.mAltitudeWarningDialog.dismiss();
        }
    }

    private void resetWarnings() {
        this.mLastVoltageLowWarning1 = false;
        this.mLastVoltageLowWarning2 = false;
        this.mLastGPSDisabledWarning = true;
        this.mLastAirportWarning = false;
        this.mLastAltitudeWarning = false;
        dismissVoltageLowWarning1();
        dismissVoltageLowWarning2();
        dismissGPSDisabledWarning();
        dismissAirportWarning();
        dismissAltitudeWarning();
        stopVibrator(7);
    }

    private int trimDataConversion(float data) {
        if (data > 20.0f) {
            data = 20.0f;
        }
        if (data < -20.0f) {
            data = -20.0f;
        }
        return (int) data;
    }

    private void handleSwitchChanged(SwitchChanged sc) {
        if (this.mProgressDialog == null || !this.mProgressDialog.isShowing()) {
            if (this.mIsPlayFPV) {
                if (sc.hw_id == Utilities.CAMERA_KEY_INDEX) {
                    if (sc.old_state == 0 && sc.new_state == 1) {
                        Log.d(TAG, "Camera Button pressed");
                    } else if (sc.old_state == 1 && sc.new_state == 0) {
                        Log.d(TAG, "Camera Button released");
                        if (this.mSnapShotProcessing) {
                            Log.d(TAG, "SnapShot request processing...");
                        } else if (this.mIsRecording) {
                            this.mStatusBarView.setInfoText(getResources().getString(R.string.snapshot_while_recording), -65536);
                        } else {
                            this.mSnapShotProcessing = true;
                            this.mBtnSnapshot.performClick();
                            this.mSoundPool.play(this.mCameraShutterSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                        }
                        this.mRTVPlayer.snapShot(0, "/sdcard/FPV-Video/Local/" + prepareFilename(), 0, 0);
                    }
                } else if (sc.hw_id == Utilities.VIDEO_KEY_INDEX) {
                    if (sc.old_state == 0 && sc.new_state == 1) {
                        Log.d(TAG, "Video Button pressed");
                    } else if (sc.old_state == 1 && sc.new_state == 0) {
                        Log.d(TAG, "Video Button released");
                        if (this.cameraModeCache == 2 || this.mCameraParams.cam_mode == 2) {
                            if (!this.isChangeAndStartRecord) {
                                this.mSRswitch.setChecked(true);
                                this.isChangeAndStartRecord = true;
                                showProgressDialog(this.mChangeCameraModeRunnable, 5000);
                            }
                        } else if (this.mRecordProcessing) {
                            Log.d(TAG, "Record request processing...");
                        } else {
                            this.mRecordProcessing = true;
                            this.mBtnRecord.performClick();
                        }
                        if (this.mRTVPlayer.canRecord()) {
                            if (this.mRTVPlayer.isRecording()) {
                                this.mRTVPlayer.stopRecord();
                                this.mIsLocalRecording = false;
                            } else if (this.mRTVPlayer.startRecord("/sdcard/FPV-Video/Local/" + prepareFilename()) >= 0) {
                                this.mIsLocalRecording = true;
                            }
                            this.mSoundPool.play(this.mRecorderShutterSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
                        } else {
                            Log.w(TAG, "RTVPlayer can't record for now, maybe the video is not playing");
                        }
                    }
                }
            } else if (this.mFlightToggle.isChecked()) {
                if (sc.hw_id == Utilities.FMODE_KEY_INDEX) {
                    Log.d(TAG, "F mode Key changed old:" + sc.old_state + " new:" + sc.new_state);
                    this.mTimerHelper.handleFmodeTrigger();
                }
            } else if (sc.hw_id == Utilities.BIND_KEY_INDEX) {
                if (!this.mInitiailDataTranfered) {
                    Log.d(TAG, "data still initializing...");
                    return;
                } else if (sc.old_state == 0 && sc.new_state == 1) {
                    Log.d(TAG, "Bind Button pressed");
                } else if (sc.old_state == 1 && sc.new_state == 0) {
                    Log.d(TAG, "Bind Button released");
                    startActivity(new Intent(this, FlightSettings.class));
                }
            }
            if (sc.hw_id == Utilities.FMODE_KEY_INDEX) {
                this.mStatusBarView.setLeftText(1, getString(R.string.fmode_state, new Object[]{Integer.valueOf(sc.new_state)}));
            }
        }
    }

    private void handleZoomAction(Channel cmsg) {
        if (cmsg.channels.size() >= Utilities.ZOOM_KEY_INDEX) {
            zoom((int) Utilities.getChannelValue(cmsg, Utilities.ZOOM_KEY_INDEX));
        }
    }

    private void zoom(int value) {
        this.mZoombar.setProgress((value * 18) / 4096);
    }

    private void handleTimerTirgger(Channel cmsg) {
        if (this.mFlightToggle.isChecked()) {
            this.mTimerHelper.handleThrottleTrigger(cmsg);
        }
    }

    private void changeSRstateCache(int mode) {
        if (this.mSRswitchCacheRunnable != null) {
            this.mHandler.removeCallbacks(this.mSRswitchCacheRunnable);
        }
        this.cameraModeCache = mode;
        Log.d(TAG, "CameraMode-- changeSRstateCache=" + mode + ",cancel change after 10200 millis");
        this.mHandler.postDelayed(this.mSRswitchCacheRunnable, 10200);
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int tmp = progress + 1;
        if (this.mValueForZoom != tmp) {
            this.mHandler.removeCallbacks(this.mZoomRunnable);
            this.mValueForZoom = tmp;
            this.mHandler.postDelayed(this.mZoomRunnable, 200);
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private void reconnectCamera() {
        if (this.mIsPaused) {
            Log.d(TAG, "the activity is paused, don't retry connect to camera");
            return;
        }
        if (!this.mFlightToggle.isChecked()) {
            Log.d(TAG, "the plane has been taken off, don't retry connect to camera");
        }
        onWiFiConntectionLost();
        this.mStatusBarView.setInfoText(getResources().getString(R.string.unable_connect_to_camera_video), -65536);
        this.mHandler.removeCallbacks(this.mReconnectCameraRunnable);
        this.mHandler.postDelayed(this.mReconnectCameraRunnable, 3000);
    }

    private void onWiFiConntectionLost() {
        Log.i(TAG, "#### Rec onWiFiConntectionLost mIsRecording=" + this.mIsRecording);
        if (this.mRTVPlayer.isRecording()) {
            this.mRTVPlayer.stopRecord();
            this.mIsLocalRecording = false;
        }
        this.mRecTime.stop();
        this.mRecTime.setVisibility(4);
        if (this.mCurrentCameraName.equals(CAMERA_TYPE_CGO3_PRO) || this.mCurrentCameraName.equals("C-GO3")) {
            this.mSRswitch.setVisibility(0);
        }
        this.mResolution = null;
        setRlnBtnText(null);
        if (this.mIsRecording) {
            this.mIsRecording = false;
            this.mBtnRecord.setChecked(false);
        }
        this.mRecordProcessing = false;
        this.mHasStartedRecord = false;
        this.mHasStoppedRecord = false;
        this.mSDErrDialogShown = false;
        this.isChangeAndStartRecord = false;
        setRlnText(null);
    }

    private void prepareVideoFolder() {
        File folder = new File(SAVED_LOCAL_VIDEO);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    private void prepareSoundPool() {
        this.mSoundPool = new SoundPool(2, 3, 0);
        this.mCameraShutterSoundId = this.mSoundPool.load(this, R.raw.camera_click2, 1);
        this.mRecorderShutterSoundId = this.mSoundPool.load(this, R.raw.video_record, 1);
    }

    private void connectWifi() {
        if (this.mCurrentModelId != -2) {
            Utilities.connectModelWifi(this, this.mCurrentModelId);
        }
    }

    private void showConnectingDialog() {
        if (this.mCurrentModelId != -2) {
            if (this.mConnectingDialog == null) {
                this.mConnectingDialog = MyProgressDialog.show(this, null, getResources().getText(R.string.str_connect_status), false, false);
                this.mConnectingDialog.setCanceledOnTouchOutside(true);
            } else if (!this.mConnectingDialog.isShowing()) {
                this.mConnectingDialog.show();
            }
            if (this.mConnectingDialog == null) {
                return;
            }
            if (this.isFSKConneted && !this.isWIFIConneted) {
                this.mConnectingDialog.setMessage(getResources().getText(R.string.str_connect_fsk_completed));
            } else if (!this.isFSKConneted && this.isWIFIConneted) {
                this.mConnectingDialog.setMessage(getResources().getText(R.string.str_connect_wifi_completed));
            } else if (this.isFSKConneted && this.isWIFIConneted) {
                this.mConnectingDialog.setMessage(getResources().getText(R.string.str_connect_all_completed));
                dismissConnectingDialog();
            } else {
                this.mConnectingDialog.setMessage(getResources().getText(R.string.str_connect_status));
            }
        }
    }

    private void dismissConnectingDialog() {
        if (this.mConnectingDialog != null && this.mConnectingDialog.isShowing()) {
            this.mConnectingDialog.dismiss();
        }
    }

    private boolean isBindWifi(Context context, long model_id) {
        if (Utilities.getModelWifiInfoToDatabase(context, model_id) != null) {
            return true;
        }
        return false;
    }

    private boolean isBindRX(Context context, long model_id) {
        boolean result = false;
        if (model_id == -2) {
            return false;
        }
        Uri uri = ContentUris.withAppendedId(DataProvider.MODEL_URI, model_id);
        Cursor c = context.getContentResolver().query(uri, new String[]{DBOpenHelper.KEY_ID, DBOpenHelper.KEY_RX}, null, null, null);
        if (!DataProviderHelper.isCursorValid(c)) {
            Log.e(TAG, "isBindRX, Cursor is invalid");
            result = false;
        }
        String rx_id = c.getString(c.getColumnIndex(DBOpenHelper.KEY_RX));
        c.close();
        if (rx_id != null) {
            return true;
        }
        return result;
    }

    private void setupDrawerLayout() {
        this.mCameraSettingsBtn = (Button) findViewById(R.id.camera_settings);
        this.mCameraSettingsBtn.setEnabled(false);
        this.mCameraSettingsBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                final TwoButtonPopDialog dialog = new TwoButtonPopDialog(MainActivity.this);
                dialog.adjustHeight(380);
                dialog.setTitle((int) R.string.str_exit_warning_title);
                dialog.setMessage((int) R.string.str_exit_warning_message2);
                dialog.setPositiveButton(17039379, new OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                        MainActivity.this.mRlnBtnPressed = true;
                        MainActivity.this.stopFPV();
                        Intent intent = new Intent(MainActivity.this, CameraSettingsActivity.class);
                        intent.putExtra("currentCamera", MainActivity.this.mCurrentCameraName);
                        intent.putExtra("wifi_connected", MainActivity.this.isWIFIConneted);
                        intent.putExtra("fmodeStatus", MainActivity.this.mCurrentFmode);
                        intent.putExtra("gps_switch", MainActivity.this.mGPSswtich);
                        if (MainActivity.this.mCameraParams != null) {
                            Log.i(MainActivity.TAG, "mCameraParams != null" + MainActivity.this.mCameraParams);
                            intent.putExtra("camera_params", MainActivity.this.mCameraParams);
                        }
                        MainActivity.this.startActivity(intent);
                    }
                });
                dialog.setNegativeButton(17039369, new OnClickListener() {
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });
        this.wbIsoFrame = (FrameLayout) findViewById(R.id.left_layout);
        this.cameraControlCombine = (RelativeLayout) findViewById(R.id.cemare_control_combine);
        ToggleFrameView toggleFrameView = (ToggleFrameView) findViewById(R.id.toggle_frame);
        toggleFrameView.setToggleOnClickListener(new ToggleOnClickListener() {
            public void wbItemOnClick(View toggleWbView) {
                MainActivity.this.mCurrentExposureIndex = 0;
                MainActivity.this.setExposure(MainActivity.this.mCurrentExposureIndex);
            }

            public void isoItemOnClick(View toggleIsoView) {
                MainActivity.this.mCurrentExposureIndex = 1;
                MainActivity.this.setExposure(MainActivity.this.mCurrentExposureIndex);
            }
        });
        this.wbIsoChangedListener = toggleFrameView;
        this.mWhitBalanceList = (WhiteBalanceScrollView) findViewById(R.id.white_balance);
        this.mWhitBalanceList.setVerticalScrollBarEnabled(false);
        this.mWhitBalanceList.setOnItemSelectedListener(this.mOnItemSelectedListener);
        this.mManualModeFrame = (LinearLayout) findViewById(R.id.mode_manual);
        this.mAutoModeFrame = (LinearLayout) findViewById(R.id.mode_auto);
        this.mEVListValue = Arrays.asList(getResources().getStringArray(R.array.ev_array_value));
        this.mISOListValue = Arrays.asList(getResources().getStringArray(R.array.iso_array_value));
        this.mSHTimeListValue = Arrays.asList(getResources().getStringArray(R.array.shutter_time_value));
        String[] evArray = getResources().getStringArray(R.array.ev_array);
        this.mEVpicker = (StyledNumberPicker) findViewById(R.id.ev_picker);
        this.mEVpicker.setOnValueChangedListener(this.mPickerChangedListener);
        this.mEVpicker.setOnScrollListener(this.mPickerScrollListener);
        this.mEVpicker.setOnButtonClickedListener(this.mPickerButtonListener);
        this.mEVpicker.setDisplayedValues(evArray);
        this.mEVpicker.setMinValue(0);
        this.mEVpicker.setMaxValue(evArray.length - 1);
        this.mEVincrement = (ImageView) findViewById(R.id.increment_ev);
        this.mEVdecrement = (ImageView) findViewById(R.id.decrement_ev);
        this.mAutoBtn = (Button) findViewById(R.id.auto_btn);
        this.mAutoBtn.setOnClickListener(this.mModeSwitchListener);
        this.mManualBtn = (Button) findViewById(R.id.manual_btn);
        this.mManualBtn.setOnClickListener(this.mModeSwitchListener);
        String[] isoArray = getResources().getStringArray(R.array.iso_array);
        this.mISOpicker = (StyledNumberPicker) findViewById(R.id.iso_picker);
        this.mISOpicker.setOnValueChangedListener(this.mPickerChangedListener);
        this.mISOpicker.setOnScrollListener(this.mPickerScrollListener);
        this.mISOpicker.setOnButtonClickedListener(this.mPickerButtonListener);
        this.mISOpicker.setDisplayedValues(isoArray);
        this.mISOpicker.setMinValue(0);
        this.mISOpicker.setMaxValue(isoArray.length - 1);
        this.mISOincrement = (ImageView) findViewById(R.id.increment_iso);
        this.mISOdecrement = (ImageView) findViewById(R.id.decrement_iso);
        String[] shutterTime = getResources().getStringArray(R.array.shutter_time);
        this.mSTpicker = (StyledNumberPicker) findViewById(R.id.shuttertime_picker);
        this.mSTpicker.setOnValueChangedListener(this.mPickerChangedListener);
        this.mSTpicker.setOnScrollListener(this.mPickerScrollListener);
        this.mSTpicker.setOnButtonClickedListener(this.mPickerButtonListener);
        this.mSTpicker.setDisplayedValues(shutterTime);
        this.mSTpicker.setMinValue(0);
        this.mSTpicker.setMaxValue(shutterTime.length - 1);
        this.mSTincrement = (ImageView) findViewById(R.id.increment_sht);
        this.mSTdecrement = (ImageView) findViewById(R.id.decrement_sht);
        ((FrameLayout) findViewById(R.id.mainscreen_lcd_frame)).setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return MainActivity.this.mGesture.onTouchEvent(event);
            }
        });
        this.mSRswitch = (SyncToggleButton) findViewById(R.id.sr_switch);
        this.mSRswitch.setOnUpdateChangeListener(new OnUpdateChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked, boolean byUser) {
                if (byUser) {
                    String mode;
                    if (isChecked) {
                        mode = "video";
                        MainActivity.this.changeSRstateCache(1);
                    } else {
                        mode = "photo";
                        MainActivity.this.changeSRstateCache(2);
                    }
                    Log.d(MainActivity.TAG, "CameraMode--set camera mode:" + mode);
                    ((Amba2) MainActivity.this.mIPCameraManager).setCameraMode(MainActivity.this.mHttpResponseMessenger, mode);
                    MainActivity.this.showProgressDialog(MainActivity.this.mChangeCameraModeRunnable, 5000);
                }
            }
        });
        setLeftDrawerDefault();
    }

    private void setExposure(int index) {
        switch (index) {
            case 0:
                this.mAutoModeFrame.setVisibility(8);
                this.mManualModeFrame.setVisibility(8);
                this.mWhitBalanceList.setVisibility(0);
                return;
            case 1:
                this.mWhitBalanceList.setVisibility(8);
                if (this.mCameraParams.ae_enable == 0) {
                    this.mAutoModeFrame.setVisibility(8);
                    this.mManualModeFrame.setVisibility(0);
                    return;
                }
                this.mAutoModeFrame.setVisibility(0);
                this.mManualModeFrame.setVisibility(8);
                return;
            default:
                return;
        }
    }

    private void updateDrawerLayout() {
        boolean z;
        boolean z2 = true;
        WbIsoChangedListener wbIsoChangedListener = this.wbIsoChangedListener;
        int indexOf = mWBList.indexOf(Integer.valueOf(this.mCameraParams.white_balance));
        if (this.mCurrentExposureIndex == 0) {
            z = true;
        } else {
            z = false;
        }
        wbIsoChangedListener.onWbModeChanged(indexOf, z);
        WbIsoChangedListener wbIsoChangedListener2 = this.wbIsoChangedListener;
        if (this.mCameraParams.ae_enable != 0) {
            z2 = false;
        }
        wbIsoChangedListener2.onIsoModeChanged(z2);
        this.mWhitBalanceList.setItemSelect(mWBList.indexOf(Integer.valueOf(this.mCameraParams.white_balance)));
        int exIndex = this.mEVListValue.indexOf(this.mCameraParams.exposure_value);
        this.mEVpicker.setValue(exIndex);
        if (exIndex == this.mEVpicker.getMaxValue()) {
            this.mEVdecrement.setVisibility(4);
        } else if (exIndex == this.mEVpicker.getMinValue()) {
            this.mEVincrement.setVisibility(4);
        } else {
            this.mEVincrement.setVisibility(0);
            this.mEVdecrement.setVisibility(0);
        }
        int isoIndex = this.mISOListValue.indexOf(this.mCameraParams.iso);
        this.mISOpicker.setValue(isoIndex);
        if (isoIndex == this.mISOpicker.getMaxValue()) {
            this.mISOdecrement.setVisibility(4);
        } else if (isoIndex == this.mEVpicker.getMinValue()) {
            this.mISOincrement.setVisibility(4);
        } else {
            this.mISOincrement.setVisibility(0);
            this.mISOdecrement.setVisibility(0);
        }
        int timeIndex = this.mSHTimeListValue.indexOf(Integer.toString(this.mCameraParams.shutter_time));
        this.mSTpicker.setValue(timeIndex);
        if (timeIndex == this.mSTpicker.getMaxValue()) {
            this.mSTdecrement.setVisibility(4);
        } else if (timeIndex == this.mSTpicker.getMinValue()) {
            this.mSTincrement.setVisibility(4);
        } else {
            this.mSTincrement.setVisibility(0);
            this.mSTdecrement.setVisibility(0);
        }
        setExposure(this.mCurrentExposureIndex);
    }

    private void showCommunicatingDialog() {
        if (this.mCommunicating == null) {
            this.mCommunicating = ProgressDialog.show(this, null, getResources().getString(R.string.pls_waiting), false, false);
            this.mCommunicating.setCanceledOnTouchOutside(true);
        } else if (!this.mCommunicating.isShowing()) {
            this.mCommunicating.show();
        }
        this.mHandler.postDelayed(this.mCommunicatingRunnable, 5000);
    }

    private void dismissCommunicatingDialog() {
        if (this.mCommunicating != null && this.mCommunicating.isShowing()) {
            this.mCommunicating.dismiss();
            this.mHandler.removeCallbacks(this.mCommunicatingRunnable);
            this.mCommunicating = null;
        }
    }

    private void setLeftDrawerDefault() {
        this.mCurrentExposureIndex = 0;
        this.mCameraParams.ae_enable = 1;
        this.mCameraParams.white_balance = 0;
        this.mCameraParams.exposure_value = "0.0";
        this.mCameraParams.iso = "ISO_600";
        this.mCameraParams.shutter_time = 800;
        updateDrawerLayout();
    }
}

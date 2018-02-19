package com.yuneec.flightmode15;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.yuneec.IPCameraManager.CameraParams;
import com.yuneec.IPCameraManager.IPCameraManager;
import com.yuneec.IPCameraManager.IPCameraManager.RecordStatus;
import com.yuneec.IPCameraManager.IPCameraManager.SDCardStatus;
import com.yuneec.IPCameraManager.IPCameraManager.ToneSetting;
import com.yuneec.flight_settings.FlightSettings;

public class CameraDaemon {
    public static final int CONNECTION = 101;
    private static final boolean DEBUG = true;
    public static final int HTTP_DEAD = 104;
    private static final int HTTP_DEAD_COUNT = 20;
    private static final int RECORD_INTERVAL = 10000;
    public static final int SDSTATUS = 100;
    private static final int SDSTATUS_INTERVAL = 10000;
    public static final int STATUS_CHANGE = 102;
    private static final String TAG = "CameraDaemon";
    public static final int TONE_SETTING = 103;
    public static final int VIDEO_RESOLUTION = 105;
    public static final String WIFI_STATE_INA = "invalid";
    public static final String WIFI_STATE_NONE = "disconnectted";
    public static final String WIFI_STATE_OK = "connectted";
    private static final int WORKING_INTERVAL = 10000;
    private Handler mHandler;
    private int mHttpDeadCounter = 0;
    private boolean mHttpDeadSent = false;
    private WeakHandler<CameraDaemon> mHttpHandler = new WeakHandler<CameraDaemon>(this) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Message toUI;
                    switch (msg.arg1) {
                        case 10:
                            if (msg.obj instanceof SDCardStatus) {
                                toUI = Message.obtain(msg);
                                toUI.what = 100;
                                CameraDaemon.this.mHandler.sendMessage(toUI);
                                CameraDaemon.this.mHttpDeadCounter = 0;
                                return;
                            }
                            Log.i(CameraDaemon.TAG, "request sdcard status:" + msg.obj.toString());
                            checkAndSendHttpDeadMessage();
                            return;
                        case 15:
                            if (msg.obj instanceof ToneSetting) {
                                toUI = Message.obtain(msg);
                                toUI.what = 103;
                                CameraDaemon.this.mHandler.sendMessage(toUI);
                                CameraDaemon.this.mHttpDeadCounter = 0;
                                return;
                            }
                            Log.i(CameraDaemon.TAG, "request tone setting:" + msg.obj.toString());
                            checkAndSendHttpDeadMessage();
                            return;
                        case 21:
                            Log.i(CameraDaemon.TAG, "REQUEST_IS_RECORDING:" + msg.obj);
                            if (msg.obj instanceof RecordStatus) {
                                toUI = Message.obtain(msg);
                                toUI.what = 102;
                                CameraDaemon.this.mHandler.sendMessage(toUI);
                                CameraDaemon.this.mHttpDeadCounter = 0;
                                return;
                            }
                            Log.i(CameraDaemon.TAG, "recording status:" + msg.obj.toString());
                            checkAndSendHttpDeadMessage();
                            return;
                        case 32:
                            if (msg.obj instanceof String) {
                                toUI = Message.obtain(msg);
                                toUI.what = 105;
                                CameraDaemon.this.mHandler.sendMessage(toUI);
                                CameraDaemon.this.mHttpDeadCounter = 0;
                                return;
                            }
                            return;
                        case IPCameraManager.REQUEST_GET_WORK_STATUS /*37*/:
                            Log.i(CameraDaemon.TAG, "REQUEST_GET_WORK_STATUS:" + msg.obj);
                            if ((msg.obj instanceof RecordStatus) || (msg.obj instanceof CameraParams)) {
                                toUI = Message.obtain(msg);
                                toUI.what = 102;
                                CameraDaemon.this.mHandler.sendMessage(toUI);
                                CameraDaemon.this.mHttpDeadCounter = 0;
                                return;
                            }
                            Log.i(CameraDaemon.TAG, "recording status:" + msg.obj.toString());
                            checkAndSendHttpDeadMessage();
                            return;
                        default:
                            Log.i(CameraDaemon.TAG, "response :" + msg.arg1);
                            return;
                    }
                default:
                    return;
            }
        }

        private void checkAndSendHttpDeadMessage() {
        }
    };
    private Messenger mHttpMessenger = new Messenger(this.mHttpHandler);
    private IPCameraManager mIPCameraManager;
    private Runnable mRecordingDaemon = new Runnable() {
        public void run() {
            if (CameraDaemon.this.mIPCameraManager != null) {
                CameraDaemon.this.mIPCameraManager.isRecording(CameraDaemon.this.mHttpMessenger);
                CameraDaemon.this.mHandler.postDelayed(CameraDaemon.this.mRecordingDaemon, 10000);
            }
        }
    };
    private Runnable mSDStausDaemon = new Runnable() {
        public void run() {
            if (CameraDaemon.this.mIPCameraManager != null) {
                CameraDaemon.this.mIPCameraManager.getSDCardStatus(CameraDaemon.this.mHttpMessenger);
                CameraDaemon.this.mHandler.postDelayed(CameraDaemon.this.mSDStausDaemon, 10000);
            }
        }
    };
    private Runnable mWokingDaemon = new Runnable() {
        public void run() {
            if (CameraDaemon.this.mIPCameraManager != null) {
                CameraDaemon.this.mIPCameraManager.getWorkStatus(CameraDaemon.this.mHttpMessenger);
                CameraDaemon.this.mHandler.postDelayed(CameraDaemon.this.mWokingDaemon, 10000);
            }
        }
    };

    public CameraDaemon(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void start(Context context) {
        int camera_type = context.getSharedPreferences(FlightSettings.FLIGHT_SETTINGS_FILE, 0).getInt(FlightSettings.CAMERA_TYPE_VALUE, context.getResources().getInteger(R.integer.def_camera_type_value));
        if ((camera_type & 1) == 1) {
            this.mIPCameraManager = IPCameraManager.getIPCameraManager(context, 101);
        } else if ((camera_type & 4) == 4) {
            this.mIPCameraManager = IPCameraManager.getIPCameraManager(context, 102);
            this.mSDStausDaemon.run();
            this.mRecordingDaemon.run();
            Log.d(TAG, "mSDStausDaemon--start");
        } else if ((camera_type & 8) == 8 || (camera_type & 32) == 32) {
            this.mIPCameraManager = IPCameraManager.getIPCameraManager(context, 104);
            this.mWokingDaemon.run();
            Log.d(TAG, "getworkingdaemon--start");
        } else {
            this.mIPCameraManager = IPCameraManager.getIPCameraManager(context, 100);
        }
        this.mHttpDeadCounter = 0;
        this.mHttpDeadSent = false;
    }

    public void stop(Context context) {
        this.mHandler.removeCallbacks(this.mSDStausDaemon);
        this.mHandler.removeCallbacks(this.mRecordingDaemon);
        this.mHandler.removeCallbacks(this.mWokingDaemon);
        this.mIPCameraManager.finish();
        this.mIPCameraManager = null;
    }

    public void updateSDStatusImediately() {
        if (this.mIPCameraManager != null) {
            this.mSDStausDaemon.run();
        } else {
            Log.i(TAG, "the daemon has not been started yet");
        }
    }
}

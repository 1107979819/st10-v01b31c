package com.yuneec.uartcontroller;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.util.SparseArray;
import com.yuneec.uartcontroller.UARTInfoMessage.BindState;
import com.yuneec.uartcontroller.UARTInfoMessage.CalibrationRawData;
import com.yuneec.uartcontroller.UARTInfoMessage.MissionReply;
import com.yuneec.uartcontroller.UARTInfoMessage.SwitchState;
import com.yuneec.uartcontroller.UARTInfoMessage.TransmitterState;
import com.yuneec.uartcontroller.UARTInfoMessage.UARTRelyMessage;
import com.yuneec.uartcontroller.UARTInfoMessage.Version;
import java.util.Iterator;
import java.util.LinkedList;

public class UARTController {
    public static final int BIND_KEY_FUNCTION_BIND = 1;
    public static final int BIND_KEY_FUNCTION_PWR = 0;
    public static final int STATE_AWAIT = 1;
    public static final int STATE_BIND = 2;
    public static final int STATE_CALIBRATION = 3;
    public static final int STATE_FACTORY_CALI = 7;
    public static final int STATE_RUN = 5;
    public static final int STATE_SIM = 6;
    public static final int SYNC_MIXING_DATA_ADD = 0;
    public static final int SYNC_MIXING_DATA_DELETE = 2;
    public static final int SYNC_MIXING_DATA_DELETE_ALL = 3;
    public static final int SYNC_MIXING_DATA_UPDATE = 1;
    private static final String TAG = "UARTController";
    private static final int TIMEOUT = 300;
    public static final int TTS_START_INDEX = 19;
    private static final int TX_STATE_UPDATE_INTERVAL = 500;
    private static final int TX_STATE_UPDATE_TIMEOUT = 5000;
    private static UARTController sInstance;
    private static String sOccupiedprocess;
    private ChangeState AwaitState = new ChangeState() {
        public boolean exitState(boolean sync) {
            return true;
        }

        public boolean enterState(boolean sync) {
            return true;
        }
    };
    private ChangeState BindState = new ChangeState() {
        public boolean exitState(boolean sync) {
            return UARTController.this.exitBind(sync);
        }

        public boolean enterState(boolean sync) {
            return UARTController.this.enterBind(sync);
        }
    };
    private ChangeState CalibrationState = new ChangeState() {
        public boolean exitState(boolean sync) {
            return UARTController.this.finishCalibration(sync);
        }

        public boolean enterState(boolean sync) {
            return UARTController.this.startCalibration(sync);
        }
    };
    private ChangeState FactoryCalibrationState = new ChangeState() {
        public boolean exitState(boolean sync) {
            return UARTController.this.exitFactoryCalibration(sync);
        }

        public boolean enterState(boolean sync) {
            return UARTController.this.enterFactoryCalibration(sync);
        }
    };
    private ChangeState RunState = new ChangeState() {
        public boolean exitState(boolean sync) {
            return UARTController.this.exitRun(sync);
        }

        public boolean enterState(boolean sync) {
            return UARTController.this.enterRun(sync);
        }
    };
    private ChangeState SimState = new ChangeState() {
        public boolean exitState(boolean sync) {
            return UARTController.this.exitSim(sync);
        }

        public boolean enterState(boolean sync) {
            return UARTController.this.enterSim(sync);
        }
    };
    private UartCommand acquireTxResourceInfoCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.acquireTxResourceInfo();
        }
    };
    private UartCommand bindCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.bind(((Integer) args[0]).intValue());
        }
    };
    private UartCommand enterBindCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.enterBind();
        }
    };
    private UartCommand enterFactoryCalibrationCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.enterFactoryCalibration();
        }
    };
    private UartCommand enterRunCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.enterRun();
        }
    };
    private UartCommand enterSimCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.enterSim();
        }
    };
    private UartCommand enterTransmitTestCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.enterTransmitTest();
        }
    };
    private UartCommand exitBindCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.exitBind();
        }
    };
    private UartCommand exitFactoryCalibrationCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.exitFactoryCalibration();
        }
    };
    private UartCommand exitRunCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.exitRun();
        }
    };
    private UartCommand exitSimCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.exitSim();
        }
    };
    private UartCommand exitTransmitTestCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.exitTransmitTest();
        }
    };
    private UartCommand finishBindCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.finishBind();
        }
    };
    private UartCommand finishCalibrationCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.finishCalibration();
        }
    };
    private UartCommand getSignalCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.getSignal(((Integer) args[0]).intValue());
        }
    };
    private ReaderThread mReader;
    private Handler mReaderHandler;
    private final SparseArray<ChangeState> mStateChangeAction = new SparseArray();
    private StateMachine mStateMachine = new StateMachine();
    private Object mStateMachineLock = new Object();
    private UartCommand missionRequestCommand = new UartCommand() {
        public boolean execute(boolean sync, Object[] args) {
            int result = 1;
            int actionRequest = ((Integer) args[0]).intValue();
            int modeType = ((Integer) args[1]).intValue();
            int settingCount = ((Integer) args[2]).intValue();
            synchronized (UARTController.this.responseMissionQueue) {
                UARTController.this.responseMissionQueue.clear();
            }
            int i = 0;
            while (true) {
                i++;
                if (i > 15) {
                    break;
                }
                UartInterface.sendMissionRequest(actionRequest, modeType, settingCount);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            synchronized (UARTController.this.responseMissionQueue) {
                try {
                    UARTController.this.responseMissionQueue.wait(100);
                } catch (InterruptedException e2) {
                }
                Iterator it = UARTController.this.responseMissionQueue.iterator();
                while (it.hasNext()) {
                    byte[] responseMission = (byte[]) it.next();
                    if (responseMission.length > 3) {
                        Log.i(UARTController.TAG, "missionRequest, receive: " + UARTController.byteArrayToString(responseMission));
                        if ((responseMission[0] & MotionEventCompat.ACTION_MASK) == 1) {
                            result = responseMission[1] & MotionEventCompat.ACTION_MASK;
                            break;
                        }
                    }
                }
                UARTController.this.responseMissionQueue.clear();
            }
            if (result == 0) {
                return true;
            }
            return false;
        }
    };
    private UartCommand missionRequestGetWaypoint = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            WaypointData waypointData = args[0];
            synchronized (UARTController.this.responseMissionQueue) {
                UARTController.this.responseMissionQueue.clear();
            }
            int i = 0;
            while (true) {
                i++;
                if (i > 15) {
                    break;
                }
                UartInterface.sendMissionRequest(5, 3, 0);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            byte[] receiveWaypoint = null;
            synchronized (UARTController.this.responseMissionQueue) {
                try {
                    UARTController.this.responseMissionQueue.wait(100);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                Iterator it = UARTController.this.responseMissionQueue.iterator();
                while (it.hasNext()) {
                    byte[] responseMission = (byte[]) it.next();
                    if (responseMission.length > 24 && (responseMission[1] & MotionEventCompat.ACTION_MASK) == 0) {
                        receiveWaypoint = responseMission;
                        break;
                    }
                }
                UARTController.this.responseMissionQueue.clear();
            }
            if (receiveWaypoint == null) {
                return false;
            }
            waypointData.pointerIndex = receiveWaypoint[2] & MotionEventCompat.ACTION_MASK;
            waypointData.latitude = ((float) LittleEndianUtil.getUInt(receiveWaypoint, 3)) * 1.0E-7f;
            waypointData.longitude = ((float) LittleEndianUtil.getUInt(receiveWaypoint, 7)) * 1.0E-7f;
            waypointData.altitude = ((float) LittleEndianUtil.getUInt(receiveWaypoint, 11)) * 0.01f;
            waypointData.roll = ((float) LittleEndianUtil.getUShort(receiveWaypoint, 15)) * 0.01f;
            waypointData.pitch = ((float) LittleEndianUtil.getUShort(receiveWaypoint, 17)) * 0.01f;
            waypointData.yaw = ((float) LittleEndianUtil.getUShort(receiveWaypoint, 19)) * 0.01f;
            waypointData.gimbalPitch = ((float) LittleEndianUtil.getUShort(receiveWaypoint, 21)) * 0.01f;
            waypointData.gimbalYam = ((float) LittleEndianUtil.getUShort(receiveWaypoint, 23)) * 0.01f;
            return true;
        }
    };
    private UartCommand missionSettingCccCommand = new UartCommand() {
        public boolean execute(boolean sync, Object[] args) {
            int result = 1;
            WaypointData waypointData = args[0];
            synchronized (UARTController.this.responseMissionQueue) {
                UARTController.this.responseMissionQueue.clear();
            }
            int i = 0;
            while (true) {
                i++;
                if (i > 15) {
                    break;
                }
                UartInterface.sendMissionSettingCccWaypoint(waypointData);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            synchronized (UARTController.this.responseMissionQueue) {
                try {
                    UARTController.this.responseMissionQueue.wait(100);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                Iterator it = UARTController.this.responseMissionQueue.iterator();
                while (it.hasNext()) {
                    byte[] responseMission = (byte[]) it.next();
                    if (responseMission != null && responseMission.length > 3) {
                        result = responseMission[1] & MotionEventCompat.ACTION_MASK;
                        break;
                    }
                }
                UARTController.this.responseMissionQueue.clear();
            }
            if (result == 0) {
                return true;
            }
            return false;
        }
    };
    private UartCommand missionSettingRoiCenterCommand = new UartCommand() {
        public boolean execute(boolean sync, Object[] args) {
            int result = 1;
            RoiData roiData = args[0];
            synchronized (UARTController.this.responseMissionQueue) {
                UARTController.this.responseMissionQueue.clear();
            }
            int i = 0;
            while (true) {
                i++;
                if (i > 15) {
                    break;
                }
                UartInterface.sendMissionSettingRoiCenter(roiData);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            synchronized (UARTController.this.responseMissionQueue) {
                try {
                    UARTController.this.responseMissionQueue.wait(100);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
                Iterator it = UARTController.this.responseMissionQueue.iterator();
                while (it.hasNext()) {
                    byte[] responseMission = (byte[]) it.next();
                    if (responseMission != null && responseMission.length > 3) {
                        result = responseMission[1] & MotionEventCompat.ACTION_MASK;
                        break;
                    }
                }
                UARTController.this.responseMissionQueue.clear();
            }
            if (result == 0) {
                return true;
            }
            return false;
        }
    };
    private UartCommand receiveBothChannelCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.receiveBothChannel();
        }
    };
    private UartCommand receiveMixedChannelOnlyCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.receiveMixedChannelOnly();
        }
    };
    private UartCommand receiveRawChannelOnlyCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.receiveRawChannelOnly();
        }
    };
    private volatile LinkedList<byte[]> responseMissionQueue = new LinkedList();
    private volatile int responseOfBindState = 0;
    private volatile String responseOfRadioVersion = null;
    private volatile SwitchState responseOfSwitchState = null;
    private volatile String responseOfTxBLVersion = null;
    private volatile String responseOfTxVersion = null;
    private volatile boolean responseReached = false;
    private UartCommand setBindKeyFunctionCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.setBindKeyFunction(((Integer) args[0]).intValue());
        }
    };
    private UartCommand setChannelConfigCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.setChannelConfig(((Integer) args[0]).intValue(), ((Integer) args[1]).intValue());
        }
    };
    private UartCommand setFmodeKeyCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.setFmodeKey(((Integer) args[0]).intValue());
        }
    };
    private UartCommand setSubTrimCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.setSubTrim(args[0]);
        }
    };
    private UartCommand setTTBStateCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.setTTBstate(((Integer) args[0]).intValue(), ((Boolean) args[1]).booleanValue());
        }
    };
    private UartCommand setTrimStepCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.setTrimStep(((Integer) args[0]).intValue());
        }
    };
    private UartCommand shutDownCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.shutDown();
        }
    };
    private UartCommand sonarSwitchCommand = new UartCommand() {
        public boolean execute(boolean sync, Object[] args) {
            boolean isOn = ((Boolean) args[0]).booleanValue();
            int i = 0;
            while (true) {
                i++;
                if (i > 15) {
                    return false;
                }
                UartInterface.sonarSwitch(isOn);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private UartCommand startBindCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.startBind();
        }
    };
    private UartCommand startCalibrationCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.startCalibration();
        }
    };
    private UartCommand syncMixingDataCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.syncMixingData(args[0], ((Integer) args[1]).intValue());
        }
    };
    private UartCommand syncMixingDataDeleteAllCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.syncMixingDataDeleteAll();
        }
    };
    private UartCommand unBindCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.unbind();
        }
    };
    private UartCommand updateCompassCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.updateCompass(((Float) args[0]).floatValue());
        }
    };
    private UartCommand updateGpsCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.updateGPS(((Float) args[0]).floatValue(), ((Float) args[1]).floatValue(), ((Float) args[2]).floatValue(), ((Float) args[3]).floatValue(), ((Float) args[4]).floatValue(), ((Float) args[5]).floatValue(), ((Integer) args[6]).intValue());
        }
    };
    private UartCommand writeTransmitRateCmd = new UartCommand() {
        public boolean execute(boolean sync, Object... args) {
            return UartInterface.writeTransmitRate(((Integer) args[0]).intValue());
        }
    };

    public interface ChangeState {
        boolean enterState(boolean z);

        boolean exitState(boolean z);
    }

    private class ReaderThread extends Thread {
        private boolean mRunning;

        private ReaderThread() {
            this.mRunning = false;
        }

        public void run() {
            setName("UART Reader");
            while (this.mRunning) {
                handleUartMsg(readUartCommand2());
            }
        }

        private void notifySender(UARTInfoMessage umsg) {
            if (umsg != null && UARTController.this.mReaderHandler != null) {
                Message msg = new Message();
                msg.obj = umsg;
                UARTController.this.mReaderHandler.sendMessage(msg);
            }
        }

        private UARTInfoMessage readUartCommand2() {
            return UartInterface.RecvMsg();
        }

        private void handleUartMsg(UARTInfoMessage umsg) {
            if (umsg != null) {
                if (umsg instanceof UARTRelyMessage) {
                    if (umsg.what == UARTRelyMessage.REPLY_BIND_INFO) {
                        notifySender(umsg);
                        return;
                    }
                    synchronized (UARTController.this) {
                        UARTController.this.responseReached = true;
                        UARTController.this.notifyAll();
                    }
                } else if (umsg.what == 12) {
                    synchronized (UARTController.this) {
                        UARTController.this.responseOfBindState = ((BindState) umsg).state;
                        UARTController.this.notifyAll();
                    }
                } else if (umsg.what == 15) {
                    synchronized (UARTController.this) {
                        UARTController.this.responseOfSwitchState = (SwitchState) umsg;
                        UARTController.this.notifyAll();
                    }
                } else if (umsg.what == 1) {
                    TransmitterState tx = (TransmitterState) umsg;
                    synchronized (UARTController.this.mStateMachineLock) {
                        UARTController.this.mStateMachine.timestamp = SystemClock.elapsedRealtime();
                        UARTController.this.mStateMachine.mState = tx.status;
                        Log.d(UARTController.TAG, "------ status ------ : " + tx.status);
                    }
                } else if (umsg.what == 16) {
                    version = (Version) umsg;
                    synchronized (UARTController.this) {
                        UARTController.this.responseOfTxVersion = version.version;
                        UARTController.this.notifyAll();
                    }
                } else if (umsg.what == 17) {
                    version = (Version) umsg;
                    synchronized (UARTController.this) {
                        UARTController.this.responseOfRadioVersion = version.version;
                        UARTController.this.notifyAll();
                    }
                } else if (umsg.what == 21) {
                    version = (Version) umsg;
                    synchronized (UARTController.this) {
                        UARTController.this.responseOfTxBLVersion = version.version;
                        UARTController.this.notifyAll();
                    }
                } else if (umsg.what == 23) {
                    MissionReply missionReply = (MissionReply) umsg;
                    if ((missionReply.replyInfo[0] & MotionEventCompat.ACTION_MASK) == 2) {
                        UartInterface.sendMissionResponse(0, missionReply.replyInfo[2] & MotionEventCompat.ACTION_MASK, 0);
                        notifySender(umsg);
                        return;
                    }
                    synchronized (UARTController.this.responseMissionQueue) {
                        UARTController.this.responseMissionQueue.addLast(missionReply.replyInfo);
                        UARTController.this.responseMissionQueue.notifyAll();
                    }
                } else {
                    notifySender(umsg);
                }
            }
        }
    }

    private static class StateMachine {
        public int mState;
        public long timestamp;

        private StateMachine() {
        }
    }

    public interface UartCommand {
        boolean execute(boolean z, Object... objArr);
    }

    public boolean startReading() {
        if (this.mReader == null || !this.mReader.mRunning) {
            if (this.mReader == null) {
                this.mReader = new ReaderThread();
            }
            UartInterface.clearRecvBuf();
            this.mReader.mRunning = true;
            this.mReader.start();
            return true;
        }
        Log.w(TAG, "UART Reader is already running");
        return false;
    }

    public void stopReading() {
        if (this.mReader == null || !this.mReader.mRunning) {
            Log.w(TAG, "UART Reader is already stopped");
            return;
        }
        if (this.mReader != null) {
            this.mReader.mRunning = false;
            this.mReader.interrupt();
            this.mReader = null;
        }
        UartInterface.clearRecvBuf();
    }

    public boolean isReading() {
        if (this.mReader == null) {
            return false;
        }
        return this.mReader.mRunning;
    }

    public void registerReaderHandler(Handler handler) {
        if (!(handler == null || this.mReaderHandler == null)) {
            Log.i(TAG, "previous Handler " + this.mReaderHandler.toString() + "will no longer receive UART Info");
        }
        this.mReaderHandler = handler;
    }

    private UARTController() {
        initStateChangeActionMaps();
        UartInterface.nativeInit();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static com.yuneec.uartcontroller.UARTController getInstance() {
        /*
        r0 = 0;
        r1 = com.yuneec.uartcontroller.UARTController.class;
        monitor-enter(r1);
        r2 = sInstance;	 Catch:{ all -> 0x0025 }
        if (r2 != 0) goto L_0x0018;
    L_0x0008:
        r2 = 0;
        sOccupiedprocess = r2;	 Catch:{ all -> 0x0025 }
        r2 = ensureUartInterface();	 Catch:{ all -> 0x0025 }
        if (r2 == 0) goto L_0x001c;
    L_0x0011:
        r0 = new com.yuneec.uartcontroller.UARTController;	 Catch:{ all -> 0x0025 }
        r0.<init>();	 Catch:{ all -> 0x0025 }
        sInstance = r0;	 Catch:{ all -> 0x0025 }
    L_0x0018:
        monitor-exit(r1);	 Catch:{ all -> 0x0025 }
        r0 = sInstance;
    L_0x001b:
        return r0;
    L_0x001c:
        r2 = "UARTController";
        r3 = "can not get UARTController instance,make sure UARTController were not occupied by other apps";
        android.util.Log.e(r2, r3);	 Catch:{ all -> 0x0025 }
        monitor-exit(r1);	 Catch:{ all -> 0x0025 }
        goto L_0x001b;
    L_0x0025:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0025 }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.yuneec.uartcontroller.UARTController.getInstance():com.yuneec.uartcontroller.UARTController");
    }

    private static boolean ensureUartInterface() {
        String jniResult = UartInterface.openDevice();
        if ("OK".equals(jniResult)) {
            return true;
        }
        if (!"FAIL".equals(jniResult)) {
            Log.i(TAG, "occupied process " + jniResult);
            sOccupiedprocess = jniResult;
        }
        return false;
    }

    public static String getOccupiedprocess() {
        return sOccupiedprocess;
    }

    public static CharSequence getPackagenameByProcess(Context context) {
        PackageManager pm = context.getPackageManager();
        CharSequence package_name = null;
        String process_name = getOccupiedprocess();
        try {
            package_name = pm.getApplicationLabel(pm.getApplicationInfo(process_name, 0));
        } catch (NameNotFoundException e) {
            Log.e(TAG, "cannot get the procee package name:" + process_name);
        }
        if (package_name == null) {
            return process_name;
        }
        return package_name;
    }

    public void destory() {
        stopReading();
        registerReaderHandler(null);
        UartInterface.closeDevice();
        sInstance = null;
    }

    public boolean acquireTxResourceInfo(boolean sync) {
        return sendCommandInternal(this.acquireTxResourceInfoCmd, 1, sync, new Object[0]);
    }

    public void clearRadioInfo() {
        UartInterface.nativeDestory();
    }

    public void readyForUpdateTxVersion() {
        UartInterface.readyForUpdateTxVersion();
    }

    public void readyForUpdateRfVersion() {
        UartInterface.readyForUpdateRfVersion();
    }

    public void UpdateTxVersionCompleted() {
        UartInterface.UpdateTxVersionCompleted();
    }

    public void UpdateRfVersionCompleted() {
        UartInterface.UpdateRfVersionCompleted();
    }

    public String updateTxVersion(String path) {
        return UartInterface.updateTxVersion(path);
    }

    public String updateRfVersion(String path) {
        return UartInterface.updateRfVersion(path);
    }

    public boolean startBind(boolean sync) {
        return sendCommandInternal(this.startBindCmd, 1, sync, new Object[0]);
    }

    public boolean finishBind(boolean sync) {
        return sendCommandInternal(this.finishBindCmd, 1, sync, new Object[0]);
    }

    public boolean bind(boolean sync, int RxAddr) {
        return sendCommandInternal(this.bindCmd, 1, sync, Integer.valueOf(RxAddr));
    }

    public boolean unbind(boolean sync) {
        return sendCommandInternal(this.unBindCmd, 1, sync, new Object[0]);
    }

    public boolean startBind(boolean sync, int retry_times) {
        return sendCommandInternal(this.startBindCmd, retry_times, sync, new Object[0]);
    }

    public boolean finishBind(boolean sync, int retry_times) {
        return sendCommandInternal(this.finishBindCmd, retry_times, sync, new Object[0]);
    }

    public boolean bind(boolean sync, int RxAddr, int retry_times) {
        return sendCommandInternal(this.bindCmd, retry_times, sync, Integer.valueOf(RxAddr));
    }

    public boolean unbind(boolean sync, int retry_times) {
        return sendCommandInternal(this.unBindCmd, retry_times, sync, new Object[0]);
    }

    public int queryBindState() {
        this.responseOfBindState = -1;
        if (UartInterface.queryBindState()) {
            return waitBindState();
        }
        return -1;
    }

    public int querySwitchState(int hw_id) {
        int state = -1;
        if (UartInterface.querySwitchState(hw_id)) {
            synchronized (this) {
                this.responseOfSwitchState = null;
                try {
                    wait(300);
                    if (this.responseOfSwitchState != null && hw_id == this.responseOfSwitchState.hw_id) {
                        state = this.responseOfSwitchState.state;
                    }
                } catch (InterruptedException e) {
                }
            }
        }
        return state;
    }

    public String getTransmitterVersion() {
        String version = "Native Error";
        if (UartInterface.getTxVersion()) {
            synchronized (this) {
                this.responseOfTxVersion = null;
                try {
                    wait(300);
                    if (this.responseOfTxVersion == null) {
                        version = "Transmitter not response";
                    } else {
                        version = this.responseOfTxVersion;
                    }
                } catch (InterruptedException e) {
                }
            }
        }
        return version;
    }

    public String getRadioVersion() {
        String version = "Native Error";
        if (UartInterface.getRfVersion()) {
            synchronized (this) {
                this.responseOfRadioVersion = null;
                try {
                    wait(300);
                    if (this.responseOfRadioVersion == null) {
                        version = "Transmitter not response";
                    } else {
                        version = this.responseOfRadioVersion;
                    }
                } catch (InterruptedException e) {
                }
            }
        }
        return version;
    }

    public String getTxBootloaderVersion() {
        String version = "Native Error";
        if (UartInterface.getTxBLVersion()) {
            synchronized (this) {
                this.responseOfTxBLVersion = null;
                try {
                    wait(300);
                    if (this.responseOfTxBLVersion == null) {
                        version = "Transmitter not response";
                    } else {
                        version = this.responseOfTxBLVersion;
                    }
                } catch (InterruptedException e) {
                }
            }
        }
        return version;
    }

    public int getStateMachine() {
        return this.mStateMachine.mState;
    }

    public boolean correctTxState(int expected_state, int retry_times) {
        while (retry_times > 0) {
            if (correctTxState(expected_state)) {
                return true;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
            retry_times--;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean correctTxState(int r13) {
        /*
        r12 = this;
        r5 = 0;
        r6 = 1;
        r4 = r12.mStateChangeAction;
        r0 = r4.get(r13);
        r0 = (com.yuneec.uartcontroller.UARTController.ChangeState) r0;
        if (r0 != 0) goto L_0x0022;
    L_0x000c:
        r4 = "UARTController";
        r6 = new java.lang.StringBuilder;
        r7 = "Can not change to a unknown state :";
        r6.<init>(r7);
        r6 = r6.append(r13);
        r6 = r6.toString();
        android.util.Log.e(r4, r6);
        r4 = r5;
    L_0x0021:
        return r4;
    L_0x0022:
        r1 = 0;
        r7 = r12.mStateMachineLock;
        monitor-enter(r7);
        r2 = android.os.SystemClock.elapsedRealtime();	 Catch:{ all -> 0x009b }
        r4 = r12.mStateMachine;	 Catch:{ all -> 0x009b }
        r8 = r4.timestamp;	 Catch:{ all -> 0x009b }
        r8 = r2 - r8;
        r10 = 5000; // 0x1388 float:7.006E-42 double:2.4703E-320;
        r4 = (r8 > r10 ? 1 : (r8 == r10 ? 0 : -1));
        if (r4 <= 0) goto L_0x005b;
    L_0x0036:
        r4 = "UARTController";
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x009b }
        r8 = "Tx has not updated its state since ";
        r6.<init>(r8);	 Catch:{ all -> 0x009b }
        r8 = r12.mStateMachine;	 Catch:{ all -> 0x009b }
        r8 = r8.timestamp;	 Catch:{ all -> 0x009b }
        r6 = r6.append(r8);	 Catch:{ all -> 0x009b }
        r8 = " now is ";
        r6 = r6.append(r8);	 Catch:{ all -> 0x009b }
        r6 = r6.append(r2);	 Catch:{ all -> 0x009b }
        r6 = r6.toString();	 Catch:{ all -> 0x009b }
        android.util.Log.e(r4, r6);	 Catch:{ all -> 0x009b }
        monitor-exit(r7);	 Catch:{ all -> 0x009b }
        r4 = r5;
        goto L_0x0021;
    L_0x005b:
        r4 = r12.mStateMachine;	 Catch:{ all -> 0x009b }
        r4 = r4.mState;	 Catch:{ all -> 0x009b }
        if (r13 != r4) goto L_0x0078;
    L_0x0061:
        r4 = "UARTController";
        r5 = new java.lang.StringBuilder;	 Catch:{ all -> 0x009b }
        r8 = "Tx already in state ";
        r5.<init>(r8);	 Catch:{ all -> 0x009b }
        r5 = r5.append(r13);	 Catch:{ all -> 0x009b }
        r5 = r5.toString();	 Catch:{ all -> 0x009b }
        android.util.Log.i(r4, r5);	 Catch:{ all -> 0x009b }
        monitor-exit(r7);	 Catch:{ all -> 0x009b }
        r4 = r6;
        goto L_0x0021;
    L_0x0078:
        r4 = r12.mStateChangeAction;	 Catch:{ all -> 0x009b }
        r8 = r12.mStateMachine;	 Catch:{ all -> 0x009b }
        r8 = r8.mState;	 Catch:{ all -> 0x009b }
        r4 = r4.get(r8);	 Catch:{ all -> 0x009b }
        r4 = (com.yuneec.uartcontroller.UARTController.ChangeState) r4;	 Catch:{ all -> 0x009b }
        r8 = 1;
        r1 = r4.exitState(r8);	 Catch:{ all -> 0x009b }
        monitor-exit(r7);	 Catch:{ all -> 0x009b }
        if (r1 == 0) goto L_0x00bd;
    L_0x008c:
        r4 = r0.enterState(r6);
        if (r4 == 0) goto L_0x009e;
    L_0x0092:
        r4 = "UARTController";
        r5 = "correct state success";
        android.util.Log.i(r4, r5);
        r4 = r6;
        goto L_0x0021;
    L_0x009b:
        r4 = move-exception;
        monitor-exit(r7);	 Catch:{ all -> 0x009b }
        throw r4;
    L_0x009e:
        r8 = 500; // 0x1f4 float:7.0E-43 double:2.47E-321;
        java.lang.Thread.sleep(r8);	 Catch:{ InterruptedException -> 0x00c7 }
    L_0x00a3:
        r4 = r12.mStateMachine;
        r4 = r4.mState;
        if (r13 != r4) goto L_0x00b3;
    L_0x00a9:
        r4 = "UARTController";
        r5 = "correct state success";
        android.util.Log.i(r4, r5);
        r4 = r6;
        goto L_0x0021;
    L_0x00b3:
        r4 = "UARTController";
        r6 = "Correct Tx State fail,Success return to await,but fail to enter to expected state";
        android.util.Log.w(r4, r6);
        r4 = r5;
        goto L_0x0021;
    L_0x00bd:
        r4 = "UARTController";
        r6 = "Correct Tx State fail, fail to return to await";
        android.util.Log.w(r4, r6);
        r4 = r5;
        goto L_0x0021;
    L_0x00c7:
        r4 = move-exception;
        goto L_0x00a3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.yuneec.uartcontroller.UARTController.correctTxState(int):boolean");
    }

    private int waitBindState() {
        int state;
        synchronized (this) {
            state = -1;
            try {
                wait(300);
                state = this.responseOfBindState;
            } catch (InterruptedException e) {
            }
        }
        return state;
    }

    private boolean waitResponse() {
        boolean result;
        synchronized (this) {
            result = false;
            try {
                wait(300);
                result = this.responseReached;
            } catch (InterruptedException e) {
            }
            Log.d(TAG, "waitResponse: " + result);
        }
        return result;
    }

    public boolean startCalibration(boolean sync) {
        return sendCommandInternal(this.startCalibrationCmd, 1, sync, new Object[0]);
    }

    public boolean finishCalibration(boolean sync) {
        return sendCommandInternal(this.finishCalibrationCmd, 1, sync, new Object[0]);
    }

    public boolean enterFactoryCalibration(boolean sync) {
        return sendCommandInternal(this.enterFactoryCalibrationCmd, 1, sync, new Object[0]);
    }

    public boolean exitFactoryCalibration(boolean sync) {
        return sendCommandInternal(this.exitFactoryCalibrationCmd, 1, sync, new Object[0]);
    }

    public boolean enterRun(boolean sync) {
        return sendCommandInternal(this.enterRunCmd, 1, sync, new Object[0]);
    }

    public boolean exitRun(boolean sync) {
        return sendCommandInternal(this.exitRunCmd, 1, sync, new Object[0]);
    }

    public boolean enterSim(boolean sync) {
        return sendCommandInternal(this.enterSimCmd, 1, sync, new Object[0]);
    }

    public boolean exitSim(boolean sync) {
        return sendCommandInternal(this.exitSimCmd, 1, sync, new Object[0]);
    }

    public boolean enterBind(boolean sync) {
        return sendCommandInternal(this.enterBindCmd, 1, sync, new Object[0]);
    }

    public boolean exitBind(boolean sync) {
        return sendCommandInternal(this.exitBindCmd, 1, sync, new Object[0]);
    }

    public boolean receiveRawChannelOnly(boolean sync) {
        return sendCommandInternal(this.receiveRawChannelOnlyCmd, 1, sync, new Object[0]);
    }

    public boolean receiveRawChannelOnly(boolean sync, int retry_times) {
        return sendCommandInternal(this.receiveRawChannelOnlyCmd, retry_times, sync, new Object[0]);
    }

    public boolean receiveMixedChannelOnly(boolean sync) {
        return sendCommandInternal(this.receiveMixedChannelOnlyCmd, 1, sync, new Object[0]);
    }

    public boolean receiveMixedChannelOnly(boolean sync, int retry_times) {
        return sendCommandInternal(this.receiveMixedChannelOnlyCmd, retry_times, sync, new Object[0]);
    }

    public boolean receiveBothChannel(boolean sync) {
        return sendCommandInternal(this.receiveBothChannelCmd, 1, sync, new Object[0]);
    }

    public boolean receiveBothChannel(boolean sync, int retry_times) {
        return sendCommandInternal(this.receiveBothChannelCmd, retry_times, sync, new Object[0]);
    }

    public boolean syncMixingData(boolean sync, MixedData data, int opt) {
        return sendCommandInternal(this.syncMixingDataCmd, 1, sync, data, Integer.valueOf(opt));
    }

    public boolean syncMixingData(boolean sync, MixedData data, int opt, int retry_times) {
        return sendCommandInternal(this.syncMixingDataCmd, retry_times, sync, data, Integer.valueOf(opt));
    }

    public boolean syncMixingDataDeleteAll(boolean sync) {
        return sendCommandInternal(this.syncMixingDataDeleteAllCmd, 1, sync, new Object[0]);
    }

    public boolean syncMixingDataDeleteAll(boolean sync, int retry_times) {
        return sendCommandInternal(this.syncMixingDataDeleteAllCmd, retry_times, sync, new Object[0]);
    }

    public boolean setFmodeKey(boolean sync, int key) {
        return sendCommandInternal(this.setFmodeKeyCmd, 1, sync, Integer.valueOf(key));
    }

    public boolean setFmodeKey(boolean sync, int key, int retry_times) {
        return sendCommandInternal(this.setFmodeKeyCmd, retry_times, sync, Integer.valueOf(key));
    }

    public boolean setBindKeyFunction(boolean sync, int func) {
        return sendCommandInternal(this.setBindKeyFunctionCmd, 1, sync, Integer.valueOf(func));
    }

    public boolean setBindKeyFunction(boolean sync, int func, int retry_times) {
        return sendCommandInternal(this.setBindKeyFunctionCmd, retry_times, sync, Integer.valueOf(func));
    }

    public boolean setTTBState(boolean sync, int index, boolean on_off) {
        return sendCommandInternal(this.setTTBStateCmd, 1, sync, Integer.valueOf(index), Boolean.valueOf(on_off));
    }

    public boolean setTTBState(boolean sync, int index, boolean on_off, int retry_times) {
        return sendCommandInternal(this.setTTBStateCmd, retry_times, sync, Integer.valueOf(index), Boolean.valueOf(on_off));
    }

    public boolean setChannelConfig(boolean sync, int analog_num, int switch_num) {
        return sendCommandInternal(this.setChannelConfigCmd, 1, sync, Integer.valueOf(analog_num), Integer.valueOf(switch_num));
    }

    public boolean setChannelConfig(boolean sync, int analog_num, int switch_num, int retry_times) {
        return sendCommandInternal(this.setChannelConfigCmd, retry_times, sync, Integer.valueOf(analog_num), Integer.valueOf(switch_num));
    }

    public boolean updateGps(boolean sync, GPSUpLinkData data) {
        return sendCommandInternal(this.updateGpsCmd, 1, sync, Float.valueOf(data.lat), Float.valueOf(data.lon), Float.valueOf(data.alt), Float.valueOf(data.accuracy), Float.valueOf(data.speed), Float.valueOf(data.angle), Integer.valueOf(data.no_satelites));
    }

    public boolean updateGps(boolean sync, GPSUpLinkData data, int retry_times) {
        return sendCommandInternal(this.updateGpsCmd, retry_times, sync, Float.valueOf(data.lat), Float.valueOf(data.lon), Float.valueOf(data.alt), Float.valueOf(data.accuracy), Float.valueOf(data.speed), Float.valueOf(data.angle), Integer.valueOf(data.no_satelites));
    }

    public boolean updateCompass(boolean sync, float angle) {
        return sendCommandInternal(this.updateCompassCmd, 1, sync, Float.valueOf(angle));
    }

    public boolean updateCompass(boolean sync, float angle, int retry_times) {
        return sendCommandInternal(this.updateCompassCmd, retry_times, sync, Float.valueOf(angle));
    }

    public boolean setTrimStep(boolean sync, int step) {
        return sendCommandInternal(this.setTrimStepCmd, 1, sync, Integer.valueOf(step));
    }

    public boolean setTrimStep(boolean sync, int step, int retry_times) {
        return sendCommandInternal(this.setTrimStepCmd, retry_times, sync, Integer.valueOf(step));
    }

    public int getTrimStep() {
        return UartInterface.getTrimStep(false);
    }

    public boolean setSubTrim(boolean sync, int[] sub_value) {
        return sendCommandInternal(this.setSubTrimCmd, 1, sync, sub_value);
    }

    public boolean setSubTrim(boolean sync, int[] sub_value, int retry_times) {
        return sendCommandInternal(this.setSubTrimCmd, retry_times, sync, sub_value);
    }

    public boolean getSubTrim(int[] sub_value) {
        return UartInterface.getSubTrim(sub_value, false);
    }

    public CalibrationRawData getCalibrationRawData(boolean sync) {
        return UartInterface.getCalibrationRawData(sync);
    }

    public boolean shutDown(boolean sync) {
        return sendCommandInternal(this.shutDownCmd, 1, sync, new Object[0]);
    }

    public boolean PCenterTestRF(int value, int mode) {
        return UartInterface.enterTestRF(value, mode);
    }

    public boolean enterTransmitTest(boolean sync) {
        return sendCommandInternal(this.enterTransmitTestCmd, 1, sync, new Object[0]);
    }

    public boolean exitTransmitTest(boolean sync) {
        return sendCommandInternal(this.exitTransmitTestCmd, 1, sync, new Object[0]);
    }

    public boolean writeTransmitRate(int value) {
        return sendCommandInternal(this.writeTransmitRateCmd, 3, false, Integer.valueOf(value));
    }

    public int readTransmitRate() {
        int retry_times = 3;
        int rate = -1;
        while (retry_times > 0) {
            rate = UartInterface.readTransmitRate();
            if (rate != -1) {
                return rate;
            }
            retry_times--;
            if (retry_times != 0) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
        }
        return rate;
    }

    public boolean getSignal(boolean sync, int value) {
        return sendCommandInternal(this.getSignalCmd, 3, sync, Integer.valueOf(value));
    }

    private boolean sendCommandInternal(UartCommand cmd, int retry_times, boolean sync, Object... args) {
        while (retry_times > 0) {
            if (sendCommandInternal(cmd, sync, args)) {
                return true;
            }
            retry_times--;
            if (retry_times != 0) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            }
        }
        return false;
    }

    private boolean sendCommandInternal(UartCommand cmd, boolean sync, Object... args) {
        this.responseReached = false;
        boolean result = cmd.execute(sync, args);
        if (result && sync) {
            return waitResponse();
        }
        return result;
    }

    public byte[] getRxResInfo(int RxAddr) {
        return UartInterface.getRxResInfo(RxAddr);
    }

    public boolean sendRxResInfo(byte[] buf) {
        return UartInterface.sendRxResInfo(buf);
    }

    private void initStateChangeActionMaps() {
        this.mStateChangeAction.put(1, this.AwaitState);
        this.mStateChangeAction.put(2, this.BindState);
        this.mStateChangeAction.put(3, this.CalibrationState);
        this.mStateChangeAction.put(5, this.RunState);
        this.mStateChangeAction.put(6, this.SimState);
        this.mStateChangeAction.put(7, this.FactoryCalibrationState);
    }

    public static String byteArrayToString(byte[] byteValue) {
        StringBuilder s = new StringBuilder();
        int length = byteValue.length;
        for (int i = 0; i < length; i++) {
            s.append(String.format(" %02x", new Object[]{Byte.valueOf(byteValue[i])}));
        }
        return s.toString();
    }

    public boolean sendMissionRequest(int actionRequest, int modeType, int settingCount) {
        return sendCommandInternal(this.missionRequestCommand, 3, false, Integer.valueOf(actionRequest), Integer.valueOf(modeType), Integer.valueOf(settingCount));
    }

    public boolean sendMissionRequestGetWaypoint(WaypointData waypointData) {
        return sendCommandInternal(this.missionRequestGetWaypoint, 3, false, waypointData);
    }

    public boolean sendMissionSettingCCC(WaypointData waypointData) {
        return sendCommandInternal(this.missionSettingCccCommand, 6, false, waypointData);
    }

    public boolean sendMissionSettingRoiCenter(RoiData roiData) {
        return sendCommandInternal(this.missionSettingRoiCenterCommand, 3, false, roiData);
    }

    public boolean sonarSwitch(boolean isOn) {
        sendCommandInternal(this.sonarSwitchCommand, 3, false, Boolean.valueOf(isOn));
        return true;
    }
}

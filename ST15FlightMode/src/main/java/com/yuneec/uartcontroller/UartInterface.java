package com.yuneec.uartcontroller;

import com.yuneec.uartcontroller.UARTInfoMessage.CalibrationRawData;

public class UartInterface {
    static native int Recv(byte[] bArr, int i);

    public static native UARTInfoMessage RecvMsg();

    static native int Send(byte[] bArr, int i);

    static native void UpdateRfVersionCompleted();

    static native void UpdateTxVersionCompleted();

    static native boolean acquireTxResourceInfo();

    static native boolean bind(int i);

    static native void clearRecvBuf();

    static native boolean closeDevice();

    static native boolean enterBind();

    static native boolean enterFactoryCalibration();

    static native boolean enterRun();

    static native boolean enterSim();

    static native boolean enterTestRF(int i, int i2);

    static native boolean enterTransmitTest();

    static native boolean exitBind();

    static native boolean exitFactoryCalibration();

    static native boolean exitRun();

    static native boolean exitSim();

    static native boolean exitTransmitTest();

    static native boolean finishBind();

    static native boolean finishCalibration();

    static native CalibrationRawData getCalibrationRawData(boolean z);

    static native boolean getRfVersion();

    static native byte[] getRxResInfo(int i);

    static native boolean getSignal(int i);

    static native boolean getSubTrim(int[] iArr, boolean z);

    static native int getTrimStep(boolean z);

    static native boolean getTxBLVersion();

    static native boolean getTxVersion();

    static native boolean isOpenDevice();

    static native void nativeDestory();

    static native void nativeInit();

    static native String openDevice();

    static native UARTInfoMessage parseMsg(byte[] bArr, int i);

    static native boolean queryBindState();

    static native boolean querySwitchState(int i);

    static native int readTransmitRate();

    static native void readyForUpdateRfVersion();

    static native void readyForUpdateTxVersion();

    static native boolean receiveBothChannel();

    static native boolean receiveMixedChannelOnly();

    static native boolean receiveRawChannelOnly();

    static native boolean sendMissionRequest(int i, int i2, int i3);

    static native boolean sendMissionResponse(int i, int i2, int i3);

    static native boolean sendMissionSettingCccWaypoint(WaypointData waypointData);

    static native boolean sendMissionSettingRoiCenter(RoiData roiData);

    static native boolean sendRxResInfo(byte[] bArr);

    static native boolean setBindKeyFunction(int i);

    static native boolean setChannelConfig(int i, int i2);

    static native boolean setFmodeKey(int i);

    static native boolean setSubTrim(int[] iArr);

    static native boolean setTTBstate(int i, boolean z);

    static native boolean setTrimStep(int i);

    static native boolean shutDown();

    static native boolean sonarSwitch(boolean z);

    static native boolean startBind();

    static native boolean startCalibration();

    static native boolean syncMixingData(MixedData mixedData, int i);

    static native boolean syncMixingDataDeleteAll();

    public static native boolean testBit();

    static native boolean unbind();

    static native boolean updateCompass(float f);

    static native boolean updateGPS(float f, float f2, float f3, float f4, float f5, float f6, int i);

    static native String updateRfVersion(String str);

    static native String updateTxVersion(String str);

    static native boolean writeTransmitRate(int i);

    static {
        System.loadLibrary("flycontroljni");
    }
}

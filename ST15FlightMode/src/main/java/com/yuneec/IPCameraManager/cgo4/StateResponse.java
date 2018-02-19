package com.yuneec.IPCameraManager.cgo4;

public class StateResponse extends ResponseResult {
    public double batteryCapacity;
    public boolean isRecording;
    public boolean isSdcardInserted;
    public int phtotoCapacity;
    public int recordedTime;
    public int videoCapacity;

    public StateResponse(boolean isOK, boolean isSdcardInserted, double batteryCapacity, boolean isRecording) {
        super(isOK);
        this.isSdcardInserted = isSdcardInserted;
        this.batteryCapacity = batteryCapacity;
        this.isRecording = isRecording;
    }

    public void copyExceptRecordingFlag(StateResponse desnation) {
        desnation.isSdcardInserted = this.isSdcardInserted;
        desnation.batteryCapacity = this.batteryCapacity;
        desnation.phtotoCapacity = this.phtotoCapacity;
        desnation.videoCapacity = this.videoCapacity;
        desnation.recordedTime = this.recordedTime;
    }
}

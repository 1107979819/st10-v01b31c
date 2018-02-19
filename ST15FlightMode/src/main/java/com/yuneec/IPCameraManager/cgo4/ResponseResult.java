package com.yuneec.IPCameraManager.cgo4;

public abstract class ResponseResult {
    public final boolean isOk;

    public ResponseResult(boolean isOk) {
        this.isOk = isOk;
    }
}

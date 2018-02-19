package com.yuneec.IPCameraManager;

import com.yuneec.IPCameraManager.cgo4.ResponseResult;

public class BindResponse extends ResponseResult {
    public final boolean bindedResult;
    public final String serverMacAddress;

    public BindResponse(boolean isOk, boolean bindedResult, String serverMac) {
        super(isOk);
        this.bindedResult = bindedResult;
        this.serverMacAddress = serverMac;
    }
}

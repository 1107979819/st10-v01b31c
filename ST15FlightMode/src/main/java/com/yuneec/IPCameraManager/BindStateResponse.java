package com.yuneec.IPCameraManager;

import com.yuneec.IPCameraManager.cgo4.ResponseResult;

public class BindStateResponse extends ResponseResult {
    public final String bindedClientAddress;
    public final boolean isBinded;

    public BindStateResponse(boolean isOk, boolean isBinded, String clientAddress) {
        super(isOk);
        this.isBinded = isBinded;
        this.bindedClientAddress = clientAddress;
    }
}

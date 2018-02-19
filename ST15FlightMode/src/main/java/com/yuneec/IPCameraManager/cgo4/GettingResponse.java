package com.yuneec.IPCameraManager.cgo4;

public class GettingResponse extends ResponseResult {
    public String attr;
    public String value;
    public String value2;

    public GettingResponse(boolean isOk, String attr, String value) {
        super(isOk);
        this.attr = attr;
        this.value = value;
    }
}

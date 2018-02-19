package com.spreadtrum.android.eng;

import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class engfetch {
    private int mSocketID = -1;
    private int mType = 0;

    private static native void disable_modemdebugpm(int i);

    private static native void enable_modemdebugpm(int i);

    private static native int eng_getdebugnowakelock(int i);

    private static native void engf_close(int i);

    private static native int engf_getphasecheck(byte[] bArr, int i);

    private static native int engf_open(int i);

    private static native int engf_read(int i, byte[] bArr, int i2);

    private static native int engf_write(int i, byte[] bArr, int i2);

    static {
        System.loadLibrary("engmodeljni");
    }

    public void writeCmd(String cmd) {
        int sockid = engopen();
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
        String str = "CMD:" + cmd;
        try {
            outputBufferStream.writeBytes(str);
            engwrite(sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
            Log.d("engfetch", "write cmd '" + str + "'");
            engclose(sockid);
        } catch (IOException e) {
            Log.e("engfetch", "writebytes error");
        }
    }

    public int engopen() {
        return engopen(0);
    }

    public int engopen(int type) {
        if (this.mSocketID >= 0) {
            engclose(this.mSocketID);
        }
        int result = engf_open(type);
        if (result < 0) {
            return 0;
        }
        this.mType = type;
        this.mSocketID = result;
        return result;
    }

    public void engclose(int fd) {
        engf_close(this.mSocketID);
        this.mSocketID = -1;
    }

    private boolean engreopen() {
        boolean z;
        int result = engf_open(this.mType);
        if (result >= 0) {
            this.mSocketID = result;
        }
        String str = "engfetch";
        StringBuilder append = new StringBuilder().append("engreopen: ");
        if (result >= 0) {
            z = true;
        } else {
            z = false;
        }
        Log.e(str, append.append(z).toString());
        if (result >= 0) {
            return true;
        }
        return false;
    }

    public int engwrite(int fd, byte[] data, int dataSize) {
        if (this.mSocketID < 0 && !engreopen()) {
            return 0;
        }
        int result;
        int iCount = 0;
        while (true) {
            result = engf_write(this.mSocketID, data, dataSize);
            if (result < 0 && iCount < 1 && engreopen()) {
                iCount++;
            }
        }
        return result;
    }

    public int engread(int fd, byte[] data, int size) {
        if (this.mSocketID < 0 && !engreopen()) {
            return 0;
        }
        int result;
        int iCount = 0;
        while (true) {
            result = engf_read(this.mSocketID, data, size);
            if (result < 0 && iCount < 1 && engreopen()) {
                iCount++;
            }
        }
        return result;
    }

    public int enggetphasecheck(byte[] data, int size) {
        return engf_getphasecheck(data, size);
    }

    public void enablemodemdebugpm(int size) {
        enable_modemdebugpm(size);
    }

    public void disablemodemdebugpm(int size) {
        disable_modemdebugpm(size);
    }

    public int enggetdebugnowakelock(int size) {
        return eng_getdebugnowakelock(size);
    }
}

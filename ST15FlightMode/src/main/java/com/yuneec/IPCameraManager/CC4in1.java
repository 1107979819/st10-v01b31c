package com.yuneec.IPCameraManager;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import com.yuneec.IPCameraManager.IPCameraManager.SDCardStatus;

public class CC4in1 extends DM368 {
    private static final String TAG = "CC4in1";

    public void startRecord(Messenger relyTo, String cameraInfo) {
        Log.i(TAG, "start record");
        postRequest(this.SERVER_URL + "vb.htm" + "?" + "cc4in1record=1" + "@" + cameraInfo, relyTo, 2, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void stopRecord(Messenger relyTo, String cameraInfo) {
        Log.i(TAG, "stop record");
        postRequest(this.SERVER_URL + "vb.htm" + "?" + "cc4in1record=0" + "@" + cameraInfo, relyTo, 3, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void snapShot(String filename, Messenger relyTo, String cameraInfo) {
        Log.i(TAG, "snapshot");
        postRequest(this.SERVER_URL + "vb.htm" + "?" + "cc4in1capture=1" + "@" + cameraInfo, relyTo, 4, IPCameraManager.APACHE_SET_NORESPONSE);
    }

    public void getSDCardStatus(Messenger relyTo) {
        if (relyTo != null) {
            SDCardStatus sdCardstatus = new SDCardStatus();
            sdCardstatus.isInsert = true;
            sdCardstatus.free_space = 512000;
            Message msg = Message.obtain();
            msg.what = 1;
            msg.arg1 = 10;
            msg.obj = sdCardstatus;
            try {
                relyTo.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}

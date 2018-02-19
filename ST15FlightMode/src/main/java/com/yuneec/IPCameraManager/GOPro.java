package com.yuneec.IPCameraManager;

import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class GOPro extends DM368 {
    public void setCC4In1Config(Messenger relyTo, Cameras camera) {
        Message msg = Message.obtain();
        msg.what = 1;
        msg.arg1 = 12;
        msg.obj = IPCameraManager.HTTP_RESPONSE_CODE_OK;
        try {
            relyTo.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}

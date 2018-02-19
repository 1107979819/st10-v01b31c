package com.spreadtrum.android.eng;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.internal.telephony.PhoneFactory;

public class AutoAnswerService extends Service {
    private final String TAG = "AutoAnswerService";
    private MyHandler mHandler = new MyHandler();
    private MyPhoneStateListener mListener;
    private TelephonyManager mTele1;
    private TelephonyManager mTele2;

    private final class MyHandler extends Handler {
        private MyHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Log.d("AutoAnswerService", "MSG_DELAY_AUTOANSWER");
                    ITelephony iTele = Stub.asInterface(ServiceManager.getService("phone"));
                    if (iTele != null) {
                        try {
                            if (iTele.isRinging() && iTele.isVTCall()) {
                                iTele.silenceRinger();
                                iTele.answerRingingCall();
                                return;
                            }
                            return;
                        } catch (RemoteException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    class MyPhoneStateListener extends PhoneStateListener {
        MyPhoneStateListener() {
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case 0:
                    Log.d("AutoAnswerService", "IDLE");
                    return;
                case 1:
                    Log.d("AutoAnswerService", "RINGING");
                    try {
                        AutoAnswerService.this.answerPhoneAidl();
                        return;
                    } catch (Exception e) {
                        return;
                    }
                case 2:
                    Log.d("AutoAnswerService", "OFFHOOK");
                    return;
                default:
                    return;
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d("AutoAnswerService", "listen PhoneState");
        this.mTele1 = (TelephonyManager) getSystemService(PhoneFactory.getServiceName("phone", 0));
        this.mTele2 = (TelephonyManager) getSystemService(PhoneFactory.getServiceName("phone", 1));
        this.mListener = new MyPhoneStateListener();
        this.mTele1.listen(this.mListener, 32);
        this.mTele2.listen(this.mListener, 32);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d("AutoAnswerService", "AutoAnswerService onDestroy");
        this.mTele1.listen(this.mListener, 0);
        this.mTele2.listen(this.mListener, 0);
    }

    private void answerPhoneAidl() throws Exception {
        ITelephony iTele = Stub.asInterface(ServiceManager.getService("phone"));
        if (iTele == null) {
            return;
        }
        if (iTele.isVTCall()) {
            Log.d("AutoAnswerService", "is VT call");
            this.mHandler.removeMessages(0);
            this.mHandler.sendEmptyMessageDelayed(0, 4500);
        } else if (iTele.isRinging()) {
            Log.d("AutoAnswerService", "answer ringing call");
            iTele.silenceRinger();
            iTele.answerRingingCall();
        }
    }
}

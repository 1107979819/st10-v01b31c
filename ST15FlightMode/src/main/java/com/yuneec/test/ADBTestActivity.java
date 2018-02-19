package com.yuneec.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.yuneec.flightmode15.R;
import com.yuneec.uartcontroller.UARTController;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ADBTestActivity extends Activity implements OnClickListener {
    private static final int SERVER_PORT = 10086;
    private static Boolean mainThreadFlag = Boolean.valueOf(true);
    static String tmpStr = "{\"type\":\"setting\", \"rf_power\":10}";
    static String tmpStr1 = "{\"type\":\"getting\"}";
    static String tmpStr2 = "{\"type\":\"enter\", \"rf_channel\":15}";
    static String tmpStr3 = "{\"type\":\"exit\"}";
    private boolean DEBUG = false;
    private Button mBtnTest1;
    private Button mBtnTest2;
    private Button mBtnTest3;
    private Button mBtnTest4;
    private CommandHandler mCommandHandler;
    private EditText mEdtChannel;
    private EditText mEdtMode;
    private EditText mEdtPower;
    private Socket mPreSocket = null;
    private Socket mSocket = null;
    private Thread mThreaderMain = null;
    private Thread mThreaderSub = null;
    private Transfer mTransfer = null;
    private UARTController mUARTController = null;
    private ServerSocket serverSocket = null;

    private class CommandHandler extends Handler {
        public CommandHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.arg1 == 1 || msg.arg2 == 0) {
                switch (msg.what) {
                    case 1001:
                        if (ADBTestActivity.this.mTransfer != null) {
                            ADBTestActivity.this.mTransfer.setData((byte[]) msg.obj, 1001);
                            return;
                        }
                        return;
                    case 1002:
                        if (ADBTestActivity.this.mTransfer != null) {
                            ADBTestActivity.this.mTransfer.setData((byte[]) msg.obj, 1002);
                            return;
                        }
                        return;
                    case Transfer.TYPE_DATA_TELE /*1003*/:
                        if (ADBTestActivity.this.mTransfer != null) {
                            ADBTestActivity.this.mTransfer.setData((byte[]) msg.obj, Transfer.TYPE_DATA_TELE);
                            return;
                        }
                        return;
                    default:
                        Log.i("TEST", "TEST msg.what ERROR : " + msg.what);
                        return;
                }
            }
            Log.i("TEST", "TEST msg.arg1:" + msg.arg1 + ", msg.arg2:" + msg.arg2);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        Log.i("TEST", "TEST --onCreate--");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pc_adb_test);
    }

    protected void onResume() {
        super.onResume();
        Log.i("TEST", "TEST --onResume--");
        if (this.DEBUG) {
            this.mBtnTest1 = (Button) findViewById(R.id.button1);
            this.mBtnTest1.setOnClickListener(this);
            this.mBtnTest1.setEnabled(true);
            this.mBtnTest2 = (Button) findViewById(R.id.button2);
            this.mBtnTest2.setOnClickListener(this);
            this.mBtnTest2.setEnabled(true);
            this.mBtnTest3 = (Button) findViewById(R.id.button3);
            this.mBtnTest3.setOnClickListener(this);
            this.mBtnTest3.setEnabled(true);
            this.mBtnTest4 = (Button) findViewById(R.id.button4);
            this.mBtnTest4.setOnClickListener(this);
            this.mBtnTest4.setEnabled(true);
            this.mEdtPower = (EditText) findViewById(R.id.EdtPower);
            this.mEdtPower.setEnabled(true);
            this.mEdtChannel = (EditText) findViewById(R.id.EdtChannel);
            this.mEdtChannel.setEnabled(true);
            this.mEdtMode = (EditText) findViewById(R.id.EdtMode);
            this.mEdtMode.setEnabled(true);
        }
        Log.i("TEST", "TEST Sevice is onCreate!!!");
        HandlerThread handlerThread = new HandlerThread("usb-handler");
        handlerThread.start();
        this.mCommandHandler = new CommandHandler(handlerThread.getLooper());
        if (this.mUARTController == null) {
            this.mUARTController = UARTController.getInstance();
        }
        if (this.mThreaderMain == null) {
            this.mThreaderMain = new Thread() {
                public void run() {
                    try {
                        Log.i("TEST", "TEST Device doListen()...");
                        ADBTestActivity.mainThreadFlag = Boolean.valueOf(true);
                        ADBTestActivity.this.serverSocket = new ServerSocket(ADBTestActivity.SERVER_PORT);
                        Log.i("TEST", "TEST Device get serverSocket=" + ADBTestActivity.this.serverSocket + ", mainThreadFlag=" + ADBTestActivity.mainThreadFlag);
                        while (ADBTestActivity.mainThreadFlag.booleanValue()) {
                            ADBTestActivity.this.mSocket = ADBTestActivity.this.serverSocket.accept();
                            Log.i("TEST", "TEST Device accept socket=" + ADBTestActivity.this.mSocket);
                            if (ADBTestActivity.this.mSocket != null) {
                                if (ADBTestActivity.this.mPreSocket != null && ADBTestActivity.this.mPreSocket.isConnected()) {
                                    ADBTestActivity.this.mPreSocket.close();
                                    Log.i("TEST", "TEST Service mPreSocket is closed!!");
                                }
                                if (ADBTestActivity.this.mTransfer != null) {
                                    ADBTestActivity.this.mTransfer.stopTransfer();
                                }
                                if (ADBTestActivity.this.mThreaderSub != null && ADBTestActivity.this.mThreaderSub.isAlive()) {
                                    ADBTestActivity.this.mThreaderSub.interrupt();
                                    Log.i("TEST", "TEST Service mThreaderSub is closed!!");
                                }
                                ADBTestActivity.this.mPreSocket = ADBTestActivity.this.mSocket;
                                ADBTestActivity.this.mTransfer = new Transfer(ADBTestActivity.this.mSocket);
                                if (ADBTestActivity.this.mTransfer != null) {
                                    ADBTestActivity.this.mTransfer.setUARTService(ADBTestActivity.this.mUARTController);
                                }
                                ADBTestActivity.this.mThreaderSub = new Thread(ADBTestActivity.this.mTransfer);
                                ADBTestActivity.this.mThreaderSub.start();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            this.mThreaderMain.start();
        }
    }

    protected void onRestart() {
        super.onRestart();
        Log.i("TEST", "TEST --onRestart--");
    }

    private void destoryFun() {
        mainThreadFlag = Boolean.valueOf(false);
        try {
            if (this.mUARTController != null) {
                this.mUARTController.destory();
                this.mUARTController = null;
            }
            if (this.mTransfer != null) {
                this.mTransfer.stopTransfer();
            }
            if (this.mSocket != null) {
                this.mSocket.close();
            }
            if (this.serverSocket != null) {
                this.serverSocket.close();
            }
            this.mSocket = null;
            this.mTransfer = null;
            this.serverSocket = null;
            if (this.mThreaderSub != null && this.mThreaderSub.isAlive()) {
                this.mThreaderSub.interrupt();
            }
            if (this.mThreaderMain != null && this.mThreaderMain.isAlive()) {
                this.mThreaderMain.interrupt();
            }
            this.mThreaderSub = null;
            this.mThreaderMain = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("TEST", "TEST Sevice onDestroy!!!");
        this.mCommandHandler.getLooper().quit();
    }

    protected void onPause() {
        destoryFun();
        super.onPause();
    }

    protected void onDestroy() {
        destoryFun();
        super.onDestroy();
    }

    public void onClick(View v) {
        if (!this.DEBUG) {
            return;
        }
        if (v.equals(this.mBtnTest1)) {
            int tmpInt = 10;
            if (this.mEdtPower != null) {
                tmpInt = Integer.parseInt(this.mEdtPower.getText().toString());
            }
            Log.i("TEST", "TEST Setting power:" + tmpInt + ", mResult=" + this.mUARTController.writeTransmitRate(tmpInt));
        } else if (v.equals(this.mBtnTest2)) {
            Log.i("TEST", "TEST Getting result=" + this.mUARTController.readTransmitRate());
        } else if (v.equals(this.mBtnTest3)) {
            int tmpChannel = 10;
            int tmpMode = 10;
            if (!(this.mEdtChannel == null || this.mEdtMode == null)) {
                tmpChannel = Integer.parseInt(this.mEdtChannel.getText().toString());
                tmpMode = Integer.parseInt(this.mEdtMode.getText().toString());
            }
            Log.i("TEST", "TEST Enter Channel:" + tmpChannel + ", Mode=" + tmpMode + ", result=" + this.mUARTController.PCenterTestRF(tmpChannel, tmpMode));
        } else if (v.equals(this.mBtnTest4)) {
            Log.i("TEST", "TEST Exit result=" + this.mUARTController.enterRun(false));
        }
    }
}

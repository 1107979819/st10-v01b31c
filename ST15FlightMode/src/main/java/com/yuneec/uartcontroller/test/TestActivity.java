package com.yuneec.uartcontroller.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.yuneec.uartcontroller.GPSUpLinkData;
import com.yuneec.uartcontroller.MixedData;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.uartcontroller.UARTInfoMessage;
import com.yuneec.uartcontroller.UARTInfoMessage.CalibrationRawData;
import com.yuneec.uartcontroller.UARTInfoMessage.CalibrationState;
import com.yuneec.uartcontroller.UARTInfoMessage.Channel;
import com.yuneec.uartcontroller.UARTInfoMessage.RxBindInfo;
import com.yuneec.uartcontroller.UARTInfoMessage.SwitchChanged;
import com.yuneec.uartcontroller.UARTInfoMessage.TransmitterState;
import com.yuneec.uartcontroller.UARTInfoMessage.Trim;
import com.yuneec.uartcontroller.UARTInfoMessage.UARTRelyMessage;
import com.yuneec.uartcontroller15.R;
import java.util.ArrayList;

public class TestActivity extends Activity implements OnClickListener, OnCheckedChangeListener, OnItemClickListener {
    protected static final String TAG = "TestActivity";
    private ArrayAdapter<String> mAdapter;
    private Button mBtnCh1;
    private Button mBtnCh2;
    private Button mBtnCh3;
    private Button mBtnCh4;
    private ToggleButton mCalibration;
    private SeekBar mCh1;
    private SeekBar mCh2;
    private SeekBar mCh3;
    private SeekBar mCh4;
    private SeekBar mCh5;
    private SeekBar mCh6;
    private SeekBar mCh7;
    private SeekBar mCh8;
    private UARTController mController;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.obj instanceof UARTInfoMessage) {
                UARTInfoMessage umsg = msg.obj;
                switch (umsg.what) {
                    case 1:
                        Log.d(TestActivity.TAG, "TRANSMITTER_INFO:" + ((TransmitterState) umsg).status);
                        return;
                    case 2:
                        Log.d(TestActivity.TAG, "UARTInfoMessage.CHANNEL_INFO");
                        Channel cmsg = (Channel) umsg;
                        if (cmsg.channels.size() >= 4) {
                            float progress = ((Float) cmsg.channels.get(0)).floatValue();
                            TestActivity.this.mCh1.setProgress((int) progress);
                            TestActivity.this.mBtnCh1.setText(String.valueOf(progress));
                            progress = ((Float) cmsg.channels.get(1)).floatValue();
                            TestActivity.this.mBtnCh2.setText(String.valueOf(progress));
                            TestActivity.this.mCh2.setProgress((int) progress);
                            progress = ((Float) cmsg.channels.get(2)).floatValue();
                            TestActivity.this.mBtnCh3.setText(String.valueOf(progress));
                            TestActivity.this.mCh3.setProgress((int) progress);
                            progress = ((Float) cmsg.channels.get(3)).floatValue();
                            TestActivity.this.mBtnCh4.setText(String.valueOf(progress));
                            TestActivity.this.mCh4.setProgress((int) progress);
                            return;
                        }
                        return;
                    case 4:
                        Trim tmsg = (Trim) umsg;
                        Log.v(TestActivity.TAG, "TRIM:" + tmsg.t1 + " " + tmsg.t2 + " " + tmsg.t3 + " " + tmsg.t4);
                        return;
                    case 11:
                        RxBindInfo rx = (RxBindInfo) umsg;
                        Log.i(TestActivity.TAG, "rx info:" + rx.node_id);
                        TestActivity.this.mAdapter.add(String.valueOf(rx.node_id));
                        TestActivity.this.refreshList();
                        return;
                    case 14:
                        SwitchChanged sc = (SwitchChanged) umsg;
                        Log.d(TestActivity.TAG, "switch changed :" + sc.hw_id + " " + sc.old_state + " " + sc.new_state);
                        return;
                    case 18:
                        Log.d(TestActivity.TAG, "csi :" + ((CalibrationState) umsg).hardware_state.size() + ",");
                        return;
                    case 19:
                        Log.d(TestActivity.TAG, "tx need update");
                        return;
                    case UARTRelyMessage.REPLY_BIND_INFO /*1011*/:
                        Toast.makeText(TestActivity.this, "Bind success", 0).show();
                        return;
                    default:
                        return;
                }
            }
        }
    };
    private ListView mListView;
    private ToggleButton mRun;
    private ArrayList<String> mRxList = new ArrayList();
    private int test_addr = 1001;
    private boolean toggle = false;
    private UARTInfoMessage umsg;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bind).setOnClickListener(this);
        findViewById(R.id.read).setOnClickListener(this);
        findViewById(R.id.bindAModel).setOnClickListener(this);
        findViewById(R.id.finishBind).setOnClickListener(this);
        findViewById(R.id.testBit).setOnClickListener(this);
        this.mCh1 = (SeekBar) findViewById(R.id.seekBar1);
        this.mCh2 = (SeekBar) findViewById(R.id.seekBar2);
        this.mCh3 = (SeekBar) findViewById(R.id.seekBar3);
        this.mCh4 = (SeekBar) findViewById(R.id.seekBar4);
        this.mCh5 = (SeekBar) findViewById(R.id.seekBar5);
        this.mCh6 = (SeekBar) findViewById(R.id.seekBar6);
        this.mCh7 = (SeekBar) findViewById(R.id.seekBar7);
        this.mCh8 = (SeekBar) findViewById(R.id.seekBar8);
        this.mBtnCh1 = (Button) findViewById(R.id.button1);
        this.mBtnCh2 = (Button) findViewById(R.id.button2);
        this.mBtnCh3 = (Button) findViewById(R.id.button3);
        this.mBtnCh4 = (Button) findViewById(R.id.button4);
        this.mCh1.setMax(4096);
        this.mCh2.setMax(4096);
        this.mCh3.setMax(4096);
        this.mCh4.setMax(4096);
        this.mCh5.setMax(4096);
        this.mCh6.setMax(4096);
        this.mCh7.setMax(4096);
        this.mCh8.setMax(4096);
        this.mCalibration = (ToggleButton) findViewById(R.id.buttonStartCali);
        this.mCalibration.setOnCheckedChangeListener(this);
        this.mRun = (ToggleButton) findViewById(R.id.tog_run);
        this.mRun.setOnCheckedChangeListener(this);
        TextView tv = new TextView(this);
        tv.setTextSize(20.0f);
        tv.setText("No Rx Founds");
        this.mListView = (ListView) findViewById(R.id.listView1);
        this.mListView.setEmptyView(tv);
        this.mAdapter = new ArrayAdapter(this, 17367062, this.mRxList);
        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setOnItemClickListener(this);
        this.mController = UARTController.getInstance();
    }

    public void onClick(View v) {
        if (v.equals(findViewById(R.id.bind))) {
            this.mController.enterBind(false);
            this.mController.startBind(false);
        } else if (v.equals(findViewById(R.id.read))) {
            this.mController.startReading();
            this.mController.registerReaderHandler(this.mHandler);
            v.setEnabled(false);
        } else if (!v.equals(findViewById(R.id.bindAModel))) {
            if (v.equals(findViewById(R.id.finishBind))) {
                this.mController.finishBind(false);
                this.mController.exitBind(false);
            } else if (v.equals(findViewById(R.id.testBit))) {
                this.mController.updateRfVersion(Environment.getExternalStorageDirectory() + "/firmware/testaes.bin");
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mController.stopReading();
        this.mController.destory();
        this.mController = null;
    }

    private void refreshList() {
        this.mListView.invalidateViews();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.equals(this.mCalibration)) {
            if (isChecked) {
                this.mController.startCalibration(true);
            } else {
                this.mController.finishCalibration(true);
            }
        } else if (!buttonView.equals(this.mRun)) {
        } else {
            if (isChecked) {
                this.mController.enterRun(true);
            } else {
                this.mController.exitRun(true);
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 7) {
            this.mController.enterBind(true);
        } else if (keyCode == 8) {
            this.mController.exitBind(true);
        } else if (keyCode == 9) {
            this.mController.enterRun(true);
        } else if (keyCode == 10) {
            this.mController.exitRun(true);
        } else if (keyCode == 11) {
            this.mController.enterSim(true);
        } else if (keyCode == 12) {
            this.mController.exitSim(true);
        } else if (keyCode == 13) {
            this.mController.syncMixingDataDeleteAll(false);
            MixedData a = new MixedData();
            a.mChannel = 1;
            a.mFmode = 0;
            a.mhardware = 1;
            a.mHardwareType = 2;
            a.mMixedType = 3;
            a.mPriority = 4;
            a.mReverse = true;
            a.mSpeed = 5;
            a.mCurvePoint.add(Integer.valueOf(1));
            a.mCurvePoint.add(Integer.valueOf(2));
            a.mCurvePoint.add(Integer.valueOf(3));
            a.mCurvePoint.add(Integer.valueOf(4));
            a.mSwitchStatus.add(Boolean.valueOf(true));
            a.mSwitchStatus.add(Boolean.valueOf(false));
            a.mSwitchStatus.add(Boolean.valueOf(true));
            a.mSwitchValue.add(Integer.valueOf(10));
            a.mSwitchValue.add(Integer.valueOf(11));
            a.mSwitchValue.add(Integer.valueOf(-12));
            this.mController.syncMixingData(false, a, 0);
        } else if (keyCode == 14) {
            this.mController.unbind(false);
        } else if (keyCode == 15) {
            Log.e(TAG, "rx state " + this.mController.queryBindState());
        } else if (keyCode == 16) {
            String addr = (String) this.mListView.getSelectedItem();
            int i = 0 + 1;
        } else if (keyCode == 27) {
            Log.i(TAG, "camera state:" + event.getAction());
        } else if (keyCode == 80) {
            Log.i(TAG, "video state:" + event.getAction());
        } else if (keyCode == 29) {
            this.mController.startReading();
            this.mController.registerReaderHandler(this.mHandler);
        } else if (keyCode == 30) {
            this.mController.correctTxState(1, 3);
        } else if (keyCode == 31) {
            this.mController.correctTxState(2, 3);
        } else if (keyCode == 32) {
            this.mController.correctTxState(5, 3);
        } else if (keyCode == 33) {
            this.mController.correctTxState(6, 3);
        } else if (keyCode == 34) {
            this.mController.receiveRawChannelOnly(false);
        } else if (keyCode == 35) {
            this.mController.receiveMixedChannelOnly(true);
        } else if (keyCode == 36) {
            Log.d(TAG, "switch state:" + this.mController.querySwitchState(17));
        } else if (keyCode == 37) {
            Log.i(TAG, this.mController.getTransmitterVersion());
            Log.i(TAG, this.mController.getRadioVersion());
        } else if (keyCode == 38) {
            boolean z;
            if (this.toggle) {
                this.mController.setTTBState(false, 0, true);
                this.mController.setTTBState(false, 1, true);
                this.mController.setTTBState(false, 2, true);
                this.mController.setTTBState(false, 3, true);
                this.mController.setTTBState(false, 4, true);
                this.mController.setTTBState(false, 5, true);
            } else {
                this.mController.setTTBState(false, 0, false);
                this.mController.setTTBState(false, 1, false);
                this.mController.setTTBState(false, 2, false);
                this.mController.setTTBState(false, 3, false);
                this.mController.setTTBState(false, 4, false);
                this.mController.setTTBState(false, 5, false);
            }
            if (this.toggle) {
                z = false;
            } else {
                z = true;
            }
            this.toggle = z;
        } else if (keyCode == 39) {
            this.mController.setChannelConfig(false, 4, 4);
        } else if (keyCode == 40) {
            GPSUpLinkData gps_data = new GPSUpLinkData();
            gps_data.accuracy = 10.434f;
            gps_data.alt = 45.556f;
            gps_data.lat = 31.43213f;
            gps_data.lon = 131.14342f;
            gps_data.angle = 82.4f;
            gps_data.no_satelites = 9;
            gps_data.speed = 10.3f;
            this.mController.updateGps(false, gps_data);
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
            }
            this.mController.updateCompass(false, 45.87f);
        } else if (keyCode == 41) {
            this.mController.updateTxVersion(Environment.getExternalStorageDirectory() + "/firmware/st24_mcu_iap.bin");
        } else if (keyCode == 42) {
            this.mController.getTrimStep();
        } else if (keyCode == 43) {
            int[] array = new int[]{-2, -2, -3, -4, -5, -6, -7, -8, -8, -10};
            this.mController.setSubTrim(false, array);
            this.mController.getSubTrim(array);
        } else if (keyCode == 44) {
            this.mController.enterFactoryCalibration(true);
        } else if (keyCode == 45) {
            this.mController.exitFactoryCalibration(true);
        } else if (keyCode == 46) {
            this.mController.updateTxVersion(Environment.getExternalStorageDirectory() + "/firmware/st24_mcu_iap.bin");
        } else if (keyCode == 47) {
            this.mController.updateRfVersion(Environment.getExternalStorageDirectory() + "/firmware/testaes.bin");
        } else if (keyCode == 48) {
            CalibrationRawData data = this.mController.getCalibrationRawData(true);
            if (data != null) {
                Log.i(TAG, "info:" + data.toString());
            }
        } else if (keyCode == 49) {
            this.mController = UARTController.getInstance();
        }
        return super.onKeyDown(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == 27) {
            Log.i(TAG, "camera state:" + event.getAction());
        } else if (keyCode == 80) {
            Log.i(TAG, "video state:" + event.getAction());
        }
        return super.onKeyUp(keyCode, event);
    }

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == 27) {
            Log.i(TAG, "camera state: long press");
        } else if (keyCode == 80) {
            Log.i(TAG, "video state: long press");
        }
        return super.onKeyLongPress(keyCode, event);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            this.mController.bind(false, Integer.parseInt((String) parent.getItemAtPosition(position)));
        } catch (NumberFormatException e) {
        }
    }
}

package com.yuneec.test;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.uartcontroller.UARTInfoMessage;
import com.yuneec.uartcontroller.UARTInfoMessage.Signal;

public class SignalTest extends Activity {
    private static final String TAG = "SignalTest";
    private Runnable getSignalRunnable = new Runnable() {
        public void run() {
            SignalTest.this.mController.getSignal(false, SignalTest.this.mCurrentLine);
            Log.i(SignalTest.TAG, "Get signal value: Line-" + SignalTest.this.mCurrentLine);
            SignalTest.this.mHandler.postDelayed(SignalTest.this.getSignalRunnable, 1000);
        }
    };
    private UARTController mController;
    CountAVGTask mCountTask = null;
    private int mCurrentLine = 1;
    private Handler mHandler = new Handler();
    private RadioGroup mLineGroup;
    private Switch mSignalSwitch;
    private Handler mUartHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (SignalTest.this.mController != null && (msg.obj instanceof UARTInfoMessage)) {
                UARTInfoMessage umsg = msg.obj;
                if (umsg.what == 22) {
                    Signal signal = (Signal) umsg;
                    SignalTest.this.tx_signal.setText(SignalTest.this.getString(R.string.str_signal_real, new Object[]{"TX ", Integer.valueOf(signal.tx_signal)}));
                    SignalTest.this.rf_signal.setText(SignalTest.this.getString(R.string.str_signal_real, new Object[]{"RF ", Integer.valueOf(signal.rf_signal)}));
                    if (signal.tx_signal != 127) {
                        SignalTest.this.countTXAverage(signal.tx_signal);
                    }
                    if (signal.rf_signal != 127) {
                        SignalTest.this.countRFAverage(signal.rf_signal);
                    }
                    if (SignalTest.this.times < SignalTest.this.perTimes) {
                        SignalTest signalTest = SignalTest.this;
                        signalTest.times = signalTest.times + 1;
                        return;
                    }
                    SignalTest.this.compareTXtarget();
                    SignalTest.this.compareRFTarget();
                }
            }
        }
    };
    private int perTimes = 200;
    private TextView rf_avg;
    private float rf_avg_value = 0.0f;
    private TextView rf_result;
    private TextView rf_signal;
    private EditText rf_target;
    private EditText rf_target2;
    private int rf_target_value = 0;
    private EditText timeTxt;
    private int times = 0;
    private TextView tx_avg;
    private float tx_avg_value = 0.0f;
    private TextView tx_result;
    private TextView tx_signal;
    private EditText tx_target;
    private EditText tx_target2;
    private int tx_target_value = 0;

    private class CountAVGTask extends AsyncTask<Integer, Void, Void> {
        private int rfTarget;
        private int txTarget;

        public CountAVGTask(int txTarget, int rfTarget) {
            this.txTarget = txTarget;
            this.rfTarget = rfTarget;
        }

        protected Void doInBackground(Integer... arg0) {
            int txValue = arg0[0].intValue();
            int rfValue = arg0[1].intValue();
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signal_test);
        getWindow().addFlags(128);
        this.tx_target = (EditText) findViewById(R.id.tx_signal_target);
        this.rf_target = (EditText) findViewById(R.id.rf_signal_target);
        this.tx_target2 = (EditText) findViewById(R.id.tx_signal_target_2);
        this.rf_target2 = (EditText) findViewById(R.id.rf_signal_target_2);
        this.tx_signal = (TextView) findViewById(R.id.tx_signal);
        this.tx_avg = (TextView) findViewById(R.id.tx_signal_avg);
        this.tx_result = (TextView) findViewById(R.id.tx_signal_result);
        this.rf_signal = (TextView) findViewById(R.id.rf_signal);
        this.rf_avg = (TextView) findViewById(R.id.rf_signal_avg);
        this.rf_result = (TextView) findViewById(R.id.rf_signal_result);
        this.timeTxt = (EditText) findViewById(R.id.times);
        this.mLineGroup = (RadioGroup) findViewById(R.id.changeline);
        this.mLineGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup arg0, int arg1) {
                if (arg1 == R.id.line1_btn) {
                    SignalTest.this.tx_target.setEnabled(true);
                    SignalTest.this.rf_target.setEnabled(true);
                    SignalTest.this.tx_target2.setEnabled(false);
                    SignalTest.this.rf_target2.setEnabled(false);
                    SignalTest.this.mCurrentLine = 1;
                } else if (arg1 == R.id.line2_btn) {
                    SignalTest.this.tx_target2.setEnabled(true);
                    SignalTest.this.rf_target2.setEnabled(true);
                    SignalTest.this.tx_target.setEnabled(false);
                    SignalTest.this.rf_target.setEnabled(false);
                    SignalTest.this.mCurrentLine = 2;
                }
            }
        });
        this.mSignalSwitch = (Switch) findViewById(R.id.signal_switch);
        this.mSignalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                if (!arg1) {
                    if (SignalTest.this.mController != null) {
                        SignalTest.ensureAwaitState(SignalTest.this.mController);
                    }
                    SignalTest.this.restoreViews();
                    SignalTest.this.mHandler.removeCallbacks(SignalTest.this.getSignalRunnable);
                    SignalTest.this.mLineGroup.getChildAt(0).setEnabled(true);
                    SignalTest.this.mLineGroup.getChildAt(1).setEnabled(true);
                    SignalTest.this.times = 0;
                } else if (SignalTest.this.mController != null && SignalTest.ensureBindState(SignalTest.this.mController)) {
                    String value;
                    SignalTest.this.mController.startBind(false);
                    if (SignalTest.this.mCurrentLine == 1) {
                        value = SignalTest.this.tx_target.getText().toString();
                    } else {
                        value = SignalTest.this.tx_target2.getText().toString();
                    }
                    if (!(value == null || value.isEmpty())) {
                        SignalTest.this.tx_target_value = Integer.parseInt(value);
                        if (SignalTest.this.tx_target_value < -128) {
                            SignalTest.this.tx_target_value = -128;
                        }
                        if (SignalTest.this.tx_target_value > 127) {
                            SignalTest.this.tx_target_value = 127;
                        }
                    }
                    Log.i(SignalTest.TAG, "get TX target:" + SignalTest.this.tx_target_value);
                    if (SignalTest.this.mCurrentLine == 1) {
                        value = SignalTest.this.rf_target.getText().toString();
                    } else {
                        value = SignalTest.this.rf_target2.getText().toString();
                    }
                    if (!(value == null || value.isEmpty())) {
                        SignalTest.this.rf_target_value = Integer.parseInt(value);
                        if (SignalTest.this.rf_target_value < -128) {
                            SignalTest.this.rf_target_value = -128;
                        }
                        if (SignalTest.this.rf_target_value > 127) {
                            SignalTest.this.rf_target_value = 127;
                        }
                    }
                    Log.i(SignalTest.TAG, "get RF target:" + SignalTest.this.rf_target_value);
                    String str = SignalTest.this.timeTxt.getText().toString();
                    if (str.isEmpty()) {
                        SignalTest.this.perTimes = 200;
                    } else {
                        SignalTest.this.perTimes = Integer.parseInt(str);
                    }
                    SignalTest.this.times = 0;
                    SignalTest.this.mHandler.post(SignalTest.this.getSignalRunnable);
                    SignalTest.this.mLineGroup.getChildAt(0).setEnabled(false);
                    SignalTest.this.mLineGroup.getChildAt(1).setEnabled(false);
                }
            }
        });
    }

    protected void onResume() {
        super.onResume();
        restoreViews();
        this.mController = UARTController.getInstance();
        if (this.mController != null) {
            this.mController.registerReaderHandler(this.mUartHandler);
            this.mController.startReading();
        }
    }

    protected void onPause() {
        super.onPause();
        this.mHandler.removeCallbacks(this.getSignalRunnable);
        if (this.mController != null) {
            if (!ensureAwaitState(this.mController)) {
                Log.e(TAG, "fail to change to await");
            }
            UartControllerStandBy(this.mController);
            this.mController = null;
        }
    }

    private void countTXAverage(int real_value) {
        if (this.perTimes > 1) {
            this.tx_avg_value = (((float) real_value) + this.tx_avg_value) / Utilities.K_MAX;
        } else {
            this.tx_avg_value = (float) real_value;
        }
        TextView textView = this.tx_avg;
        Object[] objArr = new Object[1];
        objArr[0] = String.format("%.1f", new Object[]{Float.valueOf(this.tx_avg_value)});
        textView.setText(getString(R.string.str_signal_avg, objArr));
    }

    private void countRFAverage(int real_value) {
        if (this.perTimes > 1) {
            this.rf_avg_value = (((float) real_value) + this.rf_avg_value) / Utilities.K_MAX;
        } else {
            this.rf_avg_value = (float) real_value;
        }
        TextView textView = this.rf_avg;
        Object[] objArr = new Object[1];
        objArr[0] = String.format("%.1f", new Object[]{Float.valueOf(this.rf_avg_value)});
        textView.setText(getString(R.string.str_signal_avg, objArr));
    }

    private void compareTXtarget() {
        if (this.tx_avg_value < ((float) (this.tx_target_value - 2))) {
            this.tx_result.setText(R.string.str_fail);
            this.tx_result.setTextColor(-65536);
        } else {
            this.tx_result.setText(R.string.str_pass);
            this.tx_result.setTextColor(-16711936);
        }
        this.tx_result.setVisibility(0);
    }

    private void compareRFTarget() {
        if (this.rf_avg_value < ((float) (this.rf_target_value - 2))) {
            this.rf_result.setText(R.string.str_fail);
            this.rf_result.setTextColor(-65536);
        } else {
            this.rf_result.setText(R.string.str_pass);
            this.rf_result.setTextColor(-16711936);
        }
        this.rf_result.setVisibility(0);
    }

    private void restoreViews() {
        this.tx_signal.setText(getString(R.string.str_signal_real, new Object[]{"TX ", "unknow"}));
        this.tx_avg.setText(getString(R.string.str_signal_avg, new Object[]{Double.valueOf(0.0d)}));
        this.rf_signal.setText(getString(R.string.str_signal_real, new Object[]{"RF ", "unknow"}));
        this.rf_avg.setText(getString(R.string.str_signal_avg, new Object[]{Double.valueOf(0.0d)}));
        this.tx_result.setVisibility(4);
        this.rf_result.setVisibility(4);
        this.mLineGroup.check(R.id.line1_btn);
        this.mLineGroup.getChildAt(0).setEnabled(true);
        this.mLineGroup.getChildAt(1).setEnabled(true);
    }

    private static boolean ensureBindState(UARTController controller) {
        if (controller.enterBind(true) || controller.correctTxState(2, 3)) {
            return true;
        }
        return false;
    }

    private static boolean ensureAwaitState(UARTController controller) {
        return controller.correctTxState(1, 3);
    }

    public static void UartControllerStandBy(UARTController controller) {
        controller.stopReading();
        controller.registerReaderHandler(null);
    }
}

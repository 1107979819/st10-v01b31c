package com.yuneec.test;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.uartcontroller.UARTInfoMessage;
import com.yuneec.uartcontroller.UARTInfoMessage.CalibrationState;
import java.util.HashMap;
import java.util.Map;

public class CalibrationTest extends Activity {
    private static final String TAG = "CalibrationTest";
    private Button calibration_btn;
    private int color_green;
    private Intent intent;
    private boolean isShowFinish = false;
    private UARTController mController;
    private Handler mUartHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.obj instanceof UARTInfoMessage) {
                UARTInfoMessage umsg = msg.obj;
                if (umsg.what == 18) {
                    CalibrationState cs = (CalibrationState) umsg;
                    int max_num = cs.hardware_state.size();
                    if (max_num > 0) {
                        for (int i = 0; i < max_num; i++) {
                            Log.i(CalibrationTest.TAG, "****[mUartHandler],stateMap.get(" + i + ")=" + CalibrationTest.this.stateMap.get(Integer.valueOf(i)) + ", cs.hardware_state.get(" + i + ")=" + cs.hardware_state.get(i) + "****");
                            if (((Boolean) CalibrationTest.this.stateMap.get(Integer.valueOf(i))).booleanValue()) {
                                CalibrationTest.this.updateInterface();
                            } else if (((Integer) cs.hardware_state.get(i)).intValue() != 0) {
                                if (((Integer) cs.hardware_state.get(i)).intValue() == 4) {
                                    CalibrationTest.this.ragMap.put(Integer.valueOf(i), (Integer) cs.hardware_state.get(i));
                                    if (!Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG)) {
                                        if (!Utilities.PROJECT_TAG.equals("ST12")) {
                                            if (Utilities.PROJECT_TAG.equals("ST15")) {
                                                switch (i) {
                                                    case 0:
                                                        CalibrationTest.this.stateMap.put(Integer.valueOf(0), Boolean.valueOf(true));
                                                        CalibrationTest.this.test_j1.setBackgroundColor(CalibrationTest.this.color_green);
                                                        break;
                                                    case 1:
                                                        CalibrationTest.this.stateMap.put(Integer.valueOf(1), Boolean.valueOf(true));
                                                        CalibrationTest.this.test_j2.setBackgroundColor(CalibrationTest.this.color_green);
                                                        break;
                                                    case 2:
                                                        CalibrationTest.this.stateMap.put(Integer.valueOf(2), Boolean.valueOf(true));
                                                        CalibrationTest.this.test_j3.setBackgroundColor(CalibrationTest.this.color_green);
                                                        break;
                                                    case 3:
                                                        CalibrationTest.this.stateMap.put(Integer.valueOf(3), Boolean.valueOf(true));
                                                        CalibrationTest.this.test_j4.setBackgroundColor(CalibrationTest.this.color_green);
                                                        break;
                                                    case 4:
                                                        CalibrationTest.this.stateMap.put(Integer.valueOf(4), Boolean.valueOf(true));
                                                        CalibrationTest.this.test_k1.setBackgroundColor(CalibrationTest.this.color_green);
                                                        break;
                                                    case 5:
                                                        CalibrationTest.this.stateMap.put(Integer.valueOf(5), Boolean.valueOf(true));
                                                        CalibrationTest.this.test_k2.setBackgroundColor(CalibrationTest.this.color_green);
                                                        break;
                                                    case 6:
                                                        CalibrationTest.this.stateMap.put(Integer.valueOf(6), Boolean.valueOf(true));
                                                        CalibrationTest.this.test_k3.setBackgroundColor(CalibrationTest.this.color_green);
                                                        break;
                                                    case 7:
                                                        CalibrationTest.this.stateMap.put(Integer.valueOf(7), Boolean.valueOf(true));
                                                        CalibrationTest.this.test_k4.setBackgroundColor(CalibrationTest.this.color_green);
                                                        break;
                                                    case 8:
                                                        CalibrationTest.this.stateMap.put(Integer.valueOf(8), Boolean.valueOf(true));
                                                        CalibrationTest.this.test_k5.setBackgroundColor(CalibrationTest.this.color_green);
                                                        break;
                                                    default:
                                                        break;
                                                }
                                            }
                                        }
                                        switch (i) {
                                            case 0:
                                                CalibrationTest.this.stateMap.put(Integer.valueOf(0), Boolean.valueOf(true));
                                                CalibrationTest.this.test_j1.setBackgroundColor(CalibrationTest.this.color_green);
                                                break;
                                            case 1:
                                                CalibrationTest.this.stateMap.put(Integer.valueOf(1), Boolean.valueOf(true));
                                                CalibrationTest.this.test_j2.setBackgroundColor(CalibrationTest.this.color_green);
                                                break;
                                            case 2:
                                                CalibrationTest.this.stateMap.put(Integer.valueOf(2), Boolean.valueOf(true));
                                                CalibrationTest.this.test_j3.setBackgroundColor(CalibrationTest.this.color_green);
                                                break;
                                            case 3:
                                                CalibrationTest.this.stateMap.put(Integer.valueOf(3), Boolean.valueOf(true));
                                                CalibrationTest.this.test_j4.setBackgroundColor(CalibrationTest.this.color_green);
                                                break;
                                            case 4:
                                                CalibrationTest.this.stateMap.put(Integer.valueOf(4), Boolean.valueOf(true));
                                                CalibrationTest.this.test_k1.setBackgroundColor(CalibrationTest.this.color_green);
                                                break;
                                            case 5:
                                                CalibrationTest.this.stateMap.put(Integer.valueOf(5), Boolean.valueOf(true));
                                                CalibrationTest.this.test_k2.setBackgroundColor(CalibrationTest.this.color_green);
                                                break;
                                            case 6:
                                                CalibrationTest.this.stateMap.put(Integer.valueOf(6), Boolean.valueOf(true));
                                                CalibrationTest.this.test_k3.setBackgroundColor(CalibrationTest.this.color_green);
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                    switch (i) {
                                        case 0:
                                            CalibrationTest.this.stateMap.put(Integer.valueOf(0), Boolean.valueOf(true));
                                            CalibrationTest.this.test_j1.setBackgroundColor(CalibrationTest.this.color_green);
                                            break;
                                        case 1:
                                            CalibrationTest.this.stateMap.put(Integer.valueOf(1), Boolean.valueOf(true));
                                            CalibrationTest.this.test_j2.setBackgroundColor(CalibrationTest.this.color_green);
                                            break;
                                        case 2:
                                            CalibrationTest.this.stateMap.put(Integer.valueOf(2), Boolean.valueOf(true));
                                            CalibrationTest.this.test_j3.setBackgroundColor(CalibrationTest.this.color_green);
                                            break;
                                        case 3:
                                            CalibrationTest.this.stateMap.put(Integer.valueOf(3), Boolean.valueOf(true));
                                            CalibrationTest.this.test_j4.setBackgroundColor(CalibrationTest.this.color_green);
                                            break;
                                        case 4:
                                            CalibrationTest.this.stateMap.put(Integer.valueOf(4), Boolean.valueOf(true));
                                            CalibrationTest.this.test_k1.setBackgroundColor(CalibrationTest.this.color_green);
                                            break;
                                        case 5:
                                            CalibrationTest.this.stateMap.put(Integer.valueOf(5), Boolean.valueOf(true));
                                            CalibrationTest.this.test_k2.setBackgroundColor(CalibrationTest.this.color_green);
                                            break;
                                    }
                                }
                                Log.v(CalibrationTest.TAG, "---error--" + i + cs.hardware_state.get(i));
                                CalibrationTest.this.updateInterface();
                            }
                        }
                    }
                }
            }
        }
    };
    private Button page2_next;
    public Map<Integer, Integer> ragMap = new HashMap();
    public Map<Integer, Boolean> stateMap = new HashMap();
    private TextView test_j1;
    private TextView test_j2;
    private TextView test_j3;
    private TextView test_j4;
    private TextView test_k1;
    private TextView test_k2;
    private TextView test_k3;
    private TextView test_k4;
    private TextView test_k5;

    private class CalibrationButtonListener implements OnClickListener {
        private CalibrationButtonListener() {
        }

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.page2_next:
                    CalibrationTest.this.finish();
                    CalibrationTest.this.startActivity(CalibrationTest.this.intent);
                    return;
                case R.id.calibration_btn:
                    if (CalibrationTest.this.isShowFinish) {
                        CalibrationTest.this.finishCalibrationTest();
                        return;
                    } else {
                        CalibrationTest.this.startCalibrationTest();
                        return;
                    }
                default:
                    return;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG)) {
            setContentView(R.layout.calitration_test_st10);
        } else if (Utilities.PROJECT_TAG.equals("ST12")) {
            setContentView(R.layout.calitration_test_st12);
        } else if (Utilities.PROJECT_TAG.equals("ST15")) {
            setContentView(R.layout.calitration_test);
        }
        getWindow().addFlags(128);
        this.intent = new Intent(this, ReadCalibrationData.class);
        this.test_k1 = (TextView) findViewById(R.id.test_k1);
        this.test_k2 = (TextView) findViewById(R.id.test_k2);
        if (Utilities.PROJECT_TAG.equals("ST12")) {
            this.test_k3 = (TextView) findViewById(R.id.test_k3);
        } else if (Utilities.PROJECT_TAG.equals("ST15")) {
            this.test_k4 = (TextView) findViewById(R.id.test_k4);
            this.test_k5 = (TextView) findViewById(R.id.test_k5);
        }
        this.test_j1 = (TextView) findViewById(R.id.test_j1);
        this.test_j2 = (TextView) findViewById(R.id.test_j2);
        this.test_j3 = (TextView) findViewById(R.id.test_j3);
        this.test_j4 = (TextView) findViewById(R.id.test_j4);
        this.page2_next = (Button) findViewById(R.id.page2_next);
        this.calibration_btn = (Button) findViewById(R.id.calibration_btn);
        this.calibration_btn.setText(R.string.str_start);
        this.page2_next.setOnClickListener(new CalibrationButtonListener());
        this.calibration_btn.setOnClickListener(new CalibrationButtonListener());
        this.color_green = Color.parseColor("#007800");
        initTempValue();
    }

    private void startCalibrationTest() {
        this.calibration_btn.setText(R.string.str_finish);
        this.mController.enterFactoryCalibration(false);
        this.calibration_btn.setEnabled(false);
        initTempValue();
        this.isShowFinish = true;
    }

    private void finishCalibrationTest() {
        this.calibration_btn.setText(R.string.str_start);
        this.mController.exitFactoryCalibration(false);
        initTempValue();
        this.isShowFinish = false;
    }

    protected void onResume() {
        super.onResume();
        this.mController = UARTController.getInstance();
        this.mController.registerReaderHandler(this.mUartHandler);
        this.mController.startReading();
    }

    protected void onPause() {
        super.onPause();
        finishCalibrationTest();
        this.mController.stopReading();
        this.mController.registerReaderHandler(null);
    }

    private void initTempValue() {
        for (int i = 0; i < 10; i++) {
            this.ragMap.put(Integer.valueOf(i), Integer.valueOf(0));
            this.stateMap.put(Integer.valueOf(i), Boolean.valueOf(false));
            if (!this.isShowFinish) {
                updateInterface();
            }
        }
    }

    public void updateInterface() {
        if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG)) {
            if (((Boolean) this.stateMap.get(Integer.valueOf(0))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(1))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(2))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(3))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(4))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(5))).booleanValue()) {
                if (this.isShowFinish) {
                    this.calibration_btn.setEnabled(true);
                }
            } else if (this.isShowFinish) {
                this.calibration_btn.setEnabled(false);
            }
        } else if (Utilities.PROJECT_TAG.equals("ST12")) {
            if (((Boolean) this.stateMap.get(Integer.valueOf(0))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(1))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(2))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(3))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(4))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(5))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(6))).booleanValue()) {
                if (this.isShowFinish) {
                    this.calibration_btn.setEnabled(true);
                }
            } else if (this.isShowFinish) {
                this.calibration_btn.setEnabled(false);
            }
        } else if (!Utilities.PROJECT_TAG.equals("ST15")) {
        } else {
            if (((Boolean) this.stateMap.get(Integer.valueOf(0))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(1))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(2))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(3))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(4))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(5))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(6))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(7))).booleanValue() && ((Boolean) this.stateMap.get(Integer.valueOf(8))).booleanValue()) {
                if (this.isShowFinish) {
                    this.calibration_btn.setEnabled(true);
                }
            } else if (this.isShowFinish) {
                this.calibration_btn.setEnabled(false);
            }
        }
    }
}

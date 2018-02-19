package com.yuneec.test;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.yuneec.channelsettings.ChannelSettings;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.uartcontroller.UARTInfoMessage;
import com.yuneec.uartcontroller.UARTInfoMessage.CalibrationRawData;

public class ReadCalibrationData extends Activity implements OnClickListener {
    private static final int C_GREEN = -14503604;
    private static final int C_RED = -3407872;
    private static final int J1_TRAVEL = 1920;
    private static final int J2_TRAVEL = 2500;
    private static final int J3_TRAVEL = 1920;
    private static final int J4_TRAVEL = 2500;
    private static final int K1_TRAVEL = 4095;
    private static final int K2_TRAVEL = 4095;
    private static final int K3_TRAVEL = 4000;
    private static final int K4_TRAVEL = 1200;
    private static final int K5_TRAVEL = 1200;
    private static final int ST10_K_TRAVEL = 1000;
    private static final String TAG = "setValue";
    private int b1_max_value = -1;
    private int b1_min_value = -1;
    private int b2_max_value = -1;
    private int b2_min_value = -1;
    private int b3_max_value = -1;
    private int b3_min_value = -1;
    Button btn_finish;
    Button btn_reset;
    private boolean isReset = false;
    private int j1_max_value = 0;
    private int j1_min_value = 4096;
    private int j2_max_value = 0;
    private int j2_min_value = 4096;
    private int j3_max_value = 0;
    private int j3_min_value = 4096;
    private int j4_max_value = 0;
    private int j4_min_value = 4096;
    private int k1_max_value = 0;
    private int k1_min_value = 4096;
    private int k2_max_value = 0;
    private int k2_min_value = 4096;
    private int k3_max_value = 0;
    private int k3_min_value = 4096;
    private int k4_max_value = 0;
    private int k4_min_value = 4096;
    private int k5_max_value = 0;
    private int k5_min_value = 4096;
    private UARTController mController;
    private Handler mUartHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.obj instanceof UARTInfoMessage) {
                UARTInfoMessage umsg = msg.obj;
                if (ReadCalibrationData.this.isReset) {
                    ReadCalibrationData.this.resetAll();
                }
                switch (umsg.what) {
                    case 20:
                        CalibrationRawData cmsg = (CalibrationRawData) umsg;
                        if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG)) {
                            ReadCalibrationData.this.setValue10(cmsg);
                            return;
                        } else if (Utilities.PROJECT_TAG.equals("ST12")) {
                            ReadCalibrationData.this.setValue12(cmsg);
                            return;
                        } else if (Utilities.PROJECT_TAG.equals("ST15")) {
                            ReadCalibrationData.this.setValue(cmsg);
                            return;
                        } else {
                            return;
                        }
                    default:
                        return;
                }
            }
        }
    };
    TextView rcd_b1_status;
    TextView rcd_b1_value;
    TextView rcd_b2_status;
    TextView rcd_b2_value;
    TextView rcd_b3_status;
    TextView rcd_b3_value;
    TextView rcd_j1_status;
    TextView rcd_j1_value;
    TextView rcd_j2_status;
    TextView rcd_j2_value;
    TextView rcd_j3_status;
    TextView rcd_j3_value;
    TextView rcd_j4_status;
    TextView rcd_j4_value;
    TextView rcd_k1_status;
    TextView rcd_k1_value;
    TextView rcd_k2_status;
    TextView rcd_k2_value;
    TextView rcd_k3_status;
    TextView rcd_k3_value;
    TextView rcd_k4_status;
    TextView rcd_k4_value;
    TextView rcd_k5_status;
    TextView rcd_k5_value;
    TextView rcd_s1_status;
    TextView rcd_s1_value;
    TextView rcd_s2_status;
    TextView rcd_s2_value;
    TextView rcd_s3_status;
    TextView rcd_s3_value;
    TextView rcd_s4_status;
    TextView rcd_s4_value;
    TextView rcd_t1_status;
    TextView rcd_t1_value;
    TextView rcd_t2_status;
    TextView rcd_t2_value;
    TextView rcd_t3_status;
    TextView rcd_t3_value;
    TextView rcd_t4_status;
    TextView rcd_t4_value;
    private int s1_max_value = -1;
    private int s1_mid_value = -1;
    private int s1_min_value = -1;
    private int s2_max_value = -1;
    private int s2_mid_value = -1;
    private int s2_min_value = -1;
    private int s3_max_value = -1;
    private int s3_mid_value = -1;
    private int s3_min_value = -1;
    private int s4_max_value = -1;
    private int s4_min_value = -1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG)) {
            setContentView(R.layout.read_calibration_data_st10);
        } else if (Utilities.PROJECT_TAG.equals("ST12")) {
            setContentView(R.layout.read_calibration_data_st12);
        } else if (Utilities.PROJECT_TAG.equals("ST15")) {
            setContentView(R.layout.read_calibration_data);
        }
        this.rcd_j1_value = (TextView) findViewById(R.id.rcd_j1_value);
        this.rcd_j2_value = (TextView) findViewById(R.id.rcd_j2_value);
        this.rcd_j3_value = (TextView) findViewById(R.id.rcd_j3_value);
        this.rcd_j4_value = (TextView) findViewById(R.id.rcd_j4_value);
        this.rcd_k1_value = (TextView) findViewById(R.id.rcd_k1_value);
        this.rcd_k2_value = (TextView) findViewById(R.id.rcd_k2_value);
        this.rcd_s1_value = (TextView) findViewById(R.id.rcd_s1_value);
        this.rcd_b1_value = (TextView) findViewById(R.id.rcd_b1_value);
        this.rcd_b2_value = (TextView) findViewById(R.id.rcd_b2_value);
        this.rcd_b3_value = (TextView) findViewById(R.id.rcd_b3_value);
        this.rcd_b3_status = (TextView) findViewById(R.id.rcd_b3_status);
        this.rcd_j1_status = (TextView) findViewById(R.id.rcd_j1_status);
        this.rcd_j2_status = (TextView) findViewById(R.id.rcd_j2_status);
        this.rcd_j3_status = (TextView) findViewById(R.id.rcd_j3_status);
        this.rcd_j4_status = (TextView) findViewById(R.id.rcd_j4_status);
        this.rcd_k1_status = (TextView) findViewById(R.id.rcd_k1_status);
        this.rcd_k2_status = (TextView) findViewById(R.id.rcd_k2_status);
        this.rcd_s1_status = (TextView) findViewById(R.id.rcd_s1_status);
        this.rcd_b1_status = (TextView) findViewById(R.id.rcd_b1_status);
        this.rcd_b2_status = (TextView) findViewById(R.id.rcd_b2_status);
        if (Utilities.PROJECT_TAG.equals("ST12")) {
            this.rcd_s2_value = (TextView) findViewById(R.id.rcd_s2_value);
            this.rcd_s2_status = (TextView) findViewById(R.id.rcd_s2_status);
            this.rcd_k3_value = (TextView) findViewById(R.id.rcd_k3_value);
            this.rcd_k3_status = (TextView) findViewById(R.id.rcd_k3_status);
        } else if (Utilities.PROJECT_TAG.equals("ST15")) {
            this.rcd_k3_value = (TextView) findViewById(R.id.rcd_k3_value);
            this.rcd_k4_value = (TextView) findViewById(R.id.rcd_k4_value);
            this.rcd_k5_value = (TextView) findViewById(R.id.rcd_k5_value);
            this.rcd_s2_value = (TextView) findViewById(R.id.rcd_s2_value);
            this.rcd_s3_value = (TextView) findViewById(R.id.rcd_s3_value);
            this.rcd_s4_value = (TextView) findViewById(R.id.rcd_s4_value);
            this.rcd_t1_value = (TextView) findViewById(R.id.rcd_t1_value);
            this.rcd_t2_value = (TextView) findViewById(R.id.rcd_t2_value);
            this.rcd_t3_value = (TextView) findViewById(R.id.rcd_t3_value);
            this.rcd_t4_value = (TextView) findViewById(R.id.rcd_t4_value);
            this.rcd_k3_status = (TextView) findViewById(R.id.rcd_k3_status);
            this.rcd_k4_status = (TextView) findViewById(R.id.rcd_k4_status);
            this.rcd_k5_status = (TextView) findViewById(R.id.rcd_k5_status);
            this.rcd_s2_status = (TextView) findViewById(R.id.rcd_s2_status);
            this.rcd_s3_status = (TextView) findViewById(R.id.rcd_s3_status);
            this.rcd_s4_status = (TextView) findViewById(R.id.rcd_s4_status);
            this.rcd_t1_status = (TextView) findViewById(R.id.rcd_t1_status);
            this.rcd_t2_status = (TextView) findViewById(R.id.rcd_t2_status);
            this.rcd_t3_status = (TextView) findViewById(R.id.rcd_t3_status);
            this.rcd_t4_status = (TextView) findViewById(R.id.rcd_t4_status);
        }
        this.isReset = true;
        this.btn_finish = (Button) findViewById(R.id.read_calibration_finish);
        this.btn_reset = (Button) findViewById(R.id.read_calibration_reset);
        this.btn_finish.setOnClickListener(this);
        this.btn_reset.setOnClickListener(this);
    }

    protected void onResume() {
        super.onResume();
        this.mController = UARTController.getInstance();
        this.mController.registerReaderHandler(this.mUartHandler);
        this.mController.startReading();
    }

    protected void onPause() {
        super.onPause();
        this.mController.stopReading();
        this.mController.registerReaderHandler(null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setValue(com.yuneec.uartcontroller.UARTInfoMessage.CalibrationRawData r9) {
        /*
        r8 = this;
        r7 = 5;
        r6 = 4;
        r5 = 3;
        r4 = 2;
        r3 = 1;
        if (r9 == 0) goto L_0x0010;
    L_0x0007:
        r0 = r9.rawData;
        r0 = r0.size();
        switch(r0) {
            case 1: goto L_0x01d5;
            case 2: goto L_0x01b7;
            case 3: goto L_0x0199;
            case 4: goto L_0x017b;
            case 5: goto L_0x015d;
            case 6: goto L_0x013f;
            case 7: goto L_0x011f;
            case 8: goto L_0x00ff;
            case 9: goto L_0x00dd;
            case 10: goto L_0x00bb;
            case 11: goto L_0x0099;
            case 12: goto L_0x0077;
            case 13: goto L_0x0055;
            case 14: goto L_0x0033;
            case 15: goto L_0x0011;
            case 16: goto L_0x0011;
            case 17: goto L_0x0011;
            case 18: goto L_0x0011;
            case 19: goto L_0x0011;
            case 20: goto L_0x0011;
            case 21: goto L_0x0011;
            case 22: goto L_0x0011;
            case 23: goto L_0x0011;
            case 24: goto L_0x0011;
            default: goto L_0x0010;
        };
    L_0x0010:
        return;
    L_0x0011:
        r0 = r8.rcd_b2_value;
        r1 = r9.rawData;
        r2 = 14;
        r1 = r1.get(r2);
        r1 = java.lang.String.valueOf(r1);
        r0.setText(r1);
        r0 = r9.rawData;
        r1 = 14;
        r0 = r0.get(r1);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        r8.setBStatus(r4, r0);
    L_0x0033:
        r0 = r8.rcd_b1_value;
        r1 = r9.rawData;
        r2 = 13;
        r1 = r1.get(r2);
        r1 = java.lang.String.valueOf(r1);
        r0.setText(r1);
        r0 = r9.rawData;
        r1 = 13;
        r0 = r0.get(r1);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        r8.setBStatus(r3, r0);
    L_0x0055:
        r0 = r8.rcd_s4_value;
        r1 = r9.rawData;
        r2 = 12;
        r1 = r1.get(r2);
        r1 = java.lang.String.valueOf(r1);
        r0.setText(r1);
        r0 = r9.rawData;
        r1 = 12;
        r0 = r0.get(r1);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        r8.setSStatus(r6, r0);
    L_0x0077:
        r0 = r8.rcd_s3_value;
        r1 = r9.rawData;
        r2 = 11;
        r1 = r1.get(r2);
        r1 = java.lang.String.valueOf(r1);
        r0.setText(r1);
        r0 = r9.rawData;
        r1 = 11;
        r0 = r0.get(r1);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        r8.setSStatus(r5, r0);
    L_0x0099:
        r0 = r8.rcd_s2_value;
        r1 = r9.rawData;
        r2 = 10;
        r1 = r1.get(r2);
        r1 = java.lang.String.valueOf(r1);
        r0.setText(r1);
        r0 = r9.rawData;
        r1 = 10;
        r0 = r0.get(r1);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        r8.setSStatus(r4, r0);
    L_0x00bb:
        r0 = r8.rcd_s1_value;
        r1 = r9.rawData;
        r2 = 9;
        r1 = r1.get(r2);
        r1 = java.lang.String.valueOf(r1);
        r0.setText(r1);
        r0 = r9.rawData;
        r1 = 9;
        r0 = r0.get(r1);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        r8.setSStatus(r3, r0);
    L_0x00dd:
        r0 = r8.rcd_k5_value;
        r1 = r9.rawData;
        r2 = 8;
        r1 = r1.get(r2);
        r1 = java.lang.String.valueOf(r1);
        r0.setText(r1);
        r0 = r9.rawData;
        r1 = 8;
        r0 = r0.get(r1);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        r8.setKStatus(r7, r0);
    L_0x00ff:
        r0 = r8.rcd_k4_value;
        r1 = r9.rawData;
        r2 = 7;
        r1 = r1.get(r2);
        r1 = java.lang.String.valueOf(r1);
        r0.setText(r1);
        r0 = r9.rawData;
        r1 = 7;
        r0 = r0.get(r1);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        r8.setKStatus(r6, r0);
    L_0x011f:
        r0 = r8.rcd_k3_value;
        r1 = r9.rawData;
        r2 = 6;
        r1 = r1.get(r2);
        r1 = java.lang.String.valueOf(r1);
        r0.setText(r1);
        r0 = r9.rawData;
        r1 = 6;
        r0 = r0.get(r1);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        r8.setKStatus(r5, r0);
    L_0x013f:
        r0 = r8.rcd_k2_value;
        r1 = r9.rawData;
        r1 = r1.get(r7);
        r1 = java.lang.String.valueOf(r1);
        r0.setText(r1);
        r0 = r9.rawData;
        r0 = r0.get(r7);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        r8.setKStatus(r4, r0);
    L_0x015d:
        r0 = r8.rcd_k1_value;
        r1 = r9.rawData;
        r1 = r1.get(r6);
        r1 = java.lang.String.valueOf(r1);
        r0.setText(r1);
        r0 = r9.rawData;
        r0 = r0.get(r6);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        r8.setKStatus(r3, r0);
    L_0x017b:
        r0 = r8.rcd_j4_value;
        r1 = r9.rawData;
        r1 = r1.get(r5);
        r1 = java.lang.String.valueOf(r1);
        r0.setText(r1);
        r0 = r9.rawData;
        r0 = r0.get(r5);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        r8.setJStatus(r6, r0);
    L_0x0199:
        r0 = r8.rcd_j3_value;
        r1 = r9.rawData;
        r1 = r1.get(r4);
        r1 = java.lang.String.valueOf(r1);
        r0.setText(r1);
        r0 = r9.rawData;
        r0 = r0.get(r4);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        r8.setJStatus(r5, r0);
    L_0x01b7:
        r0 = r8.rcd_j2_value;
        r1 = r9.rawData;
        r1 = r1.get(r3);
        r1 = java.lang.String.valueOf(r1);
        r0.setText(r1);
        r0 = r9.rawData;
        r0 = r0.get(r3);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        r8.setJStatus(r4, r0);
    L_0x01d5:
        r0 = r8.rcd_j1_value;
        r1 = r9.rawData;
        r2 = 0;
        r1 = r1.get(r2);
        r1 = java.lang.String.valueOf(r1);
        r0.setText(r1);
        r0 = r9.rawData;
        r1 = 0;
        r0 = r0.get(r1);
        r0 = (java.lang.Integer) r0;
        r0 = r0.intValue();
        r8.setJStatus(r3, r0);
        goto L_0x0010;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.yuneec.test.ReadCalibrationData.setValue(com.yuneec.uartcontroller.UARTInfoMessage$CalibrationRawData):void");
    }

    private void setValue10(CalibrationRawData raw_data) {
        Log.i(TAG, "@@setValue10@@raw_data.rawData.size()=" + raw_data.rawData.size() + ",J1.get(0)=" + raw_data.rawData.get(0) + ",J2.get(1)=" + raw_data.rawData.get(1) + ",J3.get(2)=" + raw_data.rawData.get(2) + ",J4.get(3)=" + raw_data.rawData.get(3) + ",K1.get(4)=" + raw_data.rawData.get(4) + ",K2.get(5)=" + raw_data.rawData.get(5) + ",S1.get(6)=" + raw_data.rawData.get(6) + ",B1.get(7)=" + raw_data.rawData.get(7) + ",B2.get(8)=" + raw_data.rawData.get(8) + ",B3.get(9)=" + raw_data.rawData.get(9));
        if (raw_data != null) {
            this.rcd_b3_value.setText(String.valueOf(raw_data.rawData.get(9)));
            setBStatus(3, ((Integer) raw_data.rawData.get(9)).intValue());
            this.rcd_b2_value.setText(String.valueOf(raw_data.rawData.get(8)));
            setBStatus(2, ((Integer) raw_data.rawData.get(8)).intValue());
            this.rcd_b1_value.setText(String.valueOf(raw_data.rawData.get(7)));
            setBStatus(1, ((Integer) raw_data.rawData.get(7)).intValue());
            this.rcd_s1_value.setText(String.valueOf(raw_data.rawData.get(6)));
            setSStatus(1, ((Integer) raw_data.rawData.get(6)).intValue());
            this.rcd_k2_value.setText(String.valueOf(raw_data.rawData.get(5)));
            setKStatus(2, ((Integer) raw_data.rawData.get(5)).intValue());
            this.rcd_k1_value.setText(String.valueOf(raw_data.rawData.get(4)));
            setKStatus(1, ((Integer) raw_data.rawData.get(4)).intValue());
            this.rcd_j4_value.setText(String.valueOf(raw_data.rawData.get(3)));
            setJStatus(4, ((Integer) raw_data.rawData.get(3)).intValue());
            this.rcd_j3_value.setText(String.valueOf(raw_data.rawData.get(2)));
            setJStatus(3, ((Integer) raw_data.rawData.get(2)).intValue());
            this.rcd_j2_value.setText(String.valueOf(raw_data.rawData.get(1)));
            setJStatus(2, ((Integer) raw_data.rawData.get(1)).intValue());
            this.rcd_j1_value.setText(String.valueOf(raw_data.rawData.get(0)));
            setJStatus(1, ((Integer) raw_data.rawData.get(0)).intValue());
            return;
        }
        Log.i(TAG, "####raw_data == null####");
    }

    private void setValue12(CalibrationRawData raw_data) {
        Log.i(TAG, "@@setValue12@@raw_data.rawData.size()=" + raw_data.rawData.size() + ",J1.get(0)=" + raw_data.rawData.get(0) + ",J2.get(1)=" + raw_data.rawData.get(1) + ",J3.get(2)=" + raw_data.rawData.get(2) + ",J4.get(3)=" + raw_data.rawData.get(3) + ",K3.get(4)=" + raw_data.rawData.get(4) + ",K1.get(5)=" + raw_data.rawData.get(5) + ",K2.get(6)=" + raw_data.rawData.get(6) + ",S1.get(7)=" + raw_data.rawData.get(7) + ",S2.get(8)=" + raw_data.rawData.get(8) + ",B1.get(9)=" + raw_data.rawData.get(9) + ",B2.get(10)=" + raw_data.rawData.get(10) + ",B3.get(11)=" + raw_data.rawData.get(11));
        if (raw_data != null) {
            this.rcd_b3_value.setText(String.valueOf(raw_data.rawData.get(11)));
            setBStatus(3, ((Integer) raw_data.rawData.get(11)).intValue());
            this.rcd_b2_value.setText(String.valueOf(raw_data.rawData.get(10)));
            setBStatus(2, ((Integer) raw_data.rawData.get(10)).intValue());
            this.rcd_b1_value.setText(String.valueOf(raw_data.rawData.get(9)));
            setBStatus(1, ((Integer) raw_data.rawData.get(9)).intValue());
            this.rcd_s2_value.setText(String.valueOf(raw_data.rawData.get(8)));
            setSStatus(2, ((Integer) raw_data.rawData.get(8)).intValue());
            this.rcd_s1_value.setText(String.valueOf(raw_data.rawData.get(7)));
            setSStatus(1, ((Integer) raw_data.rawData.get(7)).intValue());
            this.rcd_k3_value.setText(String.valueOf(raw_data.rawData.get(6)));
            setKStatus(3, ((Integer) raw_data.rawData.get(6)).intValue());
            this.rcd_k2_value.setText(String.valueOf(raw_data.rawData.get(5)));
            setKStatus(2, ((Integer) raw_data.rawData.get(5)).intValue());
            this.rcd_k1_value.setText(String.valueOf(raw_data.rawData.get(4)));
            setKStatus(1, ((Integer) raw_data.rawData.get(4)).intValue());
            this.rcd_j4_value.setText(String.valueOf(raw_data.rawData.get(3)));
            setJStatus(4, ((Integer) raw_data.rawData.get(3)).intValue());
            this.rcd_j3_value.setText(String.valueOf(raw_data.rawData.get(2)));
            setJStatus(3, ((Integer) raw_data.rawData.get(2)).intValue());
            this.rcd_j2_value.setText(String.valueOf(raw_data.rawData.get(1)));
            setJStatus(2, ((Integer) raw_data.rawData.get(1)).intValue());
            this.rcd_j1_value.setText(String.valueOf(raw_data.rawData.get(0)));
            setJStatus(1, ((Integer) raw_data.rawData.get(0)).intValue());
            return;
        }
        Log.i(TAG, "####raw_data == null####");
    }

    private void setJStatus(int flag, int value) {
        switch (flag) {
            case 1:
                if (value > this.j1_max_value) {
                    this.j1_max_value = value;
                }
                if (value < this.j1_min_value) {
                    this.j1_min_value = value;
                }
                if (this.j1_max_value - this.j1_min_value >= 1920) {
                    this.rcd_j1_status.setBackgroundColor(C_GREEN);
                    this.rcd_j1_status.setText(R.string.str_pass);
                    return;
                }
                return;
            case 2:
                if (value > this.j2_max_value) {
                    this.j2_max_value = value;
                }
                if (value < this.j2_min_value) {
                    this.j2_min_value = value;
                }
                if (this.j2_max_value - this.j2_min_value >= 2500) {
                    this.rcd_j2_status.setBackgroundColor(C_GREEN);
                    this.rcd_j2_status.setText(R.string.str_pass);
                    return;
                }
                return;
            case 3:
                if (value > this.j3_max_value) {
                    this.j3_max_value = value;
                }
                if (value < this.j3_min_value) {
                    this.j3_min_value = value;
                }
                if (this.j3_max_value - this.j3_min_value >= 1920) {
                    this.rcd_j3_status.setBackgroundColor(C_GREEN);
                    this.rcd_j3_status.setText(R.string.str_pass);
                    return;
                }
                return;
            case 4:
                if (value > this.j4_max_value) {
                    this.j4_max_value = value;
                }
                if (value < this.j4_min_value) {
                    this.j4_min_value = value;
                }
                if (this.j4_max_value - this.j4_min_value >= 2500) {
                    this.rcd_j4_status.setBackgroundColor(C_GREEN);
                    this.rcd_j4_status.setText(R.string.str_pass);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void setKStatus(int flag, int value) {
        switch (flag) {
            case 1:
                if (value > this.k1_max_value) {
                    this.k1_max_value = value;
                }
                if (value < this.k1_min_value) {
                    this.k1_min_value = value;
                }
                if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG) || Utilities.PROJECT_TAG.equals("ST12")) {
                    if (this.k1_max_value - this.k1_min_value >= ST10_K_TRAVEL) {
                        this.rcd_k1_status.setBackgroundColor(C_GREEN);
                        this.rcd_k1_status.setText(R.string.str_pass);
                        return;
                    }
                    return;
                } else if (Utilities.PROJECT_TAG.equals("ST15") && this.k1_max_value - this.k1_min_value >= ChannelSettings.STICK_RATE_125_OR_150) {
                    this.rcd_k1_status.setBackgroundColor(C_GREEN);
                    this.rcd_k1_status.setText(R.string.str_pass);
                    return;
                } else {
                    return;
                }
            case 2:
                if (value > this.k2_max_value) {
                    this.k2_max_value = value;
                }
                if (value < this.k2_min_value) {
                    this.k2_min_value = value;
                }
                if (Utilities.PROJECT_TAG.equals(Utilities.PROJECT_TAG) || Utilities.PROJECT_TAG.equals("ST12")) {
                    if (this.k2_max_value - this.k2_min_value >= ST10_K_TRAVEL) {
                        this.rcd_k2_status.setBackgroundColor(C_GREEN);
                        this.rcd_k2_status.setText(R.string.str_pass);
                        return;
                    }
                    return;
                } else if (Utilities.PROJECT_TAG.equals("ST15") && this.k2_max_value - this.k2_min_value >= ChannelSettings.STICK_RATE_125_OR_150) {
                    this.rcd_k2_status.setBackgroundColor(C_GREEN);
                    this.rcd_k2_status.setText(R.string.str_pass);
                    return;
                } else {
                    return;
                }
            case 3:
                if (value > this.k3_max_value) {
                    this.k3_max_value = value;
                }
                if (value < this.k3_min_value) {
                    this.k3_min_value = value;
                }
                if (this.k3_max_value - this.k3_min_value >= K3_TRAVEL) {
                    this.rcd_k3_status.setBackgroundColor(C_GREEN);
                    this.rcd_k3_status.setText(R.string.str_pass);
                    return;
                }
                return;
            case 4:
                if (value > this.k4_max_value) {
                    this.k4_max_value = value;
                }
                if (value < this.k4_min_value) {
                    this.k4_min_value = value;
                }
                if (this.k4_max_value - this.k4_min_value >= 1200) {
                    this.rcd_k4_status.setBackgroundColor(C_GREEN);
                    this.rcd_k4_status.setText(R.string.str_pass);
                    return;
                }
                return;
            case 5:
                if (value > this.k5_max_value) {
                    this.k5_max_value = value;
                }
                if (value < this.k5_min_value) {
                    this.k5_min_value = value;
                }
                if (this.k5_max_value - this.k5_min_value >= 1200) {
                    this.rcd_k5_status.setBackgroundColor(C_GREEN);
                    this.rcd_k5_status.setText(R.string.str_pass);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void setSStatus(int flag, int value) {
        switch (flag) {
            case 1:
                if (!(this.s1_max_value == -1 || this.s1_min_value == -1)) {
                    this.rcd_s1_status.setBackgroundColor(C_GREEN);
                    this.rcd_s1_status.setText(R.string.str_pass);
                }
                if (value == 0) {
                    this.s1_min_value = value;
                    return;
                } else if (value == 1) {
                    this.s1_max_value = value;
                    return;
                } else {
                    return;
                }
            case 2:
                if (Utilities.PROJECT_TAG.equals("ST12")) {
                    if (!(this.s2_max_value == -1 || this.s2_min_value == -1 || this.s2_mid_value == -1)) {
                        this.rcd_s2_status.setBackgroundColor(C_GREEN);
                        this.rcd_s2_status.setText(R.string.str_pass);
                    }
                    if (value == 0) {
                        this.s2_min_value = value;
                        return;
                    } else if (value == 1) {
                        this.s2_mid_value = value;
                        return;
                    } else if (value == 2) {
                        this.s2_max_value = value;
                        return;
                    } else {
                        return;
                    }
                } else if (Utilities.PROJECT_TAG.equals("ST15")) {
                    if (!(this.s2_max_value == -1 || this.s2_min_value == -1 || this.s2_mid_value == -1)) {
                        this.rcd_s1_status.setBackgroundColor(C_GREEN);
                        this.rcd_s1_status.setText(R.string.str_pass);
                    }
                    if (value == 0) {
                        this.s2_min_value = value;
                        return;
                    } else if (value == 1) {
                        this.s2_max_value = value;
                        return;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            case 3:
                if (!(this.s3_max_value == -1 || this.s3_min_value == -1 || this.s3_mid_value == -1)) {
                    this.rcd_s3_status.setBackgroundColor(C_GREEN);
                    this.rcd_s3_status.setText(R.string.str_pass);
                }
                if (value == 0) {
                    this.s3_min_value = value;
                    return;
                } else if (value == 1) {
                    this.s3_mid_value = value;
                    return;
                } else if (value == 2) {
                    this.s3_max_value = value;
                    return;
                } else {
                    return;
                }
            case 4:
                if (!(this.s4_max_value == -1 || this.s4_min_value == -1)) {
                    this.rcd_s4_status.setBackgroundColor(C_GREEN);
                    this.rcd_s4_status.setText(R.string.str_pass);
                }
                if (value == 0) {
                    this.s4_min_value = value;
                    return;
                } else if (value == 1) {
                    this.s4_max_value = value;
                    return;
                } else {
                    return;
                }
            default:
                return;
        }
    }

    private void setBStatus(int flag, int value) {
        switch (flag) {
            case 1:
                if (!(this.b1_max_value == -1 || this.b1_min_value == -1)) {
                    this.rcd_b1_status.setBackgroundColor(C_GREEN);
                    this.rcd_b1_status.setText(R.string.str_pass);
                }
                if (value == 0) {
                    this.b1_min_value = value;
                    return;
                } else if (value == 1) {
                    this.b1_max_value = value;
                    return;
                } else {
                    return;
                }
            case 2:
                if (!(this.b2_max_value == -1 || this.b2_min_value == -1)) {
                    this.rcd_b2_status.setBackgroundColor(C_GREEN);
                    this.rcd_b2_status.setText(R.string.str_pass);
                }
                if (value == 0) {
                    this.b2_min_value = value;
                    return;
                } else if (value == 1) {
                    this.b2_max_value = value;
                    return;
                } else {
                    return;
                }
            case 3:
                if (!(this.b3_max_value == -1 || this.b3_min_value == -1)) {
                    this.rcd_b3_status.setBackgroundColor(C_GREEN);
                    this.rcd_b3_status.setText(R.string.str_pass);
                }
                if (value == 0) {
                    this.b3_min_value = value;
                    return;
                } else if (value == 1) {
                    this.b3_max_value = value;
                    return;
                } else {
                    return;
                }
            default:
                return;
        }
    }

    public void onClick(View v) {
        if (v.equals(this.btn_finish)) {
            finish();
        } else if (v.equals(this.btn_reset)) {
            this.isReset = true;
        }
    }

    public void resetAll() {
        this.j1_max_value = 0;
        this.j2_max_value = 0;
        this.j3_max_value = 0;
        this.j4_max_value = 0;
        this.j1_min_value = 4096;
        this.j2_min_value = 4096;
        this.j3_min_value = 4096;
        this.j4_min_value = 4096;
        this.k1_max_value = 0;
        this.k1_min_value = 4096;
        this.k2_max_value = 0;
        this.k2_min_value = 4096;
        this.k3_max_value = 0;
        this.k3_min_value = 4096;
        this.s1_max_value = -1;
        this.s1_mid_value = -1;
        this.s1_min_value = -1;
        this.s2_max_value = -1;
        this.s2_mid_value = -1;
        this.s2_min_value = -1;
        this.b1_max_value = -1;
        this.b2_max_value = -1;
        this.b1_min_value = -1;
        this.b2_min_value = -1;
        this.b3_max_value = -1;
        this.b3_min_value = -1;
        this.rcd_j1_status.setBackgroundColor(C_RED);
        this.rcd_j1_status.setText(R.string.str_fail);
        this.rcd_j2_status.setBackgroundColor(C_RED);
        this.rcd_j2_status.setText(R.string.str_fail);
        this.rcd_j3_status.setBackgroundColor(C_RED);
        this.rcd_j3_status.setText(R.string.str_fail);
        this.rcd_j4_status.setBackgroundColor(C_RED);
        this.rcd_j4_status.setText(R.string.str_fail);
        this.rcd_k1_status.setBackgroundColor(C_RED);
        this.rcd_k1_status.setText(R.string.str_fail);
        this.rcd_k2_status.setBackgroundColor(C_RED);
        this.rcd_k2_status.setText(R.string.str_fail);
        this.rcd_s1_status.setBackgroundColor(C_RED);
        this.rcd_s1_status.setText(R.string.str_fail);
        this.rcd_b1_status.setBackgroundColor(C_RED);
        this.rcd_b1_status.setText(R.string.str_fail);
        this.rcd_b2_status.setBackgroundColor(C_RED);
        this.rcd_b2_status.setText(R.string.str_fail);
        this.rcd_b3_status.setBackgroundColor(C_RED);
        this.rcd_b3_status.setText(R.string.str_fail);
        if (Utilities.PROJECT_TAG.equals("ST12")) {
            this.b3_max_value = -1;
            this.b3_min_value = -1;
            this.k3_max_value = 0;
            this.k3_min_value = 4096;
            this.s2_max_value = -1;
            this.s2_mid_value = -1;
            this.s2_min_value = -1;
            this.rcd_b3_status.setBackgroundColor(C_RED);
            this.rcd_b3_status.setText(R.string.str_fail);
            this.rcd_k3_status.setBackgroundColor(C_RED);
            this.rcd_k3_status.setText(R.string.str_fail);
            this.rcd_s2_status.setBackgroundColor(C_RED);
            this.rcd_s2_status.setText(R.string.str_fail);
        } else if (Utilities.PROJECT_TAG.equals("ST15")) {
            this.k3_max_value = 0;
            this.k4_max_value = 0;
            this.k5_max_value = 0;
            this.k3_min_value = 4096;
            this.k4_min_value = 4096;
            this.k5_min_value = 4096;
            this.s2_max_value = -1;
            this.s3_max_value = -1;
            this.s4_max_value = -1;
            this.s2_mid_value = -1;
            this.s3_mid_value = -1;
            this.s2_min_value = -1;
            this.s3_min_value = -1;
            this.s4_min_value = -1;
            this.rcd_k3_status.setBackgroundColor(C_RED);
            this.rcd_k3_status.setText(R.string.str_fail);
            this.rcd_k4_status.setBackgroundColor(C_RED);
            this.rcd_k4_status.setText(R.string.str_fail);
            this.rcd_k5_status.setBackgroundColor(C_RED);
            this.rcd_k5_status.setText(R.string.str_fail);
            this.rcd_s2_status.setBackgroundColor(C_RED);
            this.rcd_s2_status.setText(R.string.str_fail);
            this.rcd_s3_status.setBackgroundColor(C_RED);
            this.rcd_s3_status.setText(R.string.str_fail);
            this.rcd_s4_status.setBackgroundColor(C_RED);
            this.rcd_s4_status.setText(R.string.str_fail);
        }
        this.isReset = false;
    }
}

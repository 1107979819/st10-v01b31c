package com.yuneec.flight_settings;

import android.app.Activity;
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
import com.yuneec.uartcontroller.UARTInfoMessage.Channel;
import com.yuneec.widget.SlideVernierView;
import com.yuneec.widget.ThreeSpeedSwitchView;
import com.yuneec.widget.TwoSpeedSwitchView;
import com.yuneec.widget.VernierView;

public class HardwareMonitorST10 extends Activity {
    private static final String TAG = "HardwareMonitor10";
    private TwoSpeedSwitchView hm_view_b1;
    private TwoSpeedSwitchView hm_view_b2;
    private TwoSpeedSwitchView hm_view_b3;
    private VernierView hm_view_j1;
    private VernierView hm_view_j2;
    private VernierView hm_view_j3;
    private VernierView hm_view_j4;
    private SlideVernierView hm_view_k1;
    private SlideVernierView hm_view_k2;
    private ThreeSpeedSwitchView hm_view_s1;
    private Button hw_clear;
    private TextView j1_value;
    private TextView j2_value;
    private TextView j3_value;
    private TextView j4_value;
    private TextView k1_value;
    private TextView k2_value;
    private UARTController mController;
    private Handler mUartHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.obj instanceof UARTInfoMessage) {
                UARTInfoMessage umsg = msg.obj;
                switch (umsg.what) {
                    case 2:
                        Channel cmsg = (Channel) umsg;
                        HardwareMonitorST10.this.setValue(cmsg.channels.size(), cmsg);
                        return;
                    default:
                        return;
                }
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        setContentView(R.layout.hardware_monitor_main_st10);
        this.hm_view_k1 = (SlideVernierView) findViewById(R.id.hm_k1);
        this.hm_view_k2 = (SlideVernierView) findViewById(R.id.hm_k2);
        this.hm_view_b1 = (TwoSpeedSwitchView) findViewById(R.id.hm_b1);
        this.hm_view_b2 = (TwoSpeedSwitchView) findViewById(R.id.hm_b2);
        this.hm_view_b3 = (TwoSpeedSwitchView) findViewById(R.id.hm_b3);
        this.hm_view_s1 = (ThreeSpeedSwitchView) findViewById(R.id.hm_s1);
        this.hm_view_j1 = (VernierView) findViewById(R.id.hm_j1);
        this.hm_view_j2 = (VernierView) findViewById(R.id.hm_j2);
        this.hm_view_j3 = (VernierView) findViewById(R.id.hm_j3);
        this.hm_view_j4 = (VernierView) findViewById(R.id.hm_j4);
        this.k1_value = (TextView) findViewById(R.id.k1_value);
        this.k2_value = (TextView) findViewById(R.id.k2_value);
        this.j1_value = (TextView) findViewById(R.id.j1_value);
        this.j2_value = (TextView) findViewById(R.id.j2_value);
        this.j3_value = (TextView) findViewById(R.id.j3_value);
        this.j4_value = (TextView) findViewById(R.id.j4_value);
        this.hw_clear = (Button) findViewById(R.id.hw_clear);
        this.hw_clear.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HardwareMonitorST10.this.j1_value.setTextColor(-4144960);
                HardwareMonitorST10.this.j2_value.setTextColor(-4144960);
                HardwareMonitorST10.this.j3_value.setTextColor(-4144960);
                HardwareMonitorST10.this.j4_value.setTextColor(-4144960);
            }
        });
    }

    protected void onResume() {
        super.onResume();
        this.mController = UARTController.getInstance();
        this.mController.registerReaderHandler(this.mUartHandler);
        this.mController.startReading();
        if (!Utilities.ensureSimState(this.mController)) {
            Log.e(TAG, "fails to enter sim state");
        }
    }

    protected void onPause() {
        super.onPause();
        if (!Utilities.ensureAwaitState(this.mController)) {
            Log.e(TAG, "fails to enter await state");
        }
        Utilities.UartControllerStandBy(this.mController);
        this.mController = null;
    }

    private void setValue(int num, Channel cmsg) {
        this.hm_view_j1.setValue(vernieDataConversion(((Float) cmsg.channels.get(0)).floatValue()));
        if (vernieDataConversion(((Float) cmsg.channels.get(0)).floatValue()) != 0) {
            this.j1_value.setTextColor(-65536);
        }
        this.j1_value.setText(String.valueOf(vernieDataConversion(((Float) cmsg.channels.get(0)).floatValue())));
        this.hm_view_j2.setValue(vernieDataConversion(((Float) cmsg.channels.get(1)).floatValue()));
        if (vernieDataConversion(((Float) cmsg.channels.get(1)).floatValue()) != 0) {
            this.j2_value.setTextColor(-65536);
        }
        this.j2_value.setText(String.valueOf(vernieDataConversion(((Float) cmsg.channels.get(1)).floatValue())));
        this.hm_view_j3.setValue(vernieDataConversion(((Float) cmsg.channels.get(2)).floatValue()));
        if (vernieDataConversion(((Float) cmsg.channels.get(2)).floatValue()) != 0) {
            this.j3_value.setTextColor(-65536);
        }
        this.j3_value.setText(String.valueOf(vernieDataConversion(((Float) cmsg.channels.get(2)).floatValue())));
        this.hm_view_j4.setValue(vernieDataConversion(((Float) cmsg.channels.get(3)).floatValue()));
        if (vernieDataConversion(((Float) cmsg.channels.get(3)).floatValue()) != 0) {
            this.j4_value.setTextColor(-65536);
        }
        this.j4_value.setText(String.valueOf(vernieDataConversion(((Float) cmsg.channels.get(3)).floatValue())));
        this.hm_view_k1.setValue(slideDataConversion(((Float) cmsg.channels.get(4)).floatValue()));
        this.k1_value.setText(String.valueOf(slideDataConversion(((Float) cmsg.channels.get(4)).floatValue())));
        this.hm_view_k2.setValue(slideDataConversion(((Float) cmsg.channels.get(5)).floatValue()));
        this.k2_value.setText(String.valueOf(slideDataConversion(((Float) cmsg.channels.get(5)).floatValue())));
        this.hm_view_s1.setValue(threeSwitchDataConversion(((Float) cmsg.channels.get(6)).floatValue()));
        this.hm_view_b1.setValue(twoSwitchDataConversion(((Float) cmsg.channels.get(7)).floatValue()));
        this.hm_view_b2.setValue(twoSwitchDataConversion(((Float) cmsg.channels.get(8)).floatValue()));
        this.hm_view_b3.setValue(twoSwitchDataConversion(((Float) cmsg.channels.get(9)).floatValue()));
    }

    private int vernieDataConversion(float data) {
        if (data > 4095.0f) {
            data = 4095.0f;
        }
        if (data < 0.0f) {
            data = 0.0f;
        }
        return (int) ((data / 20.48f) - 100.0f);
    }

    private int slideDataConversion(float data) {
        if (data > 4095.0f) {
            data = 4095.0f;
        }
        if (data < 0.0f) {
            data = 0.0f;
        }
        return (int) (data / 20.48f);
    }

    private int threeSwitchDataConversion(float data) {
        if (data > Utilities.K_MAX) {
            data = Utilities.K_MAX;
        }
        if (data < 0.0f) {
            data = 0.0f;
        }
        return (int) data;
    }

    private int twoSwitchDataConversion(float data) {
        if (data > 1.0f) {
            data = 1.0f;
        }
        if (data < 0.0f) {
            data = 0.0f;
        }
        return (int) data;
    }
}

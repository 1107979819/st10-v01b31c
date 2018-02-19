package com.yuneec.flight_settings;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import com.yuneec.uartcontroller.UARTController;
import com.yuneec.uartcontroller.UARTInfoMessage;
import com.yuneec.uartcontroller.UARTInfoMessage.Channel;
import com.yuneec.uartcontroller.UARTInfoMessage.Trim;
import com.yuneec.widget.KnobsView;
import com.yuneec.widget.SlideVernierView;
import com.yuneec.widget.ThreeSpeedSwitchView;
import com.yuneec.widget.TrimVernierView;
import com.yuneec.widget.TwoSpeedButtonView;
import com.yuneec.widget.VernierView;

public class HardwareMonitor extends Activity {
    private static final String TAG = "HardwareMonitor";
    private TwoSpeedButtonView hm_view_b1;
    private TwoSpeedButtonView hm_view_b2;
    private VernierView hm_view_j1;
    private VernierView hm_view_j2;
    private VernierView hm_view_j3;
    private VernierView hm_view_j4;
    private KnobsView hm_view_k1;
    private KnobsView hm_view_k2;
    private KnobsView hm_view_k3;
    private SlideVernierView hm_view_k4;
    private SlideVernierView hm_view_k5;
    private ThreeSpeedSwitchView hm_view_s1;
    private TwoSpeedButtonView hm_view_s2;
    private TwoSpeedButtonView hm_view_s3;
    private ThreeSpeedSwitchView hm_view_s4;
    private TrimVernierView hm_view_t1;
    private TrimVernierView hm_view_t2;
    private TrimVernierView hm_view_t3;
    private TrimVernierView hm_view_t4;
    private UARTController mController;
    private Handler mUartHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.obj instanceof UARTInfoMessage) {
                UARTInfoMessage umsg = msg.obj;
                switch (umsg.what) {
                    case 2:
                        Channel cmsg = (Channel) umsg;
                        HardwareMonitor.this.hm_view_b2.setValue(HardwareMonitor.this.twoSwitchDataConversion(((Float) cmsg.channels.get(14)).floatValue()));
                        HardwareMonitor.this.hm_view_b1.setValue(HardwareMonitor.this.twoSwitchDataConversion(((Float) cmsg.channels.get(13)).floatValue()));
                        HardwareMonitor.this.hm_view_s4.setValue(HardwareMonitor.this.threeSwitchDataConversion(((Float) cmsg.channels.get(12)).floatValue()));
                        HardwareMonitor.this.hm_view_s3.setValue(HardwareMonitor.this.twoSwitchDataConversion(((Float) cmsg.channels.get(11)).floatValue()));
                        HardwareMonitor.this.hm_view_s2.setValue(HardwareMonitor.this.twoSwitchDataConversion(((Float) cmsg.channels.get(10)).floatValue()));
                        HardwareMonitor.this.hm_view_s1.setValue(HardwareMonitor.this.threeSwitchDataConversion(((Float) cmsg.channels.get(9)).floatValue()));
                        HardwareMonitor.this.hm_view_k5.setValue(HardwareMonitor.this.slideDataConversion(((Float) cmsg.channels.get(8)).floatValue()));
                        HardwareMonitor.this.hm_view_k4.setValue(HardwareMonitor.this.slideDataConversion(((Float) cmsg.channels.get(7)).floatValue()));
                        HardwareMonitor.this.hm_view_k3.setValue(HardwareMonitor.this.knobsDataConversion(((Float) cmsg.channels.get(6)).floatValue()));
                        HardwareMonitor.this.hm_view_k2.setValue(HardwareMonitor.this.knobsDataConversion(((Float) cmsg.channels.get(5)).floatValue()));
                        HardwareMonitor.this.hm_view_k1.setValue(HardwareMonitor.this.knobsDataConversion(((Float) cmsg.channels.get(4)).floatValue()));
                        HardwareMonitor.this.hm_view_j4.setValue(HardwareMonitor.this.vernieDataConversion(((Float) cmsg.channels.get(3)).floatValue()));
                        HardwareMonitor.this.hm_view_j3.setValue(HardwareMonitor.this.vernieDataConversion(((Float) cmsg.channels.get(2)).floatValue()));
                        HardwareMonitor.this.hm_view_j2.setValue(HardwareMonitor.this.vernieDataConversion(((Float) cmsg.channels.get(1)).floatValue()));
                        HardwareMonitor.this.hm_view_j1.setValue(HardwareMonitor.this.vernieDataConversion(((Float) cmsg.channels.get(0)).floatValue()));
                        return;
                    case 4:
                        Trim tmsg = (Trim) umsg;
                        HardwareMonitor.this.hm_view_t1.setValue(HardwareMonitor.this.vernieTrimDataConversion(tmsg.t1));
                        HardwareMonitor.this.hm_view_t2.setValue(HardwareMonitor.this.vernieTrimDataConversion(tmsg.t2));
                        HardwareMonitor.this.hm_view_t3.setValue(HardwareMonitor.this.vernieTrimDataConversion(tmsg.t3));
                        HardwareMonitor.this.hm_view_t4.setValue(HardwareMonitor.this.vernieTrimDataConversion(tmsg.t4));
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
        setContentView(R.layout.hardware_monitor_main);
        this.hm_view_k1 = (KnobsView) findViewById(R.id.hm_k1);
        this.hm_view_k2 = (KnobsView) findViewById(R.id.hm_k2);
        this.hm_view_k3 = (KnobsView) findViewById(R.id.hm_k3);
        this.hm_view_k4 = (SlideVernierView) findViewById(R.id.hm_k4);
        this.hm_view_k5 = (SlideVernierView) findViewById(R.id.hm_k5);
        this.hm_view_b1 = (TwoSpeedButtonView) findViewById(R.id.hm_b1);
        this.hm_view_b2 = (TwoSpeedButtonView) findViewById(R.id.hm_b2);
        this.hm_view_s2 = (TwoSpeedButtonView) findViewById(R.id.hm_s2);
        this.hm_view_s3 = (TwoSpeedButtonView) findViewById(R.id.hm_s3);
        this.hm_view_s1 = (ThreeSpeedSwitchView) findViewById(R.id.hm_s1);
        this.hm_view_s4 = (ThreeSpeedSwitchView) findViewById(R.id.hm_s4);
        this.hm_view_j1 = (VernierView) findViewById(R.id.hm_j1);
        this.hm_view_j2 = (VernierView) findViewById(R.id.hm_j2);
        this.hm_view_j3 = (VernierView) findViewById(R.id.hm_j3);
        this.hm_view_j4 = (VernierView) findViewById(R.id.hm_j4);
        this.hm_view_t1 = (TrimVernierView) findViewById(R.id.hm_t1);
        this.hm_view_t2 = (TrimVernierView) findViewById(R.id.hm_t2);
        this.hm_view_t3 = (TrimVernierView) findViewById(R.id.hm_t3);
        this.hm_view_t4 = (TrimVernierView) findViewById(R.id.hm_t4);
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

    private int knobsDataConversion(float data) {
        if (data > 4095.0f) {
            data = 4095.0f;
        }
        if (data < 0.0f) {
            data = 0.0f;
        }
        return (((int) (data / 15.17037f)) + 225) % 360;
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

    private int vernieTrimDataConversion(float data) {
        if (data > 20.0f) {
            data = 20.0f;
        }
        if (data < -20.0f) {
            data = -20.0f;
        }
        return (int) data;
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

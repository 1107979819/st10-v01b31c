package com.yuneec.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.yuneec.flightmode15.R;
import com.yuneec.uartcontroller.UARTController;

public class ControllerTest extends Activity implements OnClickListener {
    private Intent intent;
    private Button m4_read;
    private TextView m4_version;
    private UARTController mController;
    private Button page1_next;
    private Button ti_read;
    private TextView ti_version;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller_test);
        getWindow().addFlags(128);
        this.m4_version = (TextView) findViewById(R.id.m4_version);
        this.ti_version = (TextView) findViewById(R.id.ti_version);
        this.page1_next = (Button) findViewById(R.id.page1_next);
        this.m4_read = (Button) findViewById(R.id.m4_read);
        this.ti_read = (Button) findViewById(R.id.ti_read);
        this.intent = new Intent(this, CalibrationTest.class);
        this.page1_next.setOnClickListener(this);
        this.m4_read.setOnClickListener(this);
        this.ti_read.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.m4_read:
                if (this.mController != null) {
                    String str_m4 = getResources().getString(R.string.str_m4_version, new Object[]{this.mController.getTransmitterVersion()});
                    if (TextUtils.isEmpty(str_m4)) {
                        str_m4 = getResources().getString(R.string.str_no_date);
                    }
                    this.m4_version.setText(str_m4);
                    this.page1_next.setEnabled(true);
                    return;
                }
                this.m4_version.setText(getResources().getString(R.string.str_m4_version, new Object[]{"UART occupied"}));
                this.page1_next.setEnabled(false);
                return;
            case R.id.ti_read:
                if (this.mController != null) {
                    String str_ti = getResources().getString(R.string.str_ti_version, new Object[]{this.mController.getRadioVersion()});
                    if (TextUtils.isEmpty(str_ti)) {
                        str_ti = getResources().getString(R.string.str_no_date);
                    }
                    this.ti_version.setText(str_ti);
                    this.page1_next.setEnabled(true);
                    return;
                }
                this.ti_version.setText(getResources().getString(R.string.str_ti_version, new Object[]{"UART occupied"}));
                this.page1_next.setEnabled(false);
                return;
            case R.id.page1_next:
                finish();
                startActivity(this.intent);
                return;
            default:
                return;
        }
    }

    protected void onResume() {
        super.onResume();
        this.mController = UARTController.getInstance();
        if (this.mController != null) {
            this.mController.startReading();
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.mController != null) {
            this.mController.stopReading();
            this.mController = null;
        }
    }
}

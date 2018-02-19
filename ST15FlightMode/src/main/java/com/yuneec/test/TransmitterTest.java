package com.yuneec.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.yuneec.IPCameraManager.IPCameraManager;
import com.yuneec.flightmode15.R;
import com.yuneec.uartcontroller.UARTController;

public class TransmitterTest extends Activity implements OnClickListener {
    private Button mBtTransmitTest;
    private Button mChangeRateBtn;
    private UARTController mController;
    private int mCurrentRate = -1;
    private TextView mCurrentRateTxt;
    private EditText mEditTxt;
    private Button mReadRateBtn;
    private boolean mTransmitTesting;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transmitter_test_main);
        getWindow().addFlags(128);
        this.mBtTransmitTest = (Button) findViewById(R.id.bt_transmit_test);
        this.mBtTransmitTest.setOnClickListener(this);
        this.mCurrentRateTxt = (TextView) findViewById(R.id.current_rate_txt);
        this.mChangeRateBtn = (Button) findViewById(R.id.change_rate);
        this.mChangeRateBtn.setOnClickListener(this);
        this.mReadRateBtn = (Button) findViewById(R.id.read_rate);
        this.mReadRateBtn.setOnClickListener(this);
        this.mEditTxt = (EditText) findViewById(R.id.edit_txt);
        this.mTransmitTesting = false;
    }

    protected void onResume() {
        super.onResume();
        this.mController = UARTController.getInstance();
        this.mBtTransmitTest.setText(this.mTransmitTesting ? R.string.str_stop_transmit_test : R.string.str_start_transmit_test);
        this.mCurrentRateTxt.setText(getString(R.string.str_rate_text, new Object[]{IPCameraManager.HTTP_RESPONSE_CODE_UNKNOWN}));
    }

    protected void onPause() {
        super.onPause();
        if (this.mController != null && this.mTransmitTesting) {
            this.mController.exitTransmitTest(true);
            this.mBtTransmitTest.setText(R.string.str_stop_transmit_test);
            this.mTransmitTesting = false;
        }
        if (this.mController != null) {
            this.mController = null;
        }
        this.mCurrentRate = -1;
    }

    private void readRate() {
        int value = -1;
        if (this.mController != null) {
            value = this.mController.readTransmitRate();
        }
        if (value != -1) {
            this.mCurrentRateTxt.setText(getString(R.string.str_rate_text, new Object[]{Integer.valueOf(value)}));
            return;
        }
        this.mCurrentRateTxt.setText(getString(R.string.str_rate_text, new Object[]{IPCameraManager.HTTP_RESPONSE_CODE_UNKNOWN}));
    }

    private void changeRate() {
        String inputValue = this.mEditTxt.getText().toString();
        if (!inputValue.isEmpty()) {
            this.mCurrentRate = Integer.parseInt(inputValue);
            if (this.mController != null) {
                Log.i("wangkang", "res=" + this.mController.writeTransmitRate(this.mCurrentRate));
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_transmit_test:
                if (this.mController == null) {
                    return;
                }
                if (this.mTransmitTesting) {
                    this.mController.exitTransmitTest(true);
                    this.mBtTransmitTest.setText(R.string.str_start_transmit_test);
                    this.mTransmitTesting = false;
                    return;
                }
                this.mController.enterTransmitTest(true);
                this.mBtTransmitTest.setText(R.string.str_stop_transmit_test);
                this.mTransmitTesting = true;
                return;
            case R.id.read_rate:
                readRate();
                return;
            case R.id.change_rate:
                changeRate();
                return;
            default:
                return;
        }
    }
}

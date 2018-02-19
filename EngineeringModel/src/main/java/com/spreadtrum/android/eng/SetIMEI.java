package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import com.android.internal.telephony.PhoneFactory;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

public class SetIMEI extends Activity implements OnClickListener {
    private String mATResponse;
    private String mATline = null;
    private engfetch mEf;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0 && SetIMEI.this.mIMEIInput != null) {
                SetIMEI.this.mIMEIEdit.setText(SetIMEI.this.mIMEIInput);
            } else if (msg.what == 1 && SetIMEI.this.mIMEIInput2 != null) {
                SetIMEI.this.mIMEIEdit2.setText(SetIMEI.this.mIMEIInput2);
            } else if (msg.what == 2) {
                SetIMEI.this.hideSim2();
            }
        }
    };
    private EditText mIMEIEdit;
    private EditText mIMEIEdit2;
    private String mIMEIInput = null;
    private String mIMEIInput2 = null;
    Thread mReadIMEI = new Thread() {
        public void run() {
            SetIMEI.this.mSocketIDs[0] = SetIMEI.this.mEf.engopen(0);
            SetIMEI.this.mIMEIInput = SetIMEI.this.readIMEI(0);
            SetIMEI.this.mHandler.sendEmptyMessage(0);
            if (SetIMEI.this.phoneCount > 1) {
                SetIMEI.this.mSocketIDs[1] = SetIMEI.this.mEf.engopen(1);
                SetIMEI.this.mIMEIInput2 = SetIMEI.this.readIMEI(1);
                SetIMEI.this.mHandler.sendEmptyMessage(1);
                return;
            }
            SetIMEI.this.mHandler.sendEmptyMessage(2);
        }
    };
    private int[] mSocketIDs = new int[2];
    private ByteArrayOutputStream outputBuffer;
    private DataOutputStream outputBufferStream;
    private int phoneCount = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setimei);
        this.mIMEIEdit = (EditText) findViewById(R.id.imei_edit);
        this.mIMEIEdit.setText("");
        this.mIMEIEdit2 = (EditText) findViewById(R.id.imei_edit2);
        this.mIMEIEdit2.setText("");
        this.phoneCount = PhoneFactory.getPhoneCount();
        this.mEf = new engfetch();
        this.mReadIMEI.start();
    }

    private void hideSim2() {
        this.mIMEIEdit2.setVisibility(8);
        findViewById(R.id.wbutton2).setVisibility(8);
        findViewById(R.id.rbutton2).setVisibility(8);
        findViewById(R.id.sim2).setVisibility(8);
    }

    public void onClick(View v) {
        String imei;
        if (v.getId() == R.id.wbutton) {
            if (checkInvalid(0)) {
                imei = this.mIMEIEdit.getText().toString();
                this.mSocketIDs[0] = this.mEf.engopen(0);
                if (writeIMEI(imei, 0)) {
                    showToast("success!");
                } else {
                    showToast("failed!");
                }
            }
        } else if (v.getId() == R.id.rbutton) {
            this.mSocketIDs[0] = this.mEf.engopen(0);
            imei = readIMEI(0);
            if (imei != null) {
                this.mIMEIEdit.setText(imei);
            } else {
                showToast("read IMEI1 failed!");
            }
        }
        if (v.getId() == R.id.wbutton2) {
            if (checkInvalid(1)) {
                imei = this.mIMEIEdit2.getText().toString();
                this.mSocketIDs[1] = this.mEf.engopen(1);
                if (writeIMEI(imei, 1)) {
                    showToast("success!");
                } else {
                    showToast("failed!");
                }
            }
        } else if (v.getId() == R.id.rbutton2) {
            this.mSocketIDs[1] = this.mEf.engopen(1);
            imei = readIMEI(1);
            if (imei != null) {
                this.mIMEIEdit2.setText(imei);
            } else {
                showToast("read IMEI2 failed!");
            }
        }
    }

    private String readIMEI(int i) {
        this.outputBuffer = new ByteArrayOutputStream();
        this.outputBufferStream = new DataOutputStream(this.outputBuffer);
        Log.e("engineeringmodel", "Engmode socket open, id:" + this.mSocketIDs[i]);
        this.mATline = String.format(Locale.US, "%d,%d", new Object[]{Integer.valueOf(1), Integer.valueOf(0)});
        try {
            this.outputBufferStream.writeBytes(this.mATline);
            this.mEf.engwrite(this.mSocketIDs[i], this.outputBuffer.toByteArray(), this.outputBuffer.toByteArray().length);
            byte[] inputBytes = new byte[128];
            this.mATResponse = new String(inputBytes, 0, this.mEf.engread(this.mSocketIDs[i], inputBytes, 128));
            Log.d("engineeringmodel", "Read IMEI: " + i + this.mATResponse);
            if (this.mATResponse.equals("Error")) {
                return null;
            }
            return this.mATResponse;
        } catch (IOException e) {
            Log.e("engineeringmodel", "writeBytes() error!");
            return null;
        }
    }

    private boolean checkInvalid(int i) {
        String imei = null;
        if (i == 0) {
            imei = this.mIMEIEdit.getText().toString();
        } else if (i == 1) {
            imei = this.mIMEIEdit2.getText().toString();
        }
        if (imei == null || imei.equals("")) {
            showToast("empty input!");
            return false;
        } else if (imei.trim().length() == 15) {
            return true;
        } else {
            showToast("must be 15 digits!");
            return false;
        }
    }

    private boolean writeIMEI(String imei, int id) {
        this.outputBuffer = new ByteArrayOutputStream();
        this.outputBufferStream = new DataOutputStream(this.outputBuffer);
        Log.e("engineeringmodel", "Engmode socket open, id:" + this.mSocketIDs[id]);
        this.mATline = String.format(Locale.US, "%d,%d,%s", new Object[]{Integer.valueOf(39), Integer.valueOf(1), "AT+SPIMEI=\"" + imei + "\""});
        try {
            this.outputBufferStream.writeBytes(this.mATline);
            this.mEf.engwrite(this.mSocketIDs[id], this.outputBuffer.toByteArray(), this.outputBuffer.toByteArray().length);
            byte[] inputBytes = new byte[256];
            this.mATResponse = new String(inputBytes, 0, this.mEf.engread(this.mSocketIDs[id], inputBytes, 256));
            if (this.mATResponse.equals("Error")) {
                return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    protected void onDestroy() {
        for (int i = 0; i < 2; i++) {
            this.mEf.engclose(this.mSocketIDs[i]);
        }
        super.onDestroy();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, 0).show();
    }
}

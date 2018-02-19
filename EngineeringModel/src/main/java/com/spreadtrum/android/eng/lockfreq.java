package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class lockfreq extends Activity {
    private Button mButton;
    private Button mButton01;
    private EditText mET01;
    private EditText mET02;
    private EditText mET03;
    private EditText mET04;
    private engfetch mEf;
    private EventHandler mHandler;
    private int mInt01;
    private int mInt02;
    private int mInt03;
    private int mInt04;
    private int sockid = 0;
    private String str = null;

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 13:
                    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
                    DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
                    Log.e("lockfreq", "engopen sockid=" + lockfreq.this.sockid);
                    getEditTextValue();
                    lockfreq.this.str = msg.what + "," + 4 + "," + lockfreq.this.mInt01 + "," + lockfreq.this.mInt02 + "," + lockfreq.this.mInt03 + "," + lockfreq.this.mInt04;
                    try {
                        outputBufferStream.writeBytes(lockfreq.this.str);
                        lockfreq.this.mEf.engwrite(lockfreq.this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                        byte[] inputBytes = new byte[128];
                        String str = new String(inputBytes, 0, lockfreq.this.mEf.engread(lockfreq.this.sockid, inputBytes, 128), Charset.defaultCharset());
                        if (str.equals("OK")) {
                            Toast.makeText(lockfreq.this.getApplicationContext(), "Lock Success.", 0).show();
                            return;
                        } else if (str.equals("ERROR")) {
                            Toast.makeText(lockfreq.this.getApplicationContext(), "Lock Failed.", 0).show();
                            return;
                        } else {
                            Toast.makeText(lockfreq.this.getApplicationContext(), "Unknown", 0).show();
                            return;
                        }
                    } catch (IOException e) {
                        Log.e("lockfreq", "writebytes error");
                        return;
                    }
                default:
                    return;
            }
        }

        private void getEditTextValue() {
            if (lockfreq.this.mET01.getText().toString().equals("")) {
                lockfreq.this.mInt01 = 0;
            } else {
                lockfreq.this.mInt01 = Integer.parseInt(lockfreq.this.mET01.getText().toString());
            }
            if (lockfreq.this.mET02.getText().toString().equals("")) {
                lockfreq.this.mInt02 = 0;
            } else {
                lockfreq.this.mInt02 = Integer.parseInt(lockfreq.this.mET02.getText().toString());
            }
            if (lockfreq.this.mET03.getText().toString().equals("")) {
                lockfreq.this.mInt03 = 0;
            } else {
                lockfreq.this.mInt03 = Integer.parseInt(lockfreq.this.mET03.getText().toString());
            }
            if (lockfreq.this.mET04.getText().toString().equals("")) {
                lockfreq.this.mInt04 = 0;
            } else {
                lockfreq.this.mInt04 = Integer.parseInt(lockfreq.this.mET04.getText().toString());
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockfreq);
        initialpara();
    }

    private void initialpara() {
        this.mET01 = (EditText) findViewById(R.id.editText1);
        this.mET02 = (EditText) findViewById(R.id.editText2);
        this.mET03 = (EditText) findViewById(R.id.editText3);
        this.mET04 = (EditText) findViewById(R.id.editText4);
        clearEditText();
        this.mButton = (Button) findViewById(R.id.lock_button);
        this.mButton01 = (Button) findViewById(R.id.clear_button);
        this.mButton.setText("Lock");
        this.mButton01.setText("Clear");
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        this.mHandler = new EventHandler(Looper.myLooper());
        this.mHandler.removeMessages(0);
        this.mButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                lockfreq.this.mHandler.sendMessage(lockfreq.this.mHandler.obtainMessage(13, 0, 0, Integer.valueOf(0)));
            }
        });
        this.mButton01.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                lockfreq.this.clearEditText();
            }
        });
    }

    private void clearEditText() {
        this.mET01.setText("0");
        this.mET02.setText("0");
        this.mET03.setText("0");
        this.mET04.setText("0");
    }
}

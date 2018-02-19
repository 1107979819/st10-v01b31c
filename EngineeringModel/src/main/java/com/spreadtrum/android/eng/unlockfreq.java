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

public class unlockfreq extends Activity {
    private Button mButton;
    private Button mButton01;
    public EditText mET;
    private engfetch mEf;
    private EventHandler mHandler;
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
                    Log.e("engnetinfo", "engopen sockid=" + unlockfreq.this.sockid);
                    try {
                        unlockfreq.this.str = msg.what + "," + 3 + "," + 1 + "," + Integer.parseInt(unlockfreq.this.mET.getText().toString()) + "," + 10088;
                        try {
                            outputBufferStream.writeBytes(unlockfreq.this.str);
                            unlockfreq.this.mEf.engwrite(unlockfreq.this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                            byte[] inputBytes = new byte[128];
                            String str = new String(inputBytes, 0, unlockfreq.this.mEf.engread(unlockfreq.this.sockid, inputBytes, 128), Charset.defaultCharset());
                            if (str.equals("OK")) {
                                Toast.makeText(unlockfreq.this.getApplicationContext(), "Unlock Success.", 0).show();
                                return;
                            } else if (str.equals("ERROR")) {
                                Toast.makeText(unlockfreq.this.getApplicationContext(), "Unlock Failed.", 0).show();
                                return;
                            } else {
                                Toast.makeText(unlockfreq.this.getApplicationContext(), "Unknown", 0).show();
                                return;
                            }
                        } catch (IOException e) {
                            Log.e("engnetinfo", "writebytes error");
                            return;
                        }
                    } catch (NumberFormatException e2) {
                        if (unlockfreq.this.mET.getText().toString().length() == 0) {
                            Toast.makeText(unlockfreq.this, "please input number", 1).show();
                            return;
                        } else {
                            Toast.makeText(unlockfreq.this, "number is too large or format isn't correct", 1).show();
                            return;
                        }
                    }
                default:
                    return;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.unlockfreq);
        initialpara();
        this.mET = (EditText) findViewById(R.id.editText1);
        this.mButton = (Button) findViewById(R.id.lock_button1);
        this.mButton01 = (Button) findViewById(R.id.clear_button1);
        clearEditText();
        this.mButton.setText("Unlock");
        this.mButton01.setText("Clear");
        this.mButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Log.d("engnetinfo", "before engwrite");
                unlockfreq.this.mHandler.sendMessage(unlockfreq.this.mHandler.obtainMessage(13, 0, 0, Integer.valueOf(0)));
                Log.d("engnetinfo", "after engwrite");
            }
        });
        this.mButton01.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                unlockfreq.this.clearEditText();
            }
        });
    }

    private void clearEditText() {
        this.mET.setText("0");
    }

    private void initialpara() {
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        this.mHandler = new EventHandler(Looper.myLooper());
        this.mHandler.removeMessages(0);
    }
}

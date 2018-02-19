package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.NumberKeyListener;
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

public class sendgprsdata extends Activity {
    private boolean bHasContent;
    private Button mButton;
    private Button mButton01;
    private EditText mET01;
    private EditText mET02;
    private engfetch mEf;
    private EventHandler mHandler;
    private int mInt01;
    private NumberKeyListener numberKeyListener = new NumberKeyListener() {
        private char[] numberChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        public int getInputType() {
            return 3;
        }

        protected char[] getAcceptedChars() {
            return this.numberChars;
        }
    };
    private int sockid = 0;
    private String str = null;
    private String strInput = "";

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 49:
                    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
                    DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
                    if (getEditTextValue()) {
                        Log.e("sendgprsdata", "engopen sockid=" + sendgprsdata.this.sockid);
                        if (sendgprsdata.this.bHasContent) {
                            sendgprsdata.this.str = msg.what + "," + 3 + "," + 100 + "," + 1 + "," + sendgprsdata.this.strInput;
                        } else {
                            sendgprsdata.this.str = msg.what + "," + 1 + "," + sendgprsdata.this.mInt01;
                        }
                        try {
                            outputBufferStream.writeBytes(sendgprsdata.this.str);
                            sendgprsdata.this.mEf.engwrite(sendgprsdata.this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                            byte[] inputBytes = new byte[128];
                            String str = new String(inputBytes, 0, sendgprsdata.this.mEf.engread(sendgprsdata.this.sockid, inputBytes, 128), Charset.defaultCharset());
                            if (str.equals("OK")) {
                                Toast.makeText(sendgprsdata.this.getApplicationContext(), "Send Success.", 0).show();
                                return;
                            } else if (str.equals("ERROR")) {
                                Toast.makeText(sendgprsdata.this.getApplicationContext(), "Send Failed.", 0).show();
                                return;
                            } else {
                                Toast.makeText(sendgprsdata.this.getApplicationContext(), "Unknown", 0).show();
                                return;
                            }
                        } catch (IOException e) {
                            Log.e("sendgprsdata", "writebytes error");
                            return;
                        }
                    }
                    return;
                default:
                    return;
            }
        }

        private boolean getEditTextValue() {
            if (sendgprsdata.this.mET01.getText().toString().equals("")) {
                sendgprsdata.this.mInt01 = 0;
            } else {
                try {
                    sendgprsdata.this.mInt01 = Integer.parseInt(sendgprsdata.this.mET01.getText().toString());
                } catch (NumberFormatException e) {
                    sendgprsdata.this.mInt01 = 0;
                    sendgprsdata.this.mET01.selectAll();
                    sendgprsdata.this.DisplayToast("data number is not a valid number");
                    return false;
                }
            }
            if (sendgprsdata.this.mET02.getText().toString().equals("")) {
                sendgprsdata.this.bHasContent = false;
            } else {
                sendgprsdata.this.strInput = sendgprsdata.this.mET02.getText().toString();
            }
            return true;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendgprsdata);
        initialpara();
    }

    private void initialpara() {
        this.bHasContent = true;
        this.mET01 = (EditText) findViewById(R.id.editText1);
        this.mET02 = (EditText) findViewById(R.id.editText2);
        this.mET01.setKeyListener(this.numberKeyListener);
        this.mET02.setKeyListener(this.numberKeyListener);
        clearEditText();
        this.mButton = (Button) findViewById(R.id.send_button);
        this.mButton01 = (Button) findViewById(R.id.clear_button1);
        this.mButton.setText("Send Data");
        this.mButton01.setText("Clear Data");
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        this.mHandler = new EventHandler(Looper.myLooper());
        this.mHandler.removeMessages(0);
        this.mButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sendgprsdata.this.mHandler.sendMessage(sendgprsdata.this.mHandler.obtainMessage(49, 0, 0, Integer.valueOf(0)));
            }
        });
        this.mButton01.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sendgprsdata.this.clearEditText();
            }
        });
    }

    private void clearEditText() {
        this.mET01.setText("0");
        this.mET02.setText("0");
    }

    private void DisplayToast(String str) {
        Toast mToast = Toast.makeText(this, str, 0);
        mToast.setGravity(80, 0, 100);
        mToast.show();
    }
}

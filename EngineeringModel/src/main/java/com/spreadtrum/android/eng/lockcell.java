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
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class lockcell extends Activity {
    private static EditText[] mET = new EditText[16];
    private static String[] ss = new String[16];
    private Button mButton;
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
                case 14:
                    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
                    DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
                    Log.e("engnetinfo", "engopen sockid=" + lockcell.this.sockid);
                    lockcell.this.str = msg.what + "," + 0;
                    try {
                        outputBufferStream.writeBytes(lockcell.this.str);
                        lockcell.this.mEf.engwrite(lockcell.this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                        byte[] inputBytes = new byte[128];
                        lockcell.ss = new String(inputBytes, 0, lockcell.this.mEf.engread(lockcell.this.sockid, inputBytes, 128), Charset.defaultCharset()).split(",");
                        for (int i = 0; i < lockcell.ss.length; i++) {
                            lockcell.mET[i].setText(lockcell.ss[i]);
                        }
                        return;
                    } catch (IOException e) {
                        Log.e("engnetinfo", "writebytes error");
                        return;
                    }
                default:
                    return;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockcell);
        mET[0] = (EditText) findViewById(R.id.editText1);
        mET[1] = (EditText) findViewById(R.id.editText11);
        mET[2] = (EditText) findViewById(R.id.editText12);
        mET[3] = (EditText) findViewById(R.id.editText13);
        mET[4] = (EditText) findViewById(R.id.editText2);
        mET[5] = (EditText) findViewById(R.id.editText21);
        mET[6] = (EditText) findViewById(R.id.editText22);
        mET[7] = (EditText) findViewById(R.id.editText23);
        mET[8] = (EditText) findViewById(R.id.editText3);
        mET[9] = (EditText) findViewById(R.id.editText31);
        mET[10] = (EditText) findViewById(R.id.editText32);
        mET[11] = (EditText) findViewById(R.id.editText33);
        mET[12] = (EditText) findViewById(R.id.editText4);
        mET[13] = (EditText) findViewById(R.id.editText41);
        mET[14] = (EditText) findViewById(R.id.editText42);
        mET[15] = (EditText) findViewById(R.id.editText43);
        this.mButton = (Button) findViewById(R.id.Button_get);
        this.mButton.setText("Get data");
        this.mButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                lockcell.this.initialpara();
            }
        });
    }

    private void initialpara() {
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        this.mHandler = new EventHandler(Looper.myLooper());
        this.mHandler.removeMessages(0);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(14, 0, 0, Integer.valueOf(0)));
    }
}

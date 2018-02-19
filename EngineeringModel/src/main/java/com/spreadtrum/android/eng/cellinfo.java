package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class cellinfo extends Activity {
    private engfetch mEf;
    private EventHandler mHandler;
    private int sockid = 0;
    private String str = null;
    public TextView tv;

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
                    DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
                    Log.e("engnetinfo", "engopen sockid=" + cellinfo.this.sockid);
                    cellinfo.this.str = msg.what + "," + new String("?");
                    try {
                        outputBufferStream.writeBytes(cellinfo.this.str);
                        cellinfo.this.mEf.engwrite(cellinfo.this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                        byte[] inputBytes = new byte[128];
                        cellinfo.this.tv.setText(new String(inputBytes, 0, cellinfo.this.mEf.engread(cellinfo.this.sockid, inputBytes, 128), Charset.defaultCharset()));
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
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        this.mHandler = new EventHandler(Looper.myLooper());
        this.mHandler.removeMessages(0);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3, 0, 0, Integer.valueOf(0)));
        this.tv = new TextView(this);
        this.tv.setText("");
        setContentView(this.tv);
    }
}

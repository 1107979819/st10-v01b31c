package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class psverinfo extends Activity {
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 44) {
                psverinfo.this.mTextView01.setText(msg.obj);
            }
        }
    };
    private engfetch mEf;
    private TextView mTextView01;
    Runnable runnable = new Runnable() {
        public void run() {
            Log.e("psverinfo", "run is Runnable~~~");
            Message msg = psverinfo.this.handler.obtainMessage();
            msg.what = 44;
            msg.obj = psverinfo.this.writeAndReadDateFromServer(msg.what);
            Log.e("psverinfo", "msg.obj = <" + msg.obj + ">");
            psverinfo.this.handler.sendMessage(msg);
        }
    };
    private int sockid = 0;
    private String str;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.psverinfo);
        this.mTextView01 = (TextView) findViewById(R.id.ps01_view);
        this.mTextView01.setText("Wait...");
        initialpara();
        Thread myThread = new Thread(this.runnable);
        myThread.setName("new thread");
        myThread.start();
        Log.e("psverinfo", "thread name =" + myThread.getName() + " id =" + myThread.getId());
    }

    private void initialpara() {
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
    }

    private String writeAndReadDateFromServer(int what) {
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
        Log.e("psverinfo", "engopen sockid=" + this.sockid);
        this.str = what + "," + 1 + "," + 0;
        try {
            outputBufferStream.writeBytes(this.str);
            this.mEf.engwrite(this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
            byte[] inputBytes = new byte[128];
            String str123 = new String(inputBytes, 0, this.mEf.engread(this.sockid, inputBytes, 128), Charset.defaultCharset());
            Log.e("psverinfo", "str123" + str123);
            return str123;
        } catch (IOException e) {
            Log.e("psverinfo", "writebytes error");
            return "error";
        }
    }
}

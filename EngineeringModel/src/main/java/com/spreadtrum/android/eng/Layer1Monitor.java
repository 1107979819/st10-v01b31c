package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class Layer1Monitor extends Activity {
    private String mATline;
    private engfetch mEf;
    private Handler mHandler = new Handler();
    private boolean mRunnable = false;
    private int mSocketID;
    private TextView mTextView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layer1monitor);
        this.mTextView = (TextView) findViewById(R.id.text_view);
        this.mEf = new engfetch();
        this.mSocketID = this.mEf.engopen();
        this.mRunnable = true;
        new Thread(new Runnable() {
            public void run() {
                while (Layer1Monitor.this.mRunnable) {
                    final String text = Layer1Monitor.this.getL1MonitorText();
                    Layer1Monitor.this.mHandler.post(new Runnable() {
                        public void run() {
                            Layer1Monitor.this.mTextView.setText(text);
                        }
                    });
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private String getL1MonitorText() {
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
        this.mATline = 103 + "," + 0;
        try {
            outputBufferStream.writeBytes(this.mATline);
            this.mEf.engwrite(this.mSocketID, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
            byte[] inputBytes = new byte[2048];
            return new String(inputBytes, 0, this.mEf.engread(this.mSocketID, inputBytes, 2048), Charset.defaultCharset());
        } catch (IOException e) {
            Log.e("Layer1Monitor", "writeBytes() error!");
            return "";
        }
    }

    protected void onDestroy() {
        this.mRunnable = false;
        super.onDestroy();
    }
}

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

public class adcCalibrateInfo extends Activity {
    private engfetch mEf;
    private EventHandler mHandler;
    private int sockid = 0;
    private String str = null;
    private TextView txtViewlabel01;

    private class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 70:
                    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
                    DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
                    Log.e("adcCalibrateInfo", "engopen sockid=" + adcCalibrateInfo.this.sockid);
                    adcCalibrateInfo.this.str = msg.what + "," + 3 + "," + 0 + "," + 0 + "," + 3;
                    try {
                        outputBufferStream.writeBytes(adcCalibrateInfo.this.str);
                        adcCalibrateInfo.this.mEf.engwrite(adcCalibrateInfo.this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                        byte[] inputBytes = new byte[1024];
                        adcCalibrateInfo.this.txtViewlabel01.setText(new String(inputBytes, 0, adcCalibrateInfo.this.mEf.engread(adcCalibrateInfo.this.sockid, inputBytes, 1024), Charset.defaultCharset()));
                        return;
                    } catch (IOException e) {
                        Log.e("adcCalibrateInfo", "writebytes error");
                        return;
                    }
                default:
                    return;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adcinfo);
        this.txtViewlabel01 = (TextView) findViewById(R.id.adc_id);
        this.txtViewlabel01.setTextSize(20.0f);
        initial();
    }

    private void initial() {
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        this.mHandler = new EventHandler(Looper.myLooper());
        this.mHandler.removeMessages(0);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(70, 0, 0, Integer.valueOf(0)));
    }
}

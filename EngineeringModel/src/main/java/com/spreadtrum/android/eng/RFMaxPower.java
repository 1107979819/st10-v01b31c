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

public class RFMaxPower extends Activity {
    private engfetch mEf;
    private Handler mUiThread = new Handler();
    private int sockid = 0;
    private String str = null;
    private TextView txtViewlabel01;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rfmaxpower);
        Log.e("weicl", "70:==================================aaaaaaaaaaaaaaaaaa===============");
        this.txtViewlabel01 = (TextView) findViewById(R.id.rfmaxpower_id);
        this.txtViewlabel01.setTextSize(20.0f);
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        new Thread(new Runnable() {
            public void run() {
                RFMaxPower.this.requestEvent(200);
            }
        }).start();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private void requestEvent(int code) {
        switch (code) {
            case 200:
                ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
                DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
                this.str = code + "," + 0;
                try {
                    outputBufferStream.writeBytes(this.str);
                    this.mEf.engwrite(this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                    byte[] inputBytes = new byte[256];
                    final String str = new String(inputBytes, 0, this.mEf.engread(this.sockid, inputBytes, 256), Charset.defaultCharset());
                    this.mUiThread.post(new Runnable() {
                        public void run() {
                            RFMaxPower.this.txtViewlabel01.setText(str);
                            Log.e("weicl", "70:=================================================");
                        }
                    });
                    return;
                } catch (IOException e) {
                    Log.e("RFMaxPower", "writebytes error");
                    return;
                }
            default:
                return;
        }
    }
}

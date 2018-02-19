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

public class versioninfo extends Activity {
    private engfetch mEf;
    private Handler mUiThread = new Handler();
    private int sockid = 0;
    private String str = null;
    private TextView txtViewlabel01;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.version);
        this.txtViewlabel01 = (TextView) findViewById(R.id.version_id);
        this.txtViewlabel01.setText(R.string.imeiinfo);
        this.txtViewlabel01.setTextSize(20.0f);
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        new Thread(new Runnable() {
            public void run() {
                versioninfo.this.requestEvent(57);
            }
        }).start();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    private void requestEvent(int code) {
        switch (code) {
            case 57:
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
                            versioninfo.this.txtViewlabel01.setText(str);
                        }
                    });
                    return;
                } catch (IOException e) {
                    Log.e("versioninfo", "writebytes error");
                    return;
                }
            default:
                return;
        }
    }
}

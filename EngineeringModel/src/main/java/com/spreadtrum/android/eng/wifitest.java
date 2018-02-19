package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class wifitest extends Activity {
    private engfetch mEf;
    private int sockid = 0;
    private String str = null;
    public TextView tv;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
        this.str = "CMD:" + "WIFI";
        try {
            outputBufferStream.writeBytes(this.str);
            this.mEf.engwrite(this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
            this.mEf.engclose(this.sockid);
            this.tv = new TextView(this);
            this.tv.setText("Enter wifi test mode");
            setContentView(this.tv);
        } catch (IOException e) {
            Log.e("engnetinfo", "writebytes error");
        }
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}

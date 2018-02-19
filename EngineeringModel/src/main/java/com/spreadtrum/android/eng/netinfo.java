package com.spreadtrum.android.eng;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class netinfo extends Activity {
    private engfetch mEf;
    private Handler mUiThread = new Handler();
    private int sockid = 0;
    private String str = "";
    private TextView tv1;
    private TextView tv1_1;
    private TextView tv2;
    private TextView tv2_2;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netinfo);
        this.tv1 = (TextView) findViewById(R.id.scell_title);
        this.tv2 = (TextView) findViewById(R.id.ncell_title);
        this.tv1_1 = (TextView) findViewById(R.id.scell_value);
        this.tv2_2 = (TextView) findViewById(R.id.ncell_value);
        this.mEf = new engfetch();
        this.sockid = this.mEf.engopen();
        new Thread(new Runnable() {
            public void run() {
                if (netinfo.this.checkCurrentNetworkState()) {
                    netinfo.this.requestEvent(101, 0, 1);
                    netinfo.this.requestEvent(101, 0, 2);
                    return;
                }
                netinfo.this.mUiThread.post(new Runnable() {
                    public void run() {
                        netinfo.this.tv1.setText("network is unavailable");
                    }
                });
                Log.d("netinfo", "network is unavailable");
            }
        }).start();
    }

    private void requestEvent(int code, int arg1, int arg2) {
        switch (code) {
            case 101:
                ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
                DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);
                Log.e("netinfo", "engopen sockid=" + this.sockid);
                this.str = code + "," + 2 + "," + arg1 + "," + arg2;
                try {
                    outputBufferStream.writeBytes(this.str);
                    this.mEf.engwrite(this.sockid, outputBuffer.toByteArray(), outputBuffer.toByteArray().length);
                    byte[] inputBytes = new byte[256];
                    String str = new String(inputBytes, 0, this.mEf.engread(this.sockid, inputBytes, 256), Charset.defaultCharset()).trim();
                    Log.d("netinfo", "get result : " + str);
                    String s;
                    String[] strs;
                    if (arg2 == 1) {
                        s = "";
                        if (str.indexOf(",") != -1) {
                            strs = str.split(",");
                            s = "        " + strs[0] + "," + strs[1] + "," + strs[5] + "," + strs[6];
                            updateUi(this.tv1_1, s);
                            return;
                        }
                        updateUi(this.tv1_1, "    " + str);
                        return;
                    } else if (arg2 != 2) {
                        return;
                    } else {
                        if (str.indexOf(",") != -1) {
                            strs = str.split(",");
                            s = "";
                            int nums = strs.length / 7;
                            for (int i = 0; i < nums; i++) {
                                s = s + ("        " + strs[i * 7] + "," + strs[(i * 7) + 1] + "," + strs[(i * 7) + 5] + "," + strs[(i * 7) + 6]);
                            }
                            updateUi(this.tv2_2, s);
                            return;
                        }
                        updateUi(this.tv2_2, "    " + str);
                        return;
                    }
                } catch (IOException e) {
                    Log.e("netinfo", "writebytes error");
                    return;
                }
            default:
                return;
        }
    }

    private void updateUi(final TextView tv, final String msg) {
        this.mUiThread.post(new Runnable() {
            public void run() {
                netinfo.this.tv1.setText("SCELL");
                netinfo.this.tv2.setText("NCELL");
                netinfo.this.tv1.setTextSize(20.0f);
                netinfo.this.tv1_1.setTextSize(20.0f);
                netinfo.this.tv2.setTextSize(20.0f);
                netinfo.this.tv2_2.setTextSize(20.0f);
                tv.setText(msg);
            }
        });
    }

    private boolean checkCurrentNetworkState() {
        if (((ConnectivityManager) getSystemService("connectivity")).getNetworkInfo(0).getState() == State.DISCONNECTED) {
            return false;
        }
        return true;
    }
}

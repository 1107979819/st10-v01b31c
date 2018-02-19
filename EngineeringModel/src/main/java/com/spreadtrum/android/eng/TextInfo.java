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

public class TextInfo extends Activity {
    private String mATResponse;
    private String mATline;
    private engfetch mEf;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    TextInfo.this.mTextView.setText("ERROR");
                    return;
                case 1:
                    TextInfo.this.mTextView.setText("NULL");
                    return;
                case 2:
                    TextInfo.this.mTextView.setText(TextInfo.this.mATResponse);
                    return;
                default:
                    return;
            }
        }
    };
    private int mSocketID;
    private int mStartN;
    private TextView mTextView;
    private ByteArrayOutputStream outputBuffer;
    private DataOutputStream outputBufferStream;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.textinfo);
        this.mTextView = (TextView) findViewById(R.id.text_view);
        this.mEf = new engfetch();
        this.mStartN = getIntent().getIntExtra("text_info", 0);
        switch (this.mStartN) {
            case 1:
                setTitle(R.string.sim_forbid_plmn);
                this.mSocketID = this.mEf.engopen();
                break;
            case 2:
                setTitle(R.string.sim_equal_plmn);
                this.mSocketID = this.mEf.engopen();
                break;
            default:
                Log.e("TextInfo", "mStartN:" + this.mStartN);
                break;
        }
        getDisplayText();
    }

    private void getDisplayText() {
        new Thread(new Runnable() {
            public void run() {
                TextInfo.this.outputBuffer = new ByteArrayOutputStream();
                TextInfo.this.outputBufferStream = new DataOutputStream(TextInfo.this.outputBuffer);
                switch (TextInfo.this.mStartN) {
                    case 1:
                        TextInfo.this.mATline = 111 + "," + 0;
                        break;
                    case 2:
                        TextInfo.this.mATline = 112 + "," + 0;
                        break;
                    default:
                        TextInfo.this.mHandler.sendEmptyMessage(0);
                        break;
                }
                Log.d("TextInfo", "mATline :" + TextInfo.this.mATline);
                try {
                    TextInfo.this.outputBufferStream.writeBytes(TextInfo.this.mATline);
                } catch (IOException e) {
                    Log.e("TextInfo", "writeBytes() error!");
                    TextInfo.this.mHandler.sendEmptyMessage(0);
                }
                TextInfo.this.mEf.engwrite(TextInfo.this.mSocketID, TextInfo.this.outputBuffer.toByteArray(), TextInfo.this.outputBuffer.toByteArray().length);
                byte[] inputBytes = new byte[512];
                TextInfo.this.mATResponse = new String(inputBytes, 0, TextInfo.this.mEf.engread(TextInfo.this.mSocketID, inputBytes, 512), Charset.defaultCharset());
                if (TextInfo.this.mATResponse.length() >= 10) {
                    TextInfo.this.mATResponse = TextInfo.this.mATResponse.substring(10);
                }
                Log.e("TextInfo", "mATResponse:" + TextInfo.this.mATResponse);
                if (TextInfo.this.mATResponse.length() > 0) {
                    TextInfo.this.mHandler.sendEmptyMessage(2);
                } else {
                    TextInfo.this.mHandler.sendEmptyMessage(1);
                }
            }
        }).start();
    }
}

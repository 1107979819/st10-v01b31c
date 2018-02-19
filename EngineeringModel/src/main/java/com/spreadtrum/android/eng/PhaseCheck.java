package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import java.nio.charset.Charset;

public class PhaseCheck extends Activity {
    private engfetch mEf;
    private String mText;
    private TextView mTextView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phasecheck);
        this.mTextView = (TextView) findViewById(R.id.text_view);
        this.mEf = new engfetch();
        byte[] inputBytes = new byte[2048];
        this.mText = new String(inputBytes, 0, this.mEf.enggetphasecheck(inputBytes, 2048), Charset.defaultCharset());
        String str = getIntent().getStringExtra("textFilter");
        if (str == null || !str.equals("filter")) {
            this.mTextView.setText(this.mText);
            return;
        }
        this.mTextView.setText(this.mText.replaceAll("(?s)DOWNLOAD.*", "").trim());
    }
}

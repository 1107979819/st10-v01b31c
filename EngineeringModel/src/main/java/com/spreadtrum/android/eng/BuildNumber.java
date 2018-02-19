package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

public class BuildNumber extends Activity {
    private TextView tv = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buildnum);
        this.tv = (TextView) findViewById(R.id.buildnuminfo);
        this.tv.setText(Build.DISPLAY);
    }
}

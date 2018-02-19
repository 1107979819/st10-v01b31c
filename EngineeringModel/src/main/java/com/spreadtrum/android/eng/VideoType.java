package com.spreadtrum.android.eng;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class VideoType extends Activity {
    RadioButton mBtn1;
    RadioButton mBtn2;
    RadioButton mBtn3;
    RadioButton mBtn4;
    RadioGroup mGroup;
    private OnCheckedChangeListener mListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.radio1:
                    if (System.getProperty("debug.videophone.videotype") != null) {
                        Toast.makeText(VideoType.this, "select H263 Prefer successful", 0).show();
                    }
                    VideoType.this.setValues(1);
                    return;
                case R.id.radio2:
                    Toast.makeText(VideoType.this, "select MPEG4 Prefer successful", 0).show();
                    VideoType.this.setValues(2);
                    return;
                case R.id.radio3:
                    Toast.makeText(VideoType.this, "select H263 Only successful", 0).show();
                    VideoType.this.setValues(3);
                    return;
                case R.id.radio4:
                    Toast.makeText(VideoType.this, "select MPEG4 Only successful", 0).show();
                    VideoType.this.setValues(4);
                    return;
                default:
                    return;
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videotype);
        this.mGroup = (RadioGroup) findViewById(R.id.group);
        this.mGroup.setOnCheckedChangeListener(this.mListener);
        this.mBtn1 = (RadioButton) findViewById(R.id.radio1);
        this.mBtn2 = (RadioButton) findViewById(R.id.radio2);
        this.mBtn3 = (RadioButton) findViewById(R.id.radio3);
        this.mBtn4 = (RadioButton) findViewById(R.id.radio4);
        this.mBtn1.setText("H263 Prefer");
        this.mBtn2.setText("MPEG4 Prefer");
        this.mBtn3.setText("H263 Only");
        this.mBtn4.setText("MPEG4 Only");
        this.mGroup.setOnCheckedChangeListener(this.mListener);
        initView();
    }

    private void initView() {
        int values = getValues();
        if (values != -1) {
            switch (values) {
                case 1:
                    this.mBtn1.setChecked(true);
                    return;
                case 2:
                    this.mBtn2.setChecked(true);
                    return;
                case 3:
                    this.mBtn3.setChecked(true);
                    return;
                case 4:
                    this.mBtn4.setChecked(true);
                    return;
                default:
                    this.mBtn1.setChecked(true);
                    return;
            }
        }
    }

    private void setValues(int value) {
        System.setProperty("debug.videophone.videotype", String.valueOf(value));
    }

    private int getValues() {
        int value = -1;
        String v = System.getProperty("debug.videophone.videotype", "1");
        try {
            value = Integer.valueOf(v).intValue();
        } catch (Exception e) {
            Log.d("VideoType", "Pase " + v + " to Integer Error !");
        }
        return value;
    }
}

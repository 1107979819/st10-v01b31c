package com.yuneec.flightmode15;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

public class SpeciallyChannelSender extends Dialog implements OnClickListener {
    private int btnNumber;
    private Button mFollowBtn;
    private Button mNormalBtn;
    private onButtonClickListener mOnButtonClickListener;
    private Button mTrackBtn;

    public interface onButtonClickListener {
        void onButtonClick(int i);
    }

    public SpeciallyChannelSender(Context context) {
        super(context, R.style.base_dialog_style);
        setContentView(R.layout.specially_channel_sender_layout);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mNormalBtn = (Button) findViewById(R.id.fmode_normal);
        this.mNormalBtn.setOnClickListener(this);
        this.mFollowBtn = (Button) findViewById(R.id.fmode_follow);
        this.mFollowBtn.setOnClickListener(this);
        this.mTrackBtn = (Button) findViewById(R.id.fmode_track);
        this.mTrackBtn.setOnClickListener(this);
    }

    public void adjustHeight(int newHeight) {
        LayoutParams params = getWindow().getAttributes();
        params.height = newHeight;
        getWindow().setAttributes(params);
    }

    public void setOnButtonClicked(onButtonClickListener onButtonCickListener) {
        this.mOnButtonClickListener = onButtonCickListener;
    }

    public void onClick(View v) {
        if (v.equals(this.mNormalBtn)) {
            this.btnNumber = 6;
        } else if (v.equals(this.mFollowBtn)) {
            this.btnNumber = 7;
        } else if (v.equals(this.mTrackBtn)) {
            this.btnNumber = 8;
        }
        this.mOnButtonClickListener.onButtonClick(this.btnNumber);
    }
}

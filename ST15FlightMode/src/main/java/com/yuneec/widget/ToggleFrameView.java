package com.yuneec.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.WbIsoChangedListener;

public class ToggleFrameView extends FrameLayout implements WbIsoChangedListener {
    private int currentWbModeIndex;
    private TextView isoModeText;
    private ImageView isoToggleView;
    private Context mContext;
    private ToggleOnClickListener toggleOnClickListener;
    private ImageView wbModeView;
    private ImageView wbToggleView;

    public interface ToggleOnClickListener {
        void isoItemOnClick(View view);

        void wbItemOnClick(View view);
    }

    public ToggleFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initView();
        initListener();
    }

    private void initView() {
        setBackgroundResource(R.drawable.toggle_bg);
        this.wbModeView = new ImageView(this.mContext);
        this.wbModeView.setImageResource(R.drawable.auto_pressed);
        LayoutParams params = new LayoutParams(60, 60);
        params.setMargins(2, 32, 0, 0);
        addView(this.wbModeView, params);
        this.currentWbModeIndex = 0;
        this.wbToggleView = new ImageView(this.mContext);
        this.wbToggleView.setImageResource(R.drawable.wb_btn0_pressed);
        params = new LayoutParams(60, 60);
        params.setMargins(2, 32, 0, 0);
        addView(this.wbToggleView, params);
        this.isoToggleView = new ImageView(this.mContext);
        this.isoToggleView.setImageResource(R.drawable.exposure_btn_normal);
        params = new LayoutParams(60, 60);
        params.setMargins(2, 103, 0, 0);
        addView(this.isoToggleView, params);
        this.isoModeText = new TextView(this.mContext);
        this.isoModeText.setText(R.string.auto);
        this.isoModeText.setTextSize(8.0f);
        this.isoModeText.setGravity(17);
        params = new LayoutParams(60, 30);
        params.setMargins(2, 132, 0, 0);
        addView(this.isoModeText, params);
    }

    private void initListener() {
        this.wbToggleView.setOnClickListener(new OnClickListener() {
            @SuppressLint({"ResourceAsColor"})
            public void onClick(View v) {
                ToggleFrameView.this.wbToggleView.setImageResource(R.drawable.wb_btn0_pressed);
                switch (ToggleFrameView.this.currentWbModeIndex) {
                    case 0:
                        ToggleFrameView.this.wbModeView.setImageResource(R.drawable.auto_pressed);
                        break;
                    case 1:
                        ToggleFrameView.this.wbModeView.setImageResource(R.drawable.lock_pressed);
                        break;
                    case 2:
                        ToggleFrameView.this.wbModeView.setImageResource(R.drawable.daylight_pressed);
                        break;
                    case 3:
                        ToggleFrameView.this.wbModeView.setImageResource(R.drawable.cloudy_pressed);
                        break;
                    case 4:
                        ToggleFrameView.this.wbModeView.setImageResource(R.drawable.fluorescent_pressed);
                        break;
                    case 5:
                        ToggleFrameView.this.wbModeView.setImageResource(R.drawable.incandescent_pressed);
                        break;
                    case 6:
                        ToggleFrameView.this.wbModeView.setImageResource(R.drawable.sunset_pressed);
                        break;
                }
                ToggleFrameView.this.isoToggleView.setImageResource(R.drawable.exposure_btn_normal);
                ToggleFrameView.this.isoModeText.setTextColor(-5592406);
                if (ToggleFrameView.this.toggleOnClickListener != null) {
                    ToggleFrameView.this.toggleOnClickListener.wbItemOnClick(v);
                }
            }
        });
        this.isoToggleView.setOnClickListener(new OnClickListener() {
            @SuppressLint({"ResourceAsColor"})
            public void onClick(View v) {
                ToggleFrameView.this.wbToggleView.setImageResource(R.drawable.wb_btn0_normal);
                switch (ToggleFrameView.this.currentWbModeIndex) {
                    case 0:
                        ToggleFrameView.this.wbModeView.setImageResource(R.drawable.auto_normal);
                        break;
                    case 1:
                        ToggleFrameView.this.wbModeView.setImageResource(R.drawable.lock_normal);
                        break;
                    case 2:
                        ToggleFrameView.this.wbModeView.setImageResource(R.drawable.daylight_normal);
                        break;
                    case 3:
                        ToggleFrameView.this.wbModeView.setImageResource(R.drawable.cloudy_normal);
                        break;
                    case 4:
                        ToggleFrameView.this.wbModeView.setImageResource(R.drawable.fluorescent_normal);
                        break;
                    case 5:
                        ToggleFrameView.this.wbModeView.setImageResource(R.drawable.incandescent_normal);
                        break;
                    case 6:
                        ToggleFrameView.this.wbModeView.setImageResource(R.drawable.sunset_normal);
                        break;
                }
                ToggleFrameView.this.isoToggleView.setImageResource(R.drawable.exposure_btn_pressed);
                ToggleFrameView.this.isoModeText.setTextColor(-47872);
                if (ToggleFrameView.this.toggleOnClickListener != null) {
                    ToggleFrameView.this.toggleOnClickListener.isoItemOnClick(v);
                }
            }
        });
    }

    public void setToggleOnClickListener(ToggleOnClickListener listener) {
        this.toggleOnClickListener = listener;
    }

    public void onIsoModeChanged(boolean isManualMode) {
        if (isManualMode) {
            this.isoModeText.setText(R.string.manual);
        } else {
            this.isoModeText.setText(R.string.auto);
        }
    }

    public void onWbModeChanged(int newItemPositon, boolean isPressed) {
        if (!isPressed) {
            switch (this.currentWbModeIndex) {
                case 0:
                    this.wbModeView.setImageResource(R.drawable.auto_normal);
                    break;
                case 1:
                    this.wbModeView.setImageResource(R.drawable.lock_normal);
                    break;
                case 2:
                    this.wbModeView.setImageResource(R.drawable.daylight_normal);
                    break;
                case 3:
                    this.wbModeView.setImageResource(R.drawable.cloudy_normal);
                    break;
                case 4:
                    this.wbModeView.setImageResource(R.drawable.fluorescent_normal);
                    break;
                case 5:
                    this.wbModeView.setImageResource(R.drawable.incandescent_normal);
                    break;
                case 6:
                    this.wbModeView.setImageResource(R.drawable.sunset_normal);
                    break;
                default:
                    break;
            }
        }
        switch (newItemPositon) {
            case 0:
                this.wbModeView.setImageResource(R.drawable.auto_pressed);
                break;
            case 1:
                this.wbModeView.setImageResource(R.drawable.lock_pressed);
                break;
            case 2:
                this.wbModeView.setImageResource(R.drawable.daylight_pressed);
                break;
            case 3:
                this.wbModeView.setImageResource(R.drawable.cloudy_pressed);
                break;
            case 4:
                this.wbModeView.setImageResource(R.drawable.fluorescent_pressed);
                break;
            case 5:
                this.wbModeView.setImageResource(R.drawable.incandescent_pressed);
                break;
            case 6:
                this.wbModeView.setImageResource(R.drawable.sunset_pressed);
                break;
        }
        this.currentWbModeIndex = newItemPositon;
    }
}

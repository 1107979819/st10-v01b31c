package com.yuneec.widget;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.yuneec.flightmode15.R;

public class ButtonSeekBar extends LinearLayout implements OnSeekBarChangeListener, OnClickListener, OnLongClickListener {
    private static final String TAG = "ButtonSeekBar";
    private Runnable longClickRunnable;
    private Button mBtnLeft;
    private Button mBtnRight;
    private onButtonSeekChangeListener mButtonSeekChangeListener;
    private Button mClickedButton;
    private Handler mHandler;
    private int mMax;
    private int mMin;
    private SeekBar mSeekBar;
    private TextView mText;
    private int mValue;

    public interface onButtonSeekChangeListener {
        void onProgressChanged(ButtonSeekBar buttonSeekBar, int i, boolean z);
    }

    public ButtonSeekBar(Context context) {
        this(context, null, 0);
    }

    public ButtonSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ButtonSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mHandler = new Handler();
        this.longClickRunnable = new Runnable() {
            public void run() {
                ButtonSeekBar.this.clickEvent(ButtonSeekBar.this.mClickedButton);
                if (ButtonSeekBar.this.mClickedButton.isPressed()) {
                    ButtonSeekBar.this.mHandler.postDelayed(ButtonSeekBar.this.longClickRunnable, 5);
                    return;
                }
                ButtonSeekBar.this.mHandler.removeCallbacks(ButtonSeekBar.this.longClickRunnable);
                ButtonSeekBar.this.mClickedButton = null;
            }
        };
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.buttonseekbarlayout, this);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getLayoutParams().width;
        if (width != -2) {
            LayoutParams seekBarParams = (LayoutParams) this.mSeekBar.getLayoutParams();
            int btn_size = this.mBtnLeft.getLayoutParams().width;
            seekBarParams.width = ((width - (btn_size * 2)) - this.mText.getLayoutParams().width) - 10;
            this.mSeekBar.measure(MeasureSpec.makeMeasureSpec(seekBarParams.width, 1073741824), heightMeasureSpec);
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mText = (TextView) findViewById(R.id.text);
        this.mBtnLeft = (Button) findViewById(R.id.left_btn);
        this.mBtnLeft.setOnClickListener(this);
        this.mBtnLeft.setOnLongClickListener(this);
        this.mBtnRight = (Button) findViewById(R.id.right_btn);
        this.mBtnRight.setOnClickListener(this);
        this.mBtnRight.setOnLongClickListener(this);
        this.mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        this.mSeekBar.setOnSeekBarChangeListener(this);
    }

    public void initiation(String text, int max, int min) {
        this.mMax = max;
        this.mMin = min;
        this.mText.setText(text);
        this.mSeekBar.setMax(this.mMax - this.mMin);
    }

    public void setProgress(int value) {
        this.mSeekBar.setProgress(value - this.mMin);
        this.mValue = value;
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            this.mValue = this.mMin + progress;
            if (this.mButtonSeekChangeListener != null) {
                this.mButtonSeekChangeListener.onProgressChanged(this, this.mValue, true);
            } else {
                Log.w(TAG, "mButtonSeekChangeListener is null");
            }
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    private void clickEvent(Button btn) {
        if (btn.equals(this.mBtnLeft)) {
            if (this.mValue > this.mMin) {
                this.mValue--;
            }
        } else if (btn.equals(this.mBtnRight) && this.mValue < this.mMax) {
            this.mValue++;
        }
        this.mSeekBar.setProgress(this.mValue - this.mMin);
        if (this.mButtonSeekChangeListener != null) {
            this.mButtonSeekChangeListener.onProgressChanged(this, this.mValue, true);
        }
    }

    public void onClick(View v) {
        clickEvent((Button) v);
    }

    public boolean onLongClick(View v) {
        this.mClickedButton = (Button) v;
        this.mHandler.post(this.longClickRunnable);
        return true;
    }

    public void setOnButtonSeekChangeListener(onButtonSeekChangeListener buttonSeekChangeListener) {
        this.mButtonSeekChangeListener = buttonSeekChangeListener;
    }
}

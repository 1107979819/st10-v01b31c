package com.yuneec.channelsettings;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import com.yuneec.flightmode15.R;
import com.yuneec.widget.ButtonPicker;
import com.yuneec.widget.ButtonPicker.OnPickerListener;

public class DualPickerGroup extends LinearLayout implements OnClickListener, OnLongClickListener {
    private static final String TAG = "DualButtonPicker";
    private Runnable longClickRunnable;
    private ButtonPicker mBp_bottom;
    private ButtonPicker mBp_top;
    private Button mClickedButton;
    private OnDualPickerListener mDualPickerListener;
    private Handler mHandler;
    private Button mLeftButton;
    private float mMax;
    private float mMin;
    private Button mRightButton;
    private float mStep;
    private float mValue1;
    private float mValue2;

    public interface OnDualPickerListener {
        void onClicked(float f, float f2);
    }

    public DualPickerGroup(Context context) {
        this(context, null);
    }

    public DualPickerGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DualPickerGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mHandler = new Handler();
        this.longClickRunnable = new Runnable() {
            public void run() {
                DualPickerGroup.this.clickEvent(DualPickerGroup.this.mClickedButton);
                if (DualPickerGroup.this.mClickedButton.isPressed()) {
                    DualPickerGroup.this.mHandler.postDelayed(DualPickerGroup.this.longClickRunnable, 5);
                    return;
                }
                DualPickerGroup.this.mHandler.removeCallbacks(DualPickerGroup.this.longClickRunnable);
                DualPickerGroup.this.mClickedButton = null;
            }
        };
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.dualbuttonpickerlayout, this);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLeftButton = (Button) findViewById(R.id.dual_sub);
        this.mLeftButton.setOnClickListener(this);
        this.mLeftButton.setOnLongClickListener(this);
        this.mRightButton = (Button) findViewById(R.id.dual_plus);
        this.mRightButton.setOnClickListener(this);
        this.mRightButton.setOnLongClickListener(this);
        this.mBp_top = (ButtonPicker) findViewById(R.id.top_picker);
        this.mBp_top.setOnPickerListener(new OnPickerListener() {
            public void onClicked(ButtonPicker picker, String value) {
                DualPickerGroup.this.mValue1 = DualPickerGroup.this.mBp_top.getFloatValue();
                DualPickerGroup.this.mDualPickerListener.onClicked(DualPickerGroup.this.mValue1, DualPickerGroup.this.mValue2);
            }
        });
        this.mBp_bottom = (ButtonPicker) findViewById(R.id.bottom_picker);
        this.mBp_bottom.setOnPickerListener(new OnPickerListener() {
            public void onClicked(ButtonPicker picker, String value) {
                DualPickerGroup.this.mValue2 = DualPickerGroup.this.mBp_bottom.getFloatValue();
                DualPickerGroup.this.mDualPickerListener.onClicked(DualPickerGroup.this.mValue1, DualPickerGroup.this.mValue2);
            }
        });
    }

    public void initation(float max, float min, float def1, float def2, float step) {
        this.mMax = max;
        this.mMin = min;
        this.mValue1 = def1;
        this.mValue2 = def2;
        this.mStep = step;
        this.mBp_top.initiation(max, min, def1, step);
        this.mBp_bottom.initiation(max, min, def2, step);
    }

    public void setValue1(float value) {
        this.mValue1 = value;
        this.mBp_top.setFloatValue(value);
    }

    public float getValue1() {
        return this.mValue1;
    }

    public void setValue2(float value) {
        this.mValue2 = value;
        this.mBp_bottom.setFloatValue(value);
    }

    public float getValue2() {
        return this.mValue2;
    }

    private void clickEvent(Button btn) {
        float temp;
        if (btn.equals(this.mLeftButton)) {
            temp = this.mValue1 + this.mStep;
            if (temp >= this.mMin) {
                this.mValue1 = temp;
                this.mBp_top.setFloatValue(temp);
            }
            temp = this.mValue2 + this.mStep;
            if (temp >= this.mMin) {
                this.mValue2 = temp;
                this.mBp_bottom.setFloatValue(temp);
            }
        } else if (btn.equals(this.mRightButton)) {
            temp = this.mValue1 - this.mStep;
            if (temp <= this.mMax) {
                this.mValue1 = temp;
                this.mBp_top.setFloatValue(temp);
            }
            temp = this.mValue2 - this.mStep;
            if (temp <= this.mMax) {
                this.mValue2 = temp;
                this.mBp_bottom.setFloatValue(temp);
            }
        }
        this.mDualPickerListener.onClicked(this.mValue1, this.mValue2);
    }

    public void onClick(View v) {
        clickEvent((Button) v);
    }

    public boolean onLongClick(View v) {
        this.mClickedButton = (Button) v;
        this.mHandler.post(this.longClickRunnable);
        return true;
    }

    public void setOnDualPickerListener(OnDualPickerListener dualPickerListener) {
        this.mDualPickerListener = dualPickerListener;
    }
}

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
import android.widget.TextView;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ButtonPicker extends LinearLayout implements OnClickListener, OnLongClickListener {
    private static final DecimalFormat DF = new DecimalFormat("###.#");
    private static final String TAG = "ButtonPicker";
    private Runnable longClickRunnable;
    private Button mBtn1;
    private Button mBtn2;
    private Button mClickedButton;
    private Handler mHandler;
    private int mIndex;
    private float mIndexF;
    private OnPickerListener mListener;
    private float mMaxF;
    private int mMaxI;
    private float mMinF;
    private int mMinI;
    private List<String> mSelections;
    private float mStepF;
    private int mStepI;
    private TextView mText;
    private int mType;

    public interface OnPickerListener {
        void onClicked(ButtonPicker buttonPicker, String str);
    }

    public ButtonPicker(Context context) {
        this(context, null, 0);
    }

    public ButtonPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ButtonPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIndex = -1;
        this.mStepI = 1;
        this.mIndexF = Utilities.B_SWITCH_MIN;
        this.mStepF = 0.1f;
        this.mHandler = new Handler();
        this.longClickRunnable = new Runnable() {
            public void run() {
                ButtonPicker.this.clickEvent(ButtonPicker.this.mClickedButton);
                if (ButtonPicker.this.mClickedButton.isPressed()) {
                    ButtonPicker.this.mHandler.postDelayed(ButtonPicker.this.longClickRunnable, 5);
                    return;
                }
                ButtonPicker.this.mHandler.removeCallbacks(ButtonPicker.this.longClickRunnable);
                ButtonPicker.this.mClickedButton = null;
            }
        };
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.buttonpickerlayout, this);
        setBackgroundResource(R.drawable.buttonpicker_bg);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width_size;
        int height_size;
        int widthSpec;
        int heightSpec;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getOrientation() == 0) {
            width_size = MeasureSpec.getSize(widthMeasureSpec) / 3;
            height_size = MeasureSpec.getSize(heightMeasureSpec);
            widthSpec = MeasureSpec.makeMeasureSpec(width_size, 1073741824);
            heightSpec = MeasureSpec.makeMeasureSpec(height_size, 1073741824);
        } else {
            width_size = MeasureSpec.getSize(widthMeasureSpec);
            height_size = MeasureSpec.getSize(heightMeasureSpec) / 3;
            widthSpec = MeasureSpec.makeMeasureSpec(width_size, 1073741824);
            heightSpec = MeasureSpec.makeMeasureSpec(height_size, 1073741824);
        }
        LayoutParams params = (LayoutParams) getLayoutParams();
        if (params.width != -2) {
            changeLayoutParamsWidth(this.mBtn1, width_size);
            changeLayoutParamsWidth(this.mBtn2, width_size);
            changeLayoutParamsWidth(this.mText, width_size);
        }
        if (params.height != -2) {
            changeLayoutParamsHeight(this.mBtn1, height_size);
            changeLayoutParamsHeight(this.mBtn2, height_size);
            changeLayoutParamsHeight(this.mText, height_size);
        }
        measureChild(this.mBtn1, widthSpec, heightSpec);
        measureChild(this.mBtn2, widthSpec, heightSpec);
        measureChild(this.mText, widthSpec, heightSpec);
    }

    private void changeLayoutParamsWidth(View view, int width) {
        ((LayoutParams) view.getLayoutParams()).width = width;
    }

    private void changeLayoutParamsHeight(View view, int height) {
        ((LayoutParams) view.getLayoutParams()).height = height;
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mText = (TextView) findViewById(R.id.value);
        this.mBtn1 = (Button) findViewById(R.id.button1);
        this.mBtn1.setOnClickListener(this);
        this.mBtn1.setOnLongClickListener(this);
        this.mBtn2 = (Button) findViewById(R.id.button2);
        this.mBtn2.setOnClickListener(this);
        this.mBtn2.setOnLongClickListener(this);
        initiation(null, null);
        initiationArray(null, null);
    }

    public void initiation(String[] selections, String defalut) {
        this.mType = 0;
        if (selections != null) {
            this.mSelections = Arrays.asList(selections);
        } else {
            this.mSelections = new ArrayList();
            this.mSelections.add("INH");
            defalut = "INH";
        }
        int index = this.mSelections.indexOf(defalut);
        if (index != -1) {
            this.mIndex = index;
            this.mText.setText(defalut);
            return;
        }
        Log.e(TAG, "initiation -- Invalid value");
    }

    public void initiationArray(ArrayList<String> selections, String defalut) {
        this.mType = 0;
        if (selections != null) {
            this.mSelections = selections;
        } else {
            this.mSelections = new ArrayList();
            this.mSelections.add("INH");
            defalut = "INH";
        }
        int index = this.mSelections.indexOf(defalut);
        if (index != -1) {
            this.mIndex = index;
            this.mText.setText(defalut);
            return;
        }
        Log.e(TAG, "initiation -- Invalid value");
    }

    public void initiation(int max, int min, int def, int step) {
        this.mType = 1;
        this.mMaxI = max;
        this.mMinI = min;
        this.mStepI = step;
        if (def < min || def > max) {
            Log.e(TAG, "Initiate invalid integer value");
        } else {
            this.mIndex = def;
        }
        this.mText.setText(String.valueOf(this.mIndex));
    }

    public void initiation(float max, float min, float def, float step) {
        this.mType = 2;
        this.mMaxF = max;
        this.mMinF = min;
        this.mStepF = step;
        if (def < min || def > max) {
            Log.e(TAG, "Initiate invalid float value");
        } else {
            this.mIndexF = def;
        }
        this.mText.setText(String.valueOf(this.mIndex));
    }

    public void setStringValue(String value) {
        int index = this.mSelections.indexOf(value);
        if (index != -1) {
            this.mIndex = index;
            this.mText.setText(value);
            return;
        }
        Log.e(TAG, "setValue -- Invalid String value");
    }

    public String getStringValue() {
        return (String) this.mSelections.get(this.mIndex);
    }

    public void setIntegerValue(int value) {
        if (value > this.mMaxI || value < this.mMinI) {
            Log.e(TAG, "setValue -- Invalid integer value");
            return;
        }
        this.mIndex = value;
        this.mText.setText(String.valueOf(value));
    }

    public void setFloatValue(float value) {
        if (value > this.mMaxF || value < this.mMinF) {
            Log.e(TAG, "setValue -- Invalid float value");
            return;
        }
        this.mIndexF = value;
        this.mText.setText(DF.format((double) this.mIndexF));
    }

    public int getIntegerValue() {
        return this.mIndex;
    }

    public float getFloatValue() {
        return this.mIndexF;
    }

    private void clickEvent(Button btn) {
        String value = null;
        switch (this.mType) {
            case 0:
                if (!btn.equals(this.mBtn1)) {
                    if (btn.equals(this.mBtn2) && this.mIndex < this.mSelections.size() - 1) {
                        this.mIndex++;
                        value = (String) this.mSelections.get(this.mIndex);
                        break;
                    }
                } else if (this.mIndex > 0) {
                    this.mIndex--;
                    value = (String) this.mSelections.get(this.mIndex);
                    break;
                }
                break;
            case 1:
                int tempI;
                if (!btn.equals(this.mBtn1)) {
                    if (btn.equals(this.mBtn2)) {
                        tempI = this.mIndex + this.mStepI;
                        if (tempI <= this.mMaxI) {
                            this.mIndex = tempI;
                            value = String.valueOf(tempI);
                            break;
                        }
                    }
                }
                tempI = this.mIndex - this.mStepI;
                if (tempI >= this.mMinI) {
                    this.mIndex = tempI;
                    value = String.valueOf(tempI);
                    break;
                }
                break;
            case 2:
                float tempF;
                if (!btn.equals(this.mBtn1)) {
                    if (btn.equals(this.mBtn2)) {
                        tempF = this.mIndexF + this.mStepF;
                        if (tempF <= this.mMaxF) {
                            this.mIndexF = tempF;
                            value = DF.format((double) tempF);
                            break;
                        }
                    }
                }
                tempF = this.mIndexF - this.mStepF;
                if (tempF >= this.mMinF) {
                    this.mIndexF = tempF;
                    value = DF.format((double) tempF);
                    break;
                }
                break;
        }
        if (value != null) {
            this.mText.setText(value);
            if (this.mListener != null) {
                this.mListener.onClicked(this, value);
            }
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

    public void setOnPickerListener(OnPickerListener listener) {
        this.mListener = listener;
    }
}

package com.appunite.ffmpeg;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SeekerView extends View {
    private int mBarColor;
    private int mBarMinHeight;
    private int mBarMinWidth;
    private Paint mBarPaint;
    private Rect mBarRect;
    private int mBorderColor;
    private int mBorderPadding;
    private Paint mBorderPaint;
    private Rect mBorderRect;
    private int mBorderWidth;
    private int mCurrentValue;
    private int mMaxValue;
    private OnProgressChangeListener mOnProgressChangeListener;

    public interface OnProgressChangeListener {
        void onProgressChange(boolean z, int i, int i2);
    }

    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        this.mOnProgressChangeListener = onProgressChangeListener;
    }

    public SeekerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mBorderPaint = new Paint();
        this.mBarPaint = new Paint();
        this.mBorderRect = new Rect();
        this.mBarRect = new Rect();
        this.mOnProgressChangeListener = null;
        this.mMaxValue = 100;
        this.mCurrentValue = 10;
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SeekerView, defStyle, 0);
        float scale = getResources().getDisplayMetrics().density;
        this.mBorderWidth = a.getDimensionPixelSize(R.styleable.SeekerView_borderWidth, (int) ((1.0f * scale) + 0.5f));
        this.mBorderColor = a.getColor(R.styleable.SeekerView_barColor, -16711681);
        this.mBorderPadding = a.getColor(R.styleable.SeekerView_borderPadding, (int) ((1.0f * scale) + 0.5f));
        this.mBarMinHeight = a.getDimensionPixelSize(R.styleable.SeekerView_barMinHeight, (int) ((10.0f * scale) + 0.5f));
        this.mBarMinWidth = a.getDimensionPixelSize(R.styleable.SeekerView_barMinWidth, (int) ((50.0f * scale) + 0.5f));
        this.mBarColor = a.getColor(R.styleable.SeekerView_barColor, -16776961);
        this.mBorderPaint.setDither(true);
        this.mBorderPaint.setColor(this.mBorderColor);
        this.mBorderPaint.setStyle(Style.STROKE);
        this.mBorderPaint.setStrokeJoin(Join.ROUND);
        this.mBorderPaint.setStrokeCap(Cap.ROUND);
        this.mBorderPaint.setStrokeWidth((float) this.mBorderWidth);
        this.mBarPaint.setDither(true);
        this.mBarPaint.setColor(this.mBarColor);
        this.mBarPaint.setStyle(Style.FILL);
        this.mBarPaint.setStrokeJoin(Join.ROUND);
        this.mBarPaint.setStrokeCap(Cap.ROUND);
        this.mMaxValue = a.getInt(R.styleable.SeekerView_maxValue, this.mMaxValue);
        this.mCurrentValue = a.getInt(R.styleable.SeekerView_currentValue, this.mCurrentValue);
    }

    public SeekerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekerView(Context context) {
        this(context, null);
    }

    public void setMaxValue(int maxValue) {
        this.mMaxValue = maxValue;
        invalidate();
    }

    public int maxValue() {
        return this.mMaxValue;
    }

    public void setCurrentValue(int currentValue) {
        this.mCurrentValue = currentValue;
        invalidate();
    }

    public int currentValue() {
        return this.mCurrentValue;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(this.mBorderRect, this.mBorderPaint);
        canvas.drawRect(this.mBarRect, this.mBarPaint);
    }

    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        boolean superResult = super.onTouchEvent(event);
        boolean grab = false;
        boolean finished = false;
        if (action == 0) {
            grab = true;
        } else if (action == 2) {
            grab = true;
        } else if (action == 1) {
            grab = true;
            finished = true;
        }
        if (!grab) {
            return superResult;
        }
        int padding = this.mBorderWidth + this.mBorderPadding;
        int barWidth = getWidth() - (padding * 2);
        float x = event.getX() - ((float) padding);
        if (x < 0.0f) {
            x = 0.0f;
        }
        if (x > ((float) barWidth)) {
            x = (float) barWidth;
        }
        this.mCurrentValue = (int) (((float) this.mMaxValue) * (x / ((float) barWidth)));
        if (this.mOnProgressChangeListener != null) {
            this.mOnProgressChangeListener.onProgressChange(finished, this.mCurrentValue, this.mMaxValue);
        }
        calculateBarRect();
        invalidate();
        return true;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(ViewCompat.resolveSizeAndState(((this.mBorderWidth + this.mBorderPadding) * 2) + this.mBarMinWidth, widthMeasureSpec, 0), ViewCompat.resolveSizeAndState(((this.mBorderWidth + this.mBorderPadding) * 2) + this.mBarMinHeight, heightMeasureSpec, 0));
    }

    private void calculateBarRect() {
        int width = getWidth();
        int barPadding = this.mBorderWidth + this.mBorderPadding;
        float pos = ((float) this.mCurrentValue) / ((float) this.mMaxValue);
        int barWidth = (int) (((float) (width - barPadding)) * pos);
        this.mBarRect.set(barPadding, barPadding, barWidth, getHeight() - barPadding);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            this.mBorderRect.set(0, 0, right - left, bottom - top);
            calculateBarRect();
        }
    }
}

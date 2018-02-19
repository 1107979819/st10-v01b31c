package com.yuneec.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.widget.SeekBar;

public class VerticalSeekBar extends SeekBar {
    private boolean isInScrollingContainer = false;
    private boolean mIsDragging;
    private int mScaledTouchSlop;
    private float mTouchDownY;
    float mTouchProgressOffset;

    public boolean isInScrollingContainer() {
        return this.isInScrollingContainer;
    }

    public void setInScrollingContainer(boolean isInScrollingContainer) {
        this.isInScrollingContainer = isInScrollingContainer;
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalSeekBar(Context context) {
        super(context);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    protected synchronized void onDraw(Canvas canvas) {
        canvas.rotate(-90.0f);
        canvas.translate((float) (-getHeight()), 0.0f);
        super.onDraw(canvas);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case 0:
                if (!isInScrollingContainer()) {
                    setPressed(true);
                    invalidate();
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    attemptClaimDrag();
                    onSizeChanged(getWidth(), getHeight(), 0, 0);
                    break;
                }
                this.mTouchDownY = event.getY();
                break;
            case 1:
                if (this.mIsDragging) {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);
                } else {
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                }
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                invalidate();
                break;
            case 2:
                if (this.mIsDragging) {
                    trackTouchEvent(event);
                } else if (Math.abs(event.getY() - this.mTouchDownY) > ((float) this.mScaledTouchSlop)) {
                    setPressed(true);
                    invalidate();
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    attemptClaimDrag();
                }
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;
        }
        return true;
    }

    private void trackTouchEvent(MotionEvent event) {
        float scale;
        int height = getHeight();
        int top = getPaddingTop();
        int bottom = getPaddingBottom();
        int available = (height - top) - bottom;
        int y = (int) event.getY();
        float progress = 0.0f;
        if (y > height - bottom) {
            scale = 0.0f;
        } else if (y < top) {
            scale = 1.0f;
        } else {
            scale = ((float) ((available - y) + top)) / ((float) available);
            progress = this.mTouchProgressOffset;
        }
        setProgress((int) (progress + (((float) getMax()) * scale)));
    }

    void onStartTrackingTouch() {
        this.mIsDragging = true;
    }

    void onStopTrackingTouch() {
        this.mIsDragging = false;
    }

    private void attemptClaimDrag() {
        ViewParent p = getParent();
        if (p != null) {
            p.requestDisallowInterceptTouchEvent(true);
        }
    }

    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }
}

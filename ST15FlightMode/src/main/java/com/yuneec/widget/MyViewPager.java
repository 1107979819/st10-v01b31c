package com.yuneec.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager {
    private static final float FLING_THRESHOLD = 25.0f;
    private float mLastX;

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyViewPager(Context context) {
        super(context);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = super.onInterceptTouchEvent(ev);
        switch (ev.getAction()) {
            case 0:
                this.mLastX = ev.getX();
                break;
            case 1:
            case 3:
                this.mLastX = 0.0f;
                break;
            case 2:
                if (Math.abs(this.mLastX - ev.getX()) > FLING_THRESHOLD) {
                    result = true;
                }
                this.mLastX = ev.getX();
                break;
        }
        return result;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }
}

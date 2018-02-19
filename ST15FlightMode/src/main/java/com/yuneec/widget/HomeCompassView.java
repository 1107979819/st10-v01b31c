package com.yuneec.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.yuneec.flightmode15.R;

public class HomeCompassView extends View {
    private Drawable mBg;
    private int mCompassHeight;
    private int mCompassWidth;
    private int mDegree;
    private Drawable mHome;
    private boolean mHomeEnabled;

    public HomeCompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public HomeCompassView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeCompassView(Context context) {
        this(context, null);
    }

    private void init(Context context) {
        this.mBg = getResources().getDrawable(R.drawable.home_bg);
        this.mHome = getResources().getDrawable(R.drawable.home);
        this.mCompassWidth = this.mBg.getIntrinsicWidth();
        this.mCompassHeight = this.mBg.getIntrinsicHeight();
        this.mBg.setBounds(0, 0, this.mCompassWidth, this.mCompassHeight);
        int width = this.mHome.getIntrinsicWidth();
        int left = (this.mCompassWidth - width) >> 1;
        this.mHome.setBounds(left, 10, left + width, 10 + this.mHome.getIntrinsicHeight());
        this.mDegree = 0;
        this.mHomeEnabled = false;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(this.mCompassWidth, this.mCompassHeight);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mBg != null) {
            this.mBg.draw(canvas);
        }
        if (this.mHomeEnabled) {
            canvas.save();
            canvas.rotate((float) this.mDegree, (float) (this.mCompassWidth >> 1), (float) (this.mCompassHeight >> 1));
            this.mHome.draw(canvas);
            canvas.restore();
        }
    }

    public void setDirection(int degree) {
        if (this.mHomeEnabled && degree != this.mDegree) {
            this.mDegree = degree;
            invalidate();
        }
    }

    public void setHomeEnabled(boolean enabled) {
        if (enabled != this.mHomeEnabled) {
            this.mHomeEnabled = enabled;
            invalidate();
        }
    }
}

package com.yuneec.model_select;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.yuneec.flightmode15.R;

public class PageIndicator extends View {
    private static final int INDICATOR_PADDING = 22;
    private static final String TAG = "PageIndicator";
    private int mCurrentPage;
    private Drawable mIndicator;
    private int mIndicatorHeight;
    private Drawable mIndicatorPressed;
    private int mIndicatorWidth;
    private int mPages;
    private boolean mShowIfNeeded = true;

    public PageIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PageIndicator(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.mIndicator = context.getResources().getDrawable(R.drawable.models_select_indicator_normal);
        this.mIndicatorPressed = context.getResources().getDrawable(R.drawable.models_select_indicator_pressed);
        this.mIndicatorWidth = this.mIndicator.getIntrinsicWidth();
        this.mIndicatorHeight = this.mIndicator.getIntrinsicHeight();
        this.mIndicator.setBounds(0, 0, this.mIndicatorWidth, this.mIndicatorHeight);
        this.mIndicatorPressed.setBounds(0, 0, this.mIndicatorWidth, this.mIndicatorHeight);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mPages < 0) {
            throw new IllegalArgumentException("PageIndicator illegal page count :" + this.mPages);
        }
        int height;
        int width;
        if (this.mPages == 0) {
            Log.w(TAG, "onMeasure mPages == 0,width&height set to 0 mPages:" + this.mPages);
            height = 0;
            width = 0;
        } else {
            width = (this.mIndicatorWidth * this.mPages) + ((this.mPages - 1) * 22);
            height = this.mIndicatorHeight;
        }
        setMeasuredDimension(width, height);
    }

    protected void onDraw(Canvas canvas) {
        if (this.mPages < 0) {
            throw new IllegalArgumentException("PageIndicator illegal page count :" + this.mPages);
        } else if (this.mPages == 0) {
            Log.w(TAG, "No pages,Nothing to Draw");
        } else if (this.mPages != 1 || !this.mShowIfNeeded) {
            int N = this.mPages;
            for (int i = 0; i < N; i++) {
                canvas.save();
                canvas.translate((float) ((this.mIndicatorWidth + 22) * i), 0.0f);
                if (i == this.mCurrentPage) {
                    this.mIndicatorPressed.draw(canvas);
                } else {
                    this.mIndicator.draw(canvas);
                }
                canvas.restore();
            }
        }
    }

    public void setPageCount(int count) {
        this.mPages = count;
        invalidate();
    }

    public void setCurrentPage(int which) {
        this.mCurrentPage = which;
        invalidate();
    }

    public int getCurrentPage() {
        return this.mCurrentPage;
    }

    public void setShowPolicy(boolean showIfNeeded) {
        this.mShowIfNeeded = showIfNeeded;
        invalidate();
    }
}

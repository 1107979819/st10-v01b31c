package com.yuneec.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.yuneec.flightmode15.R;

public class KnobsView extends View {
    private int degree;
    private int mCenterX;
    private int mCenterY;
    private Drawable mKnobBackground;
    private Drawable mKnobDrawable;

    public KnobsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.degree = 0;
        init(context);
    }

    public KnobsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KnobsView(Context context) {
        this(context, null);
    }

    private void init(Context context) {
        this.mKnobDrawable = context.getResources().getDrawable(R.drawable.hardware_monitor_knobs);
        this.mKnobDrawable.setBounds(0, 0, this.mKnobDrawable.getIntrinsicWidth(), this.mKnobDrawable.getIntrinsicHeight());
        this.mKnobBackground = context.getResources().getDrawable(R.drawable.hardware_monitor_knobs_back_ground);
        this.mKnobBackground.setBounds(0, 0, this.mKnobBackground.getIntrinsicWidth(), this.mKnobBackground.getIntrinsicHeight());
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mKnobBackground.draw(canvas);
        canvas.save();
        canvas.rotate((float) this.degree, (float) this.mCenterX, (float) this.mCenterY);
        this.mKnobDrawable.draw(canvas);
        canvas.restore();
    }

    public void setValue(int value) {
        if (this.degree != value) {
            this.degree = value;
            invalidate();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(this.mKnobDrawable.getIntrinsicWidth(), this.mKnobDrawable.getIntrinsicHeight());
        this.mCenterX = (this.mKnobDrawable.getIntrinsicWidth() >> 1) + 1;
        this.mCenterY = (this.mKnobDrawable.getIntrinsicHeight() >> 1) + 1;
    }
}

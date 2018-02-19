package com.yuneec.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import com.yuneec.flightmode15.R;

public class LeftTrimView extends View {
    private Bitmap bitmap_bg = null;
    private Bitmap bitmap_h = null;
    private Bitmap bitmap_v = null;
    private float hX;
    private float hY;
    private int mH_value;
    private Paint mPaint = new Paint();
    private Paint mPaintText = new Paint();
    private int mV_value;
    private int startX;
    private int startY;
    private float vX;
    private float vY;

    public LeftTrimView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public LeftTrimView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public LeftTrimView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.bitmap_bg = BitmapFactory.decodeResource(getResources(), R.drawable.left_trim_background);
        this.bitmap_h = BitmapFactory.decodeResource(getResources(), R.drawable.h_cursor);
        this.bitmap_v = BitmapFactory.decodeResource(getResources(), R.drawable.v_cursor_l);
        this.startX = this.bitmap_h.getWidth();
        this.startY = this.bitmap_v.getHeight();
    }

    public void drawBackground(Canvas canvas) {
        this.mPaint.setAntiAlias(true);
        canvas.drawBitmap(this.bitmap_bg, (float) this.bitmap_v.getWidth(), (float) this.bitmap_h.getHeight(), this.mPaint);
    }

    public void drawCursor(Canvas canvas) {
        this.mPaint.setAntiAlias(true);
        this.vX = (float) (this.startX + 160);
        this.vY = (((float) (this.startY + 60)) - (((float) this.mV_value) * 2.7f)) - ((float) ((this.bitmap_v.getHeight() + 1) >> 1));
        canvas.drawBitmap(this.bitmap_v, this.vX, this.vY, this.mPaint);
        this.mPaint.setAntiAlias(true);
        this.hX = ((((float) this.startX) + (((float) this.mH_value) * 2.7f)) + 63.0f) - ((float) ((this.bitmap_h.getWidth() - 1) >> 1));
        this.hY = (float) (this.startY + 126);
        canvas.drawBitmap(this.bitmap_h, this.hX, this.hY, this.mPaint);
        this.mPaintText.setAntiAlias(true);
        this.mPaintText.setTextAlign(Align.CENTER);
        this.mPaintText.setColor(-1);
        this.mPaintText.setTextSize(14.0f);
        this.mPaintText.setTypeface(Typeface.DEFAULT);
        this.mPaintText.setTextAlign(Align.RIGHT);
        canvas.drawText(String.valueOf(this.mV_value), this.vX + 28.0f, this.vY + 20.0f, this.mPaintText);
        this.mPaintText.setTextAlign(Align.CENTER);
        canvas.drawText(String.valueOf(this.mH_value), this.hX + 16.0f, this.hY + 24.0f, this.mPaintText);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawCursor(canvas);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((this.bitmap_bg.getWidth() + this.bitmap_h.getWidth()) + this.bitmap_v.getWidth(), (this.bitmap_bg.getHeight() + this.bitmap_h.getHeight()) + this.bitmap_v.getHeight());
    }

    public void setValue(int v_value, int h_value) {
        if (this.mV_value != v_value || this.mH_value != h_value) {
            this.mV_value = v_value;
            this.mH_value = h_value;
            invalidate();
        }
    }
}

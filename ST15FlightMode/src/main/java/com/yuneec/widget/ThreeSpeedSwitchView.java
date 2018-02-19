package com.yuneec.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.yuneec.flightmode15.R;

public class ThreeSpeedSwitchView extends View {
    private static final int SELECTED_HEIGHT = 44;
    private static final int SELECTED_WIDTH = 46;
    private static final String TAG = "ThreeSpeedSwitchView-->";
    private static final int VIEW_HEIGHT = 126;
    private static final int VIEW_WIDTH = 46;
    Bitmap bitmap = null;
    Bitmap bitmap_bg = null;
    private Paint mPaint = new Paint();
    private Paint mPaintText = new Paint();
    private int mSpeed;

    public ThreeSpeedSwitchView(Context context) {
        super(context);
        init(context);
    }

    public ThreeSpeedSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ThreeSpeedSwitchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.bitmap_bg = BitmapFactory.decodeResource(getResources(), R.drawable.hardware_monitor_switch_background);
        this.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hardware_monitor_switch_selected);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawValue(canvas);
    }

    public void drawBackground(Canvas canvas) {
        this.mPaint.setAntiAlias(true);
        canvas.drawBitmap(this.bitmap_bg, null, new Rect(0, 0, 46, VIEW_HEIGHT), this.mPaint);
    }

    public void drawValue(Canvas canvas) {
        this.mPaint.setAntiAlias(true);
        this.mPaintText.setAntiAlias(true);
        this.mPaintText.setTextAlign(Align.CENTER);
        this.mPaintText.setColor(-1);
        this.mPaintText.setTextSize(18.0f);
        this.mPaintText.setTypeface(Typeface.DEFAULT);
        if (this.mSpeed == 0) {
            canvas.drawBitmap(this.bitmap, null, new Rect(0, 0, 46, 42), this.mPaint);
        } else if (this.mSpeed == 1) {
            canvas.drawBitmap(this.bitmap, null, new Rect(0, 44, 46, 84), this.mPaint);
        } else if (this.mSpeed == 2) {
            canvas.drawBitmap(this.bitmap, null, new Rect(0, 84, 46, VIEW_HEIGHT), this.mPaint);
        } else {
            Log.e(TAG, "mSpeed is not {0, 1, 2}. value = " + this.mSpeed);
            return;
        }
        canvas.drawText("0", 23.0f, 27.0f, this.mPaintText);
        canvas.drawText("1", 23.0f, 69.0f, this.mPaintText);
        canvas.drawText("2", 23.0f, 111.0f, this.mPaintText);
    }

    public void setValue(int value) {
        if (value != 0 && value != 1 && value != 2) {
            Log.e(TAG, "mSpeed is not {0, 1, 2}. value is " + this.mSpeed);
        } else if (this.mSpeed != value) {
            this.mSpeed = value;
            postInvalidate();
        }
    }
}

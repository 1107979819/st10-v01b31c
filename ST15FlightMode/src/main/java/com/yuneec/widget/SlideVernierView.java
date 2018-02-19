package com.yuneec.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import com.yuneec.flightmode15.R;

public class SlideVernierView extends View {
    private static final int VIEW_HEIGHT = 126;
    private static final int VIEW_WIDTH = 46;
    Bitmap bitmap = null;
    Bitmap bitmap_bg = null;
    private float cX;
    private float cY;
    private Paint mPaint = new Paint();
    private int mValue;

    public SlideVernierView(Context context) {
        super(context);
        init(context);
    }

    public SlideVernierView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlideVernierView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.bitmap_bg = BitmapFactory.decodeResource(getResources(), R.drawable.hwm_slide_vernier);
        this.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hwm_slide_vernier_cursor);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawCursor(canvas);
    }

    public void drawBackground(Canvas canvas) {
        this.mPaint.setAntiAlias(true);
        canvas.drawBitmap(this.bitmap_bg, null, new Rect(0, 0, 46, VIEW_HEIGHT), this.mPaint);
    }

    public void drawCursor(Canvas canvas) {
        this.mPaint.setAntiAlias(true);
        this.cX = 0.0f;
        this.cY = ((float) this.mValue) * 0.55f;
        canvas.drawBitmap(this.bitmap, this.cX, this.cY, this.mPaint);
    }

    public void setValue(int value) {
        if (this.mValue == value) {
            return;
        }
        if (this.mValue - value >= 5 || this.mValue - value <= -5) {
            this.mValue = value;
            invalidate();
        }
    }
}

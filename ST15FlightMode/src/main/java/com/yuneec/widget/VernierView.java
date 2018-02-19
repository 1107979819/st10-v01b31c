package com.yuneec.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.yuneec.flightmode15.R;

public class VernierView extends View {
    private static final int CURSOR_X0 = 23;
    private static final int CURSOR_Y0 = 73;
    private static final int VERNIER_X = 0;
    private static final int VERNIER_Y = 0;
    private Cursor mCursor = new Cursor();
    private Paint mPaint = new Paint();

    class Cursor {
        int value;
        float x;
        float y;

        Cursor() {
        }
    }

    public VernierView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VernierView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VernierView(Context context) {
        super(context);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawCursor(canvas);
    }

    private void drawBackground(Canvas canvas) {
        Bitmap view_bg = BitmapFactory.decodeResource(getResources(), R.drawable.hwm_vernier);
        this.mPaint.setAntiAlias(true);
        canvas.drawBitmap(view_bg, 0.0f, 0.0f, this.mPaint);
    }

    private void drawCursor(Canvas canvas) {
        ClipDrawable cursorBarTop = (ClipDrawable) getResources().getDrawable(R.drawable.hwm_cursorbar_top);
        ClipDrawable cursorBarBottom = (ClipDrawable) getResources().getDrawable(R.drawable.hwm_cursorbar_bottom);
        Drawable cursorIcon = getResources().getDrawable(R.drawable.hwm_cursor);
        this.mPaint.setAntiAlias(true);
        this.mCursor.x = 23.0f;
        this.mCursor.y = (float) (73.0d - (((double) this.mCursor.value) * 0.45d));
        float cursorIconLeft = this.mCursor.x - 1.0f;
        float cursorIconTop = this.mCursor.y - ((float) (cursorIcon.getIntrinsicHeight() / 2));
        int level = (int) ((((double) this.mCursor.value) * 0.45d) * 200.0d);
        if (level > 0) {
            cursorBarTop.setLevel(level);
            cursorBarBottom.setLevel(0);
        } else if (level < 0) {
            cursorBarTop.setLevel(0);
            cursorBarBottom.setLevel(Math.abs(level));
        } else {
            cursorBarTop.setLevel(0);
            cursorBarBottom.setLevel(0);
        }
        cursorBarTop.setBounds(23, 73 - cursorBarTop.getIntrinsicHeight(), cursorBarTop.getIntrinsicWidth() + 23, 73);
        cursorBarTop.draw(canvas);
        cursorBarBottom.setBounds(23, 74, cursorBarBottom.getIntrinsicWidth() + 23, (cursorBarBottom.getIntrinsicHeight() + 73) + 1);
        cursorBarBottom.draw(canvas);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.hwm_cursor), cursorIconLeft, cursorIconTop, this.mPaint);
    }

    public void setValue(int value) {
        if (this.mCursor.value != value) {
            this.mCursor.value = value;
            invalidate();
        }
    }

    public int getValue() {
        return this.mCursor.value;
    }
}

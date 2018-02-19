package com.yuneec.curve;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

public class SwitchCurveView extends CurveView {
    private static final String TAG = "SwitchCurveView";

    public SwitchCurveView(Context context) {
        super(context);
    }

    public SwitchCurveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SwitchCurveView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
    }

    protected void onRefresh() {
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        super.surfaceChanged(holder, format, width, height);
        drawCurve();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
    }

    private void drawCurve() {
        Canvas canvas = getHolder().lockCanvas();
        this.mCurveCanvas.drawColor(0, Mode.CLEAR);
        canvas.drawBitmap(this.mCurveBitmap, 0.0f, 0.0f, null);
        getHolder().unlockCanvasAndPost(canvas);
        onCurveChanges();
    }
}

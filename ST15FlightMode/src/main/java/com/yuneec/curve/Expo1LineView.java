package com.yuneec.curve;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import com.yuneec.channelsettings.ChannelSettings;
import com.yuneec.flightmode15.Utilities;

public class Expo1LineView extends CurveView {
    private static final int DOUBLE_TOUCH_SPACE = 5;
    private static final int POINT_OFFSET = 30;
    private static final String TAG = "Expo1LineView";
    private static final int TOUCH_LEFT_SIDE = 1;
    private static final int TOUCH_LEFT_SPACE = 3;
    private static final int TOUCH_RIGHT_SIDE = 2;
    private static final int TOUCH_RIGHT_SPACE = 4;
    private boolean isSurfaceReady = false;
    private Point[] mAllPoint;
    private float mB = 0.17f;
    private boolean mChangeCurveExpo = false;
    private boolean mChangeCurveLeftRate = false;
    private boolean mChangeCurveOffset = false;
    private boolean mChangeCurveRightRate = false;
    private float mCurveValue_l;
    private float mCurveValue_r;
    private float mK = Utilities.K_MID;
    private float mN = 1.0f;
    private Point mTouchEndP1 = new Point();
    private Point mTouchEndP2 = new Point();
    private Point mTouchStartP1 = new Point();
    private Point mTouchStartP2 = new Point();

    public Expo1LineView(Context context) {
        super(context);
    }

    public Expo1LineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public Expo1LineView(Context context, AttributeSet attrs) {
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
        this.mAllPoint = new Point[(mAxis_width + 1)];
        this.mK = this.mCurveValue_r - this.mCurveValue_l;
        this.mB = this.mCurveValue_l;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction() & MotionEventCompat.ACTION_MASK) {
            case 0:
                int tempX1 = (int) event.getX();
                int tempY1 = (int) event.getY();
                setEventType(getTouchSpace(tempX1, tempY1));
                this.mTouchStartP1.x = tempX1;
                this.mTouchStartP1.y = tempY1;
                break;
            case 1:
                ReleaseTouchEvent();
                break;
            case 2:
                float f;
                if (event.getPointerCount() > 1) {
                    this.mTouchEndP1.x = (int) event.getX(0);
                    this.mTouchEndP1.y = (int) event.getY(0);
                    this.mTouchEndP2.x = (int) event.getX(1);
                    this.mTouchEndP2.y = (int) event.getY(1);
                    int distance1 = this.mTouchEndP1.y - this.mTouchStartP1.y;
                    int distance2 = this.mTouchEndP2.y - this.mTouchStartP2.y;
                    if (this.mChangeCurveOffset && distance1 * distance2 > 0 && (Math.abs(distance1) > 50 || Math.abs(distance2) > 50)) {
                        f = this.mB - (((float) ((distance1 + distance2) / 2)) * 5.0E-4f);
                    }
                } else {
                    int tmpY = (int) event.getY(0);
                    this.mTouchEndP1.x = (int) event.getX(0);
                    this.mTouchEndP1.y = tmpY;
                    int distance = this.mTouchEndP1.y - this.mTouchStartP1.y;
                    if (Math.abs(distance) > 10) {
                        if (this.mChangeCurveLeftRate) {
                            this.mCurveValue_l -= ((float) distance) / ((float) mAxis_height);
                            if (this.mCurveValue_l < 0.0f) {
                                this.mCurveValue_l = 0.0f;
                            } else if (this.mCurveValue_l > 1.0f) {
                                this.mCurveValue_l = 1.0f;
                            }
                        } else if (this.mChangeCurveRightRate) {
                            this.mCurveValue_r -= ((float) distance) / ((float) mAxis_height);
                            if (this.mCurveValue_r < 0.0f) {
                                this.mCurveValue_r = 0.0f;
                            } else if (this.mCurveValue_r > 1.0f) {
                                this.mCurveValue_r = 1.0f;
                            }
                        } else if (this.mChangeCurveExpo) {
                            f = this.mN + (((float) distance) * (this.mN > 1.0f ? 0.005f : 0.001f));
                        }
                    }
                }
                redraw();
                this.mTouchStartP1.x = this.mTouchEndP1.x;
                this.mTouchStartP1.y = this.mTouchEndP1.y;
                this.mTouchStartP2.x = this.mTouchEndP2.x;
                this.mTouchStartP2.y = this.mTouchEndP2.y;
                break;
            case 5:
                int pointerIndex = (event.getAction() & MotionEventCompat.ACTION_MASK) >> 8;
                this.mTouchStartP2.x = (int) event.getX(pointerIndex);
                this.mTouchStartP2.y = (int) event.getY(pointerIndex);
                setEventType(5);
                break;
        }
        return true;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        super.surfaceChanged(holder, format, width, height);
        drawCurve();
        this.isSurfaceReady = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        this.isSurfaceReady = false;
    }

    public void setParams(float userValue_l, float userValue_r, float expo) {
        this.mCurveValue_l = convertUserValueToCurveValue(userValue_l);
        this.mCurveValue_r = convertUserValueToCurveValue(userValue_r);
        redraw();
    }

    private void drawCurve() {
        int startX = this.mAllPoint[0].x;
        int startY = this.mAllPoint[0].y;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.STROKE);
        paint.setColor(-65536);
        paint.setStrokeWidth(1.0f);
        Path path = new Path();
        path.moveTo((float) startX, (float) startY);
        Canvas canvas = getHolder().lockCanvas();
        for (int i = 1; i < this.mAllPoint.length; i++) {
            path.lineTo((float) this.mAllPoint[i].x, (float) this.mAllPoint[i].y);
        }
        canvas.drawColor(0, Mode.CLEAR);
        this.mCurveCanvas.drawColor(0, Mode.CLEAR);
        this.mCurveCanvas.drawPath(path, paint);
        canvas.drawBitmap(this.mCurveBitmap, 0.0f, 0.0f, null);
        getHolder().unlockCanvasAndPost(canvas);
    }

    public int getValueY(int k, int n, int b, int x) {
        return (int) ((((double) k) * Math.pow((double) x, (double) n)) - ((double) b));
    }

    private int getTouchSpace(int x, int y) {
        if (Math.abs(x - mAxis.getAxisX()) < 30) {
            return 1;
        }
        if (Math.abs(x - (mAxis.getAxisX() + mAxis_width)) < 30) {
            return 2;
        }
        if (x <= mAxis.getAxisX() || x >= mAxis.getAxisX() + (mAxis_width / 2)) {
            return 4;
        }
        return 3;
    }

    private void setEventType(int touchSpace) {
        switch (touchSpace) {
            case 1:
                this.mChangeCurveLeftRate = true;
                return;
            case 2:
                this.mChangeCurveRightRate = true;
                return;
            case 3:
            case 4:
                this.mChangeCurveExpo = true;
                return;
            case 5:
                this.mChangeCurveOffset = false;
                return;
            default:
                Log.w(TAG, "Invalid touch space");
                return;
        }
    }

    private void ReleaseTouchEvent() {
        this.mTouchStartP1.x = 0;
        this.mTouchStartP1.y = 0;
        this.mTouchStartP2.x = 0;
        this.mTouchStartP2.y = 0;
        this.mTouchEndP1.x = 0;
        this.mTouchEndP1.y = 0;
        this.mTouchEndP2.x = 0;
        this.mTouchEndP2.y = 0;
        this.mChangeCurveLeftRate = false;
        this.mChangeCurveRightRate = false;
        this.mChangeCurveExpo = false;
        this.mChangeCurveOffset = false;
    }

    private void redraw() {
        if (this.mOnCurveTouchListener != null) {
            this.mOnCurveTouchListener.onCurveValueChanged(1, this.mCurveValue_l);
            this.mOnCurveTouchListener.onCurveValueChanged(2, this.mCurveValue_r);
            this.mOnCurveTouchListener.onCurveValueChanged(3, this.mN);
        }
        this.mK = this.mCurveValue_r - this.mCurveValue_l;
        this.mB = this.mCurveValue_l;
        if (this.isSurfaceReady) {
            drawCurve();
            if (this.mAllPointsUpdateListener != null) {
                this.mAllPointsUpdateListener.onUpdated(getAllPointsPosition());
            }
        }
    }

    public int[] getAllPointsPosition() {
        int[] value = new int[this.mAllPoint.length];
        for (int i = 0; i < this.mAllPoint.length; i++) {
            value[i] = (mAxis.getAxisY() + mAxis_height) - this.mAllPoint[i].y;
        }
        return value;
    }

    public int[] getAllPointsChValue() {
        int[] value = new int[this.mAllPoint.length];
        for (int i = 0; i < this.mAllPoint.length; i++) {
            value[i] = (((mAxis.getAxisY() + mAxis_height) - this.mAllPoint[i].y) * ChannelSettings.STICK_RATE_125_OR_150) / mAxis_height;
        }
        return value;
    }

    public static float convertChannelValueToCurveValue(float ch_value) {
        return (1.0f * ch_value) / 4095.0f;
    }

    public float convertUserValueToCurveValue(float user_value) {
        return ((user_value - ((float) this.mOutputMin)) * 1.0f) / ((float) (this.mOutputMax - this.mOutputMin));
    }

    public float convertCurveValueToUserValue(float curve_value) {
        return ((float) this.mOutputMin) + (((float) (this.mOutputMax - this.mOutputMin)) * curve_value);
    }
}

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
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;

public class Expo2LineView extends CurveView {
    private static final int DOUBLE_TOUCH_SPACE = 5;
    private static final int POINT_OFFSET = 30;
    private static final String TAG = "Expo2LineView";
    private static final int TOUCH_LEFT_SIDE = 1;
    private static final int TOUCH_LEFT_SPACE = 3;
    private static final int TOUCH_RIGHT_SIDE = 2;
    private static final int TOUCH_RIGHT_SPACE = 4;
    private boolean isSurfaceReady;
    private Point[] mAllPoint;
    private float mB;
    private boolean mChangeCurveLeftExpo;
    private boolean mChangeCurveLeftRate;
    private boolean mChangeCurveRightExpo;
    private boolean mChangeCurveRightRate;
    private boolean mChangeWholeCurveExpo;
    private boolean mChangeWholeCurveOffset;
    private boolean mChangeWholeCurveRate;
    private Paint mCurrentPaint;
    private float mCurveValue_l;
    private float mCurveValue_r;
    private float mK1;
    private float mK2;
    private float mN1;
    private float mN2;
    private Paint mPaint;
    private Paint mPaint1;
    private Paint mPaint2;
    private boolean mSeparate;
    private Point mTouchEndP1;
    private Point mTouchEndP2;
    private Point mTouchStartP1;
    private Point mTouchStartP2;

    public Expo2LineView(Context context) {
        this(context, null);
    }

    public Expo2LineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mTouchStartP1 = new Point();
        this.mTouchStartP2 = new Point();
        this.mTouchEndP1 = new Point();
        this.mTouchEndP2 = new Point();
        this.mChangeCurveLeftRate = false;
        this.mChangeCurveRightRate = false;
        this.mChangeCurveLeftExpo = false;
        this.mChangeCurveRightExpo = false;
        this.mChangeWholeCurveRate = false;
        this.mChangeWholeCurveExpo = false;
        this.mChangeWholeCurveOffset = false;
        this.mSeparate = false;
        this.isSurfaceReady = false;
    }

    public Expo2LineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setColor(-65536);
        this.mPaint.setStrokeWidth(1.0f);
        this.mPaint1 = new Paint();
        this.mPaint1.setAntiAlias(true);
        this.mPaint1.setStyle(Style.STROKE);
        this.mPaint1.setColor(-65536);
        this.mPaint1.setStrokeWidth(1.0f);
        this.mPaint2 = new Paint();
        this.mPaint2.setAntiAlias(true);
        this.mPaint2.setStyle(Style.STROKE);
        this.mPaint2.setColor(-256);
        this.mPaint2.setStrokeWidth(1.0f);
        this.mCurrentPaint = this.mPaint;
        this.mAllPoint = new Point[(mAxis_width + 1)];
        this.mAllPoint = Utilities.countAllPointOfExpo2(mAxis.getAxisX(), mAxis.getAxisY(), mAxis_width, mAxis_height, this.mK1, this.mK2, this.mN1, this.mN2, this.mB);
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
                if (event.getPointerCount() > 1) {
                    this.mTouchEndP1.x = (int) event.getX(0);
                    this.mTouchEndP1.y = (int) event.getY(0);
                    this.mTouchEndP2.x = (int) event.getX(1);
                    this.mTouchEndP2.y = (int) event.getY(1);
                    int distance1 = this.mTouchEndP1.y - this.mTouchStartP1.y;
                    int distance2 = this.mTouchEndP2.y - this.mTouchStartP2.y;
                    if (distance1 * distance2 > 0 && (Math.abs(distance1) > 10 || Math.abs(distance2) > 10)) {
                        float tmpB = this.mB - ((((float) (distance1 + distance2)) * 0.05f) / ((float) mAxis_height));
                        float tmp1 = this.mK1 - tmpB;
                        float tmp2 = this.mK2 + tmpB;
                        if (tmp1 >= 0.0f && tmp1 <= 1.0f && tmp2 >= 0.0f && tmp2 <= 1.0f) {
                            this.mB = tmpB;
                            this.mCurveValue_l = tmp1;
                            this.mCurveValue_r = tmp2;
                        }
                    }
                } else {
                    int tmpY = (int) event.getY(0);
                    this.mTouchEndP1.x = (int) event.getX(0);
                    this.mTouchEndP1.y = tmpY;
                    int distance = this.mTouchEndP1.y - this.mTouchStartP1.y;
                    if (Math.abs(distance) > 10) {
                        if (this.mChangeCurveLeftRate) {
                            this.mCurveValue_l += ((float) distance) / ((float) mAxis_height);
                            if (this.mCurveValue_l < Utilities.B_SWITCH_MIN) {
                                this.mCurveValue_l = Utilities.B_SWITCH_MIN;
                            } else if (this.mCurveValue_l > 1.0f) {
                                this.mCurveValue_l = 1.0f;
                            }
                            this.mCurveValue_r += ((float) distance) / ((float) mAxis_height);
                            if (this.mCurveValue_r < Utilities.B_SWITCH_MIN) {
                                this.mCurveValue_r = Utilities.B_SWITCH_MIN;
                            } else if (this.mCurveValue_r > 1.0f) {
                                this.mCurveValue_r = 1.0f;
                            }
                            this.mK1 = this.mCurveValue_l + this.mB;
                            this.mK2 = this.mCurveValue_r - this.mB;
                        } else if (this.mChangeCurveRightRate) {
                            this.mCurveValue_l -= ((float) distance) / ((float) mAxis_height);
                            if (this.mCurveValue_l < Utilities.B_SWITCH_MIN) {
                                this.mCurveValue_l = Utilities.B_SWITCH_MIN;
                            } else if (this.mCurveValue_l > 1.0f) {
                                this.mCurveValue_l = 1.0f;
                            }
                            this.mCurveValue_r -= ((float) distance) / ((float) mAxis_height);
                            if (this.mCurveValue_r < Utilities.B_SWITCH_MIN) {
                                this.mCurveValue_r = Utilities.B_SWITCH_MIN;
                            } else if (this.mCurveValue_r > 1.0f) {
                                this.mCurveValue_r = 1.0f;
                            }
                            this.mK1 = this.mCurveValue_l + this.mB;
                            this.mK2 = this.mCurveValue_r - this.mB;
                        } else if (this.mChangeCurveLeftExpo) {
                            tmpN = this.mN1 - (((float) distance) * (this.mN1 > 1.0f ? 0.001f : 0.005f));
                            if (tmpN >= Utilities.N_MIN && tmpN <= Utilities.N_MAX) {
                                this.mN1 = tmpN;
                            }
                        } else if (this.mChangeCurveRightExpo) {
                            tmpN = this.mN2 + (((float) distance) * (this.mN2 > 1.0f ? 0.005f : 0.001f));
                            if (tmpN >= Utilities.N_MIN && tmpN <= Utilities.N_MAX) {
                                this.mN2 = tmpN;
                            }
                        } else if (this.mChangeWholeCurveExpo) {
                            tmpN = this.mN1 + (((float) distance) * (this.mN1 > 1.0f ? 0.005f : 0.001f));
                            if (tmpN >= Utilities.N_MIN && tmpN <= Utilities.N_MAX) {
                                this.mN2 = tmpN;
                                this.mN1 = tmpN;
                            }
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

    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
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

    public void setParams(float userValue_l, float userValue_r, float expoL, float expoR, float offset) {
        this.mCurveValue_l = convertUserValueToCurveValueL((float) this.mOutputMax, (float) this.mOutputMin, userValue_l);
        this.mCurveValue_r = convertUserValueToCurveValueR((float) this.mOutputMax, (float) this.mOutputMin, userValue_r);
        this.mN1 = Utilities.convertExpoToCoefficientN(expoL);
        this.mN2 = Utilities.convertExpoToCoefficientN(expoR);
        this.mB = offset;
        this.mK1 = this.mCurveValue_l + this.mB;
        this.mK2 = this.mCurveValue_r - this.mB;
        redraw();
    }

    public void changeCurvePaint(Paint paint) {
        this.mCurrentPaint = paint;
    }

    public void drawCurve() {
        int startX = this.mAllPoint[0].x;
        int startY = this.mAllPoint[0].y;
        int midX = this.mAllPoint[this.mAllPoint.length / 2].x;
        int midY = this.mAllPoint[this.mAllPoint.length / 2].y;
        Canvas canvas = getHolder().lockCanvas();
        int i;
        if (this.mSeparate) {
            Path path1 = new Path();
            path1.moveTo((float) startX, (float) startY);
            Path path2 = new Path();
            path2.moveTo((float) midX, (float) midY);
            i = 1;
            int j = this.mAllPoint.length / 2;
            while (i < this.mAllPoint.length / 2) {
                path1.lineTo((float) this.mAllPoint[i].x, (float) this.mAllPoint[i].y);
                path2.lineTo((float) this.mAllPoint[j].x, (float) this.mAllPoint[j].y);
                i++;
                j++;
            }
            canvas.drawColor(0, Mode.CLEAR);
            this.mCurveCanvas.drawColor(0, Mode.CLEAR);
            this.mCurveCanvas.drawPath(path1, this.mPaint1);
            this.mCurveCanvas.drawPath(path2, this.mPaint2);
        } else {
            Path path = new Path();
            path.moveTo((float) startX, (float) startY);
            for (i = 1; i < this.mAllPoint.length - 1; i++) {
                path.lineTo((float) this.mAllPoint[i].x, (float) this.mAllPoint[i].y);
            }
            canvas.drawColor(0, Mode.CLEAR);
            this.mCurveCanvas.drawColor(0, Mode.CLEAR);
            this.mCurveCanvas.drawPath(path, this.mCurrentPaint);
        }
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
                if (this.mSeparate) {
                    this.mChangeCurveLeftExpo = true;
                    return;
                } else {
                    this.mChangeWholeCurveExpo = true;
                    return;
                }
            case 4:
                if (this.mSeparate) {
                    this.mChangeCurveRightExpo = true;
                    return;
                } else {
                    this.mChangeWholeCurveExpo = true;
                    return;
                }
            case 5:
                this.mChangeWholeCurveOffset = true;
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
        this.mChangeCurveLeftExpo = false;
        this.mChangeCurveRightExpo = false;
        this.mChangeWholeCurveRate = false;
        this.mChangeWholeCurveExpo = false;
        this.mChangeWholeCurveOffset = false;
    }

    private void redraw() {
        if (this.mOnCurveTouchListener != null) {
            this.mOnCurveTouchListener.onCurveValueChanged(1, this.mCurveValue_l);
            this.mOnCurveTouchListener.onCurveValueChanged(2, this.mCurveValue_r);
            this.mOnCurveTouchListener.onCurveValueChanged(5, this.mN1);
            this.mOnCurveTouchListener.onCurveValueChanged(6, this.mB);
        }
        this.mAllPoint = Utilities.countAllPointOfExpo2(mAxis.getAxisX(), mAxis.getAxisY(), mAxis_width, mAxis_height, this.mK1, this.mK2, this.mN1, this.mN2, this.mB);
        if (this.isSurfaceReady) {
            drawCurve();
            if (this.mAllPointsUpdateListener != null) {
                this.mAllPointsUpdateListener.onUpdated(getAllPointsPosition());
            }
        }
    }

    public void setSeparate(boolean separate) {
        this.mSeparate = separate;
    }

    public float getLeftExpoValue(float coeValue) {
        return Utilities.convertCoefficientNtoExpo(coeValue);
    }

    public float getRightExpoValue(float coeValue) {
        return Utilities.convertCoefficientNtoExpo(coeValue);
    }

    public boolean isSeparated() {
        return this.mSeparate;
    }

    public float[] getPointForFlightControl() {
        return null;
    }

    protected void onRefresh() {
    }

    public int[] getAllPointsPosition() {
        int[] value = new int[this.mAllPoint.length];
        for (int i = 0; i < this.mAllPoint.length; i++) {
            value[i] = (mAxis.getAxisY() + mAxis_height) - this.mAllPoint[i].y;
        }
        return value;
    }

    public static int[] getAllPointsChValue(Context context, float max, float min, float userValue_l, float userValue_r, float expoL, float expoR, float offset) {
        int axisWidth = (int) context.getResources().getDimension(R.dimen.channelsetting_axis_width);
        int axisHeight = (int) context.getResources().getDimension(R.dimen.channelsetting_axis_height);
        float curveValue_l = convertUserValueToCurveValueL(max, min, userValue_l);
        float b = offset;
        Point[] allPoints = Utilities.countAllPointOfExpo2(40, 10, axisWidth, axisHeight, curveValue_l + b, convertUserValueToCurveValueR(max, min, userValue_r) - b, Utilities.convertExpoToCoefficientN(expoL), Utilities.convertExpoToCoefficientN(expoR), b);
        int[] value = new int[allPoints.length];
        for (int i = 0; i < allPoints.length; i++) {
            value[i] = (((10 + axisHeight) - allPoints[i].y) * ChannelSettings.STICK_RATE_125_OR_150) / axisHeight;
        }
        return value;
    }

    public static float convertChannelValueToCurveValue(float ch_value) {
        if (ch_value >= 0.0f && ch_value < 2048.0f) {
            return ((2048.0f - ch_value) * 1.0f) / 2048.0f;
        }
        if (ch_value < 2048.0f || ch_value > 4095.0f) {
            return 0.0f;
        }
        return ((ch_value - 2048.0f) * 1.0f) / 2048.0f;
    }

    public static float convertUserValueToCurveValueL(float max, float min, float user_value) {
        return 1.0f - ((Utilities.K_MAX * (user_value - min)) / (max - min));
    }

    public static float convertUserValueToCurveValueR(float max, float min, float user_value) {
        return ((Utilities.K_MAX * (user_value - min)) / (max - min)) - 1.0f;
    }

    public float convertUserValueToCurveValue(float user_value) {
        return ((Utilities.K_MAX * (user_value - ((float) this.mOutputMin))) / ((float) (this.mOutputMax - this.mOutputMin))) - 1.0f;
    }

    public float convertCurveValueToUserValueL(float curve_value) {
        return ((float) this.mOutputMin) + (((1.0f - curve_value) * ((float) (this.mOutputMax - this.mOutputMin))) / Utilities.K_MAX);
    }

    public float convertCurveValueToUserValueR(float curve_value) {
        return ((float) this.mOutputMin) + (((1.0f + curve_value) * ((float) (this.mOutputMax - this.mOutputMin))) / Utilities.K_MAX);
    }

    public float convertCurveValueToUserValue(float curve_value) {
        return ((float) this.mOutputMin) + (((1.0f + curve_value) * ((float) (this.mOutputMax - this.mOutputMin))) / Utilities.K_MAX);
    }
}

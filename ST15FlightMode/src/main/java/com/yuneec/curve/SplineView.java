package com.yuneec.curve;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import com.appunite.ffmpeg.ViewCompat;
import com.yuneec.channelsettings.ChannelSettings;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;

public class SplineView extends CurveView {
    private static final int POINT_OFFSET = 25;
    private static final String TAG = "SplineView";
    private static int mKeyPointNum;
    private boolean isSurfaceReady = false;
    private int[] mAllPoint;
    private float[] mKeyPointRelativeX;
    private float[] mKeyPointRelativeY;
    private int[] mKeyPointX;
    private int[] mKeyPointY;
    private int mMaxX;
    private int mMaxY;
    private int mMinX;
    private int mMinY;
    public boolean mSeparate = false;
    private boolean mTouchEnable = false;
    private int mTouchPoint;

    public SplineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SplineView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SplineView(Context context) {
        super(context);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        initKeyPointsPosition();
        this.mMaxX = mAxis.getAxisX() + mAxis_width;
        this.mMinX = mAxis.getAxisX();
        this.mMaxY = mAxis.getAxisY() + mAxis_height;
        this.mMinY = mAxis.getAxisY();
        this.mAllPoint = new int[(mAxis_width + 1)];
        if (this.mKeyPointX != null && this.mKeyPointY != null) {
            if (this.mKeyPointX.length == 5 && this.mKeyPointY.length == 5) {
                countAllPoints();
            } else {
                Log.e(TAG, "Point number error!");
            }
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        super.surfaceChanged(holder, format, width, height);
        curveDraw();
        this.isSurfaceReady = true;
    }

    private boolean countAllPoints() {
        return JniTools.getCurvePointFromJNI(this.mKeyPointX, this.mKeyPointY, this.mMaxX, this.mMinX, this.mMaxY, this.mMinY, this.mAllPoint);
    }

    private void countKeyPointsRealPosition() {
        if (this.mKeyPointRelativeX == null || this.mKeyPointRelativeY == null) {
            Log.w(TAG, "Axis has not been inited");
        } else if (this.mKeyPointRelativeX.length == mKeyPointNum && this.mKeyPointRelativeY.length == mKeyPointNum) {
            for (int i = 0; i < mKeyPointNum; i++) {
                this.mKeyPointX[i] = countRealPositonX(this.mKeyPointRelativeX[i]);
                this.mKeyPointY[i] = countRealPositonY(this.mKeyPointRelativeY[i]);
            }
        } else {
            Log.w(TAG, "Key points value is wrong");
        }
    }

    public void setParams(float[] x, float[] y) {
        if (x.length != y.length) {
            Log.e(TAG, "input xy not matched");
        } else if (x.length < 3) {
            Log.e(TAG, "xy length must larger than 3");
        } else {
            this.mKeyPointRelativeX = x;
            this.mKeyPointRelativeY = y;
            redraw();
        }
    }

    private void redraw() {
        countKeyPointsRealPosition();
        countAllPoints();
        if (this.isSurfaceReady) {
            curveDraw();
            if (this.mAllPointsUpdateListener != null) {
                this.mAllPointsUpdateListener.onUpdated(getAllPointsPosition());
            }
        }
    }

    private void initKeyPointsPosition() {
        mKeyPointNum = this.mAxis_lon_lines;
        this.mKeyPointX = new int[mKeyPointNum];
        this.mKeyPointY = new int[mKeyPointNum];
        countKeyPointsRealPosition();
    }

    public void finish() {
        JniTools.destoryFromJNI();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        this.isSurfaceReady = false;
        this.mKeyPointRelativeX = null;
        this.mKeyPointRelativeY = null;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mKeyPointX == null || this.mKeyPointY == null) {
            Log.e(TAG, "Axis has not been inited");
            return false;
        } else if (!isEnabled()) {
            return super.onTouchEvent(event);
        } else {
            Point point = new Point();
            point.x = (int) event.getX();
            point.y = (int) event.getY();
            if (event.getAction() == 0) {
                int i = 0;
                while (i < this.mKeyPointX.length) {
                    if (Math.abs(this.mKeyPointX[i] - point.x) < 25 && Math.abs(this.mKeyPointY[i] - point.y) < 25) {
                        this.mTouchEnable = true;
                        this.mTouchPoint = i;
                    }
                    i++;
                }
            }
            if (event.getAction() == 1) {
                this.mTouchEnable = false;
            }
            if (event.getAction() != 2 || !this.mTouchEnable) {
                return true;
            }
            int tmp_Y = this.mKeyPointY[this.mTouchPoint];
            this.mKeyPointY[this.mTouchPoint] = (int) event.getY();
            if (this.mKeyPointY[this.mTouchPoint] > this.mMaxY) {
                this.mKeyPointY[this.mTouchPoint] = this.mMaxY;
            } else if (this.mKeyPointY[this.mTouchPoint] < this.mMinY) {
                this.mKeyPointY[this.mTouchPoint] = this.mMinY;
            }
            if (countAllPoints()) {
                curveDraw();
                if (this.mOnCurveTouchListener != null) {
                    this.mOnCurveTouchListener.onCurveValueChanged(this.mTouchPoint);
                }
                if (this.mAllPointsUpdateListener == null) {
                    return true;
                }
                this.mAllPointsUpdateListener.onUpdated(getAllPointsPosition());
                return true;
            }
            this.mKeyPointY[this.mTouchPoint] = tmp_Y;
            return true;
        }
    }

    private void curveDraw() {
        if (this.mKeyPointX.length == 5 && this.mKeyPointY.length == 5 && this.mAllPoint.length != 0) {
            int startX = this.mKeyPointX[0];
            int startY = this.mKeyPointY[0];
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Style.STROKE);
            paint.setColor(-65536);
            paint.setStrokeWidth(1.0f);
            Paint paintP = new Paint();
            paintP.setAntiAlias(true);
            paintP.setStyle(Style.FILL);
            paintP.setShadowLayer(7.0f, 0.0f, 0.0f, -16711936);
            paintP.setColor(-16711936);
            paintP.setStrokeWidth(1.0f);
            paintP.setTextSize(14.0f);
            Path path = new Path();
            path.moveTo((float) startX, (float) startY);
            Canvas canvas = getHolder().lockCanvas();
            if (canvas == null) {
                Log.w(TAG, "Surface not ready,abort draw");
                return;
            }
            int i = startX + 1;
            int j = 1;
            while (i < this.mMaxX) {
                path.lineTo((float) i, (float) this.mAllPoint[j]);
                i++;
                j++;
            }
            canvas.drawColor(0, Mode.CLEAR);
            this.mCurveCanvas.drawColor(0, Mode.CLEAR);
            if (this.mKeyPointDisplayed) {
                for (i = 0; i < this.mKeyPointX.length; i++) {
                    this.mCurveCanvas.drawCircle((float) this.mKeyPointX[i], (float) this.mKeyPointY[i], Utilities.N_MAX, paintP);
                    this.mCurveCanvas.drawText(String.valueOf(i + 1), ((float) this.mKeyPointX[i]) + 7.0f, ((float) this.mKeyPointY[i]) - 7.0f, paintP);
                }
            }
            this.mCurveCanvas.drawPath(path, paint);
            canvas.drawBitmap(this.mCurveBitmap, 0.0f, 0.0f, null);
            getHolder().unlockCanvasAndPost(canvas);
            return;
        }
        Log.e(TAG, "Points error");
    }

    private void clearDraw() {
        Canvas canvas = getHolder().lockCanvas(null);
        canvas.drawColor(ViewCompat.MEASURED_STATE_MASK);
        getHolder().unlockCanvasAndPost(canvas);
    }

    public void setPointValue(int index, int value) {
        this.mKeyPointY[index] = (mAxis.getAxisY() + mAxis_height) - (((value - this.mOutputMin) * mAxis_height) / (this.mOutputMax - this.mOutputMin));
        countAllPoints();
        redraw();
    }

    public float getPointValue(int index) {
        return (float) (this.mOutputMin + ((((mAxis.getAxisY() + mAxis_height) - this.mKeyPointY[index]) * (this.mOutputMax - this.mOutputMin)) / mAxis_height));
    }

    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
    }

    protected void onRefresh() {
    }

    public int[] getAllPointsPosition() {
        int[] value = new int[this.mAllPoint.length];
        for (int i = 0; i < this.mAllPoint.length; i++) {
            value[i] = (mAxis.getAxisY() + mAxis_height) - this.mAllPoint[i];
        }
        return value;
    }

    public static int[] getAllPointsChValue(Context context, float max, float min, float[] x, float[] y) {
        int axisWidth = (int) context.getResources().getDimension(R.dimen.channelsetting_axis_width);
        int axisHeight = (int) context.getResources().getDimension(R.dimen.channelsetting_axis_height);
        Point[] allPoints = countAllPointOfSpline(40, 10, axisWidth, axisHeight, CurveView.convertValueXtoPosition(axisHeight, axisWidth, x), CurveView.convertValueYtoPosition(10, axisHeight, max, min, y));
        int[] value = new int[allPoints.length];
        for (int i = 0; i < allPoints.length; i++) {
            value[i] = (((10 + axisHeight) - allPoints[i].y) * ChannelSettings.STICK_RATE_125_OR_150) / axisHeight;
        }
        return value;
    }

    public static Point[] countAllPointOfSpline(int axisX, int axisY, int axisWidth, int axisHeight, int[] x, int[] y) {
        Point[] allPoints = new Point[(axisWidth + 1)];
        if (!(x == null || y == null)) {
            int maxX = x[x.length - 1];
            int[] allPointY = new int[maxX];
            JniTools.getCurvePointFromJNI(x, y, maxX, x[0], axisY + axisHeight, axisY, allPointY);
            for (int i = 0; i <= axisWidth; i++) {
                allPoints[i] = new Point();
                allPoints[i].x = axisX + i;
                allPoints[i].y = allPointY[i];
            }
        }
        return allPoints;
    }
}

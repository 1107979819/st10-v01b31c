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
import com.yuneec.channelsettings.ChannelSettings;
import com.yuneec.flightmode15.R;
import com.yuneec.flightmode15.Utilities;

public class BrokenLineView extends CurveView {
    private static final int POINT_OFFSET = 25;
    private static final String TAG = "BrokenLineView";
    private static int mKeyPointNum;
    private boolean isSurfaceReady;
    private float[] mKeyPointRelativeX;
    private float[] mKeyPointRelativeY;
    private int[] mKeyPointX;
    private int[] mKeyPointY;
    private int mMaxY;
    private int mMinY;
    public boolean mSeparate;
    private boolean mTouchEnable;
    private int mTouchPoint;

    public BrokenLineView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mTouchEnable = false;
        this.mSeparate = false;
        this.isSurfaceReady = false;
    }

    public BrokenLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BrokenLineView(Context context) {
        this(context, null);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        initPosition();
        this.mMaxY = mAxis.getAxisY() + mAxis_height;
        this.mMinY = mAxis.getAxisY();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        super.surfaceChanged(holder, format, width, height);
        curveDraw();
        this.isSurfaceReady = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        this.isSurfaceReady = false;
        this.mKeyPointRelativeX = null;
        this.mKeyPointRelativeY = null;
    }

    public void setParams(float[] x, float[] y) {
        if (x.length != y.length) {
            Log.e(TAG, "input xy not matched");
        } else if (x.length < 3) {
            Log.e(TAG, "xy length must larger than 3");
        } else {
            this.mKeyPointRelativeX = x;
            this.mKeyPointRelativeY = y;
            countKeyPointsRealPosition();
            redraw();
        }
    }

    private void initPosition() {
        mKeyPointNum = this.mAxis_lon_lines;
        this.mKeyPointX = new int[mKeyPointNum];
        this.mKeyPointY = new int[mKeyPointNum];
        countKeyPointsRealPosition();
    }

    private void redraw() {
        if (this.isSurfaceReady) {
            curveDraw();
            if (this.mAllPointsUpdateListener != null) {
                this.mAllPointsUpdateListener.onUpdated(getAllPointsPosition());
            }
        }
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

    private void curveDraw() {
        if (this.mKeyPointX.length == 0 || this.mKeyPointY.length == 0) {
            Log.w(TAG, "Curve not initialized");
            return;
        }
        int i;
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
        for (i = 1; i < this.mKeyPointX.length; i++) {
            path.lineTo((float) this.mKeyPointX[i], (float) this.mKeyPointY[i]);
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
    }

    public boolean onTouchEvent(MotionEvent event) {
        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();
        if (!isEnabled()) {
            return super.onTouchEvent(event);
        }
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
        int tmpY = (int) event.getY();
        if (tmpY > this.mMaxY || tmpY < this.mMinY) {
            return true;
        }
        this.mKeyPointY[this.mTouchPoint] = tmpY;
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

    public void setPointValue(int index, int value) {
        if (index < this.mKeyPointY.length) {
            this.mKeyPointY[index] = (mAxis.getAxisY() + mAxis_height) - (((value - this.mOutputMin) * mAxis_height) / (this.mOutputMax - this.mOutputMin));
            if (this.mAllPointsUpdateListener != null) {
                this.mAllPointsUpdateListener.onUpdated(getAllPointsPosition());
            }
            curveDraw();
        }
    }

    public float getPointValue(int index) {
        return (float) (this.mOutputMin + ((((mAxis.getAxisY() + mAxis_height) - this.mKeyPointY[index]) * (this.mOutputMax - this.mOutputMin)) / mAxis_height));
    }

    protected void onRefresh() {
    }

    public int[] getAllPointsPosition() {
        int index = 0;
        int[] value = new int[(mAxis_width + 1)];
        if (!((this.mKeyPointX == null && this.mKeyPointY == null) || this.mKeyPointY.length == 0)) {
            for (int i = 0; i < mKeyPointNum - 1; i++) {
                float k = ((float) (this.mKeyPointY[i + 1] - this.mKeyPointY[i])) / ((float) (this.mKeyPointX[i + 1] - this.mKeyPointX[i]));
                float b = ((float) this.mKeyPointY[i]) - (((float) this.mKeyPointX[i]) * k);
                for (int j = this.mKeyPointX[i]; j < this.mKeyPointX[i + 1]; j++) {
                    int point = (int) ((((float) j) * k) + b);
                    if (index < mAxis_width) {
                        value[index] = (mAxis.getAxisY() + mAxis_height) - point;
                    } else {
                        Log.e(TAG, "Point index > mAxis_width in loop");
                    }
                    index++;
                }
            }
            if (index == mAxis_width) {
                value[index] = (mAxis.getAxisY() + mAxis_height) - this.mKeyPointY[mKeyPointNum - 1];
            } else {
                Log.e(TAG, "Point index > mAxis_width at the last point");
            }
        }
        return value;
    }

    public static int[] getAllPointsChValue(Context context, float max, float min, float[] valueX, float[] valueY) {
        int axisWidth = (int) context.getResources().getDimension(R.dimen.channelsetting_axis_width);
        int axisHeight = (int) context.getResources().getDimension(R.dimen.channelsetting_axis_height);
        int[] pos_x = CurveView.convertValueXtoPosition(axisHeight, axisWidth, valueX);
        int[] pos_y = CurveView.convertValueYtoPosition(10, axisHeight, max, min, valueY);
        int[] value = new int[(axisWidth + 1)];
        int index = 0;
        if (!((pos_x == null && pos_y == null) || pos_y.length == 0)) {
            for (int i = 0; i < pos_x.length - 1; i++) {
                for (int j = pos_x[i]; j < pos_x[i + 1]; j++) {
                    double k = (double) (((float) (pos_y[i + 1] - pos_y[i])) / ((float) (pos_x[i + 1] - pos_x[i])));
                    double point = (((double) j) * k) + (((double) pos_y[i]) - (((double) pos_x[i]) * k));
                    if (index < axisWidth) {
                        value[index] = (((int) (((double) (10 + axisHeight)) - point)) * ChannelSettings.STICK_RATE_125_OR_150) / axisHeight;
                    }
                    index++;
                }
            }
            value[axisWidth] = (((10 + axisHeight) - pos_y[pos_y.length - 1]) * ChannelSettings.STICK_RATE_125_OR_150) / axisHeight;
        }
        return value;
    }

    public static Point[] countAllPointOfBrokenLine(int axisX, int axisY, int axisWidth, int axisHeight, int[] x, int[] y) {
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

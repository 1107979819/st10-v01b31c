package com.yuneec.curve;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import com.yuneec.channelsettings.ChannelSettings;
import com.yuneec.flightmode15.R;

public abstract class CurveView extends SurfaceView implements Callback {
    public static final int COE_TYPE_EXPO_L = 3;
    public static final int COE_TYPE_EXPO_R = 4;
    public static final int COE_TYPE_EXPO_W = 5;
    public static final int COE_TYPE_OFFSET = 6;
    public static final int COE_TYPE_RATE_L = 1;
    public static final int COE_TYPE_RATE_R = 2;
    private static final int MSG_DRAW_COORDINATE_X = 2;
    private static final int MSG_DRAW_COORDINATE_Y = 3;
    private static final int MSG_DRAW_CURVE = 0;
    private static final int MSG_DRAW_POINTS = 4;
    private static final int MSG_DRAW_STICK = 1;
    private static final String TAG = "CurveView";
    protected static CurveAxis mAxis;
    protected static int mAxis_height;
    protected static int mAxis_width;
    protected Point[] mAllPoint;
    protected onAllPointsUpdateListener mAllPointsUpdateListener;
    protected int mAxis_hor_lines;
    protected int mAxis_lon_lines;
    protected boolean mCoordinateDisplayed;
    protected Bitmap mCurveBitmap;
    protected Canvas mCurveCanvas;
    private DrawerThread mDrawer;
    private EarlyMessage mEarlyMessage;
    protected boolean mIsSwitch;
    protected boolean mKeyPointDisplayed;
    protected OnCurveTouchListener mOnCurveTouchListener;
    protected int mOutputMax;
    protected int mOutputMin;
    protected boolean mStickDisplayed;
    protected int mStickLastValue;

    private class DrawerThread extends Thread {
        private Handler mHandler;
        private Looper mLooper;

        private DrawerThread() {
        }

        public void run() {
            Looper.prepare();
            this.mLooper = Looper.myLooper();
            this.mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 0:
                        case 2:
                        case 3:
                        case 4:
                            return;
                        case 1:
                            CurveView.this.drawStick(msg.arg1);
                            return;
                        default:
                            Log.i(CurveView.TAG, "Unknown Message:" + msg.what);
                            return;
                    }
                }
            };
            if (CurveView.this.mEarlyMessage != null) {
                CurveView.this.drawStick(CurveView.this.mEarlyMessage.mValue);
                CurveView.this.mEarlyMessage = null;
            }
            Looper.loop();
        }

        private void stopDrawThread() {
            if (this.mLooper == null) {
                Log.i(CurveView.TAG, "Looper not running,namely,thread is not running");
            } else {
                this.mLooper.quit();
            }
        }
    }

    private class EarlyMessage {
        int mValue;

        public EarlyMessage(int mValue) {
            this.mValue = mValue;
        }
    }

    public interface OnCurveTouchListener {
        void onCurveValueChanged(int i);

        void onCurveValueChanged(int i, float f);
    }

    public interface onAllPointsUpdateListener {
        void onUpdated(int[] iArr);
    }

    protected abstract void onRefresh();

    public CurveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.CurveView, defStyle, 0);
        mAxis_width = attributesArray.getDimensionPixelSize(0, -1);
        mAxis_height = attributesArray.getDimensionPixelSize(1, -1);
        this.mAxis_hor_lines = attributesArray.getInteger(2, 7);
        this.mAxis_lon_lines = attributesArray.getInteger(3, 9);
        this.mOutputMax = getResources().getInteger(R.integer.def_channelsetting_nor_curve_y_Max);
        this.mOutputMin = getResources().getInteger(R.integer.def_channelsetting_nor_curve_y_Min);
        this.mCoordinateDisplayed = attributesArray.getBoolean(6, true);
        this.mKeyPointDisplayed = attributesArray.getBoolean(7, true);
        this.mStickDisplayed = attributesArray.getBoolean(8, true);
        this.mIsSwitch = attributesArray.getBoolean(9, false);
        context.getResources().getDimension(R.dimen.channelsetting_curve_view_width);
        context.getResources().getDimension(R.dimen.channelsetting_curve_view_height);
        mAxis = new CurveAxis(0, 0, mAxis_width, mAxis_height, this.mAxis_hor_lines, this.mAxis_lon_lines, this.mOutputMax, this.mOutputMin, this.mCoordinateDisplayed, this.mIsSwitch);
    }

    public CurveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CurveView(Context context) {
        this(context, null);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        getHolder().addCallback(this);
        getHolder().setFormat(-3);
        setBackgroundDrawable(mAxis);
        this.mAllPoint = new Point[(mAxis_width + 1)];
        if (this.mAllPointsUpdateListener != null) {
            this.mAllPointsUpdateListener.onUpdated(getAllPointsPosition());
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mCurveBitmap = Bitmap.createBitmap((int) getResources().getDimension(R.dimen.channelsetting_curve_view_width), (int) getResources().getDimension(R.dimen.channelsetting_curve_view_height), Config.ARGB_8888);
        this.mCurveCanvas = new Canvas(this.mCurveBitmap);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mCurveBitmap.recycle();
        this.mCurveCanvas = null;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        this.mDrawer = new DrawerThread();
        this.mDrawer.start();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (this.mAllPointsUpdateListener != null) {
            this.mAllPointsUpdateListener.onUpdated(getAllPointsPosition());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        this.mDrawer.stopDrawThread();
        this.mDrawer = null;
    }

    public void drawStick(int value) {
        if (this.mStickDisplayed) {
            if (value < 0) {
                value = 0;
            } else if (value > ChannelSettings.STICK_RATE_125_OR_150) {
                value = ChannelSettings.STICK_RATE_125_OR_150;
            }
            int stickCoordinate = mAxis.getAxisX() + ((mAxis_width * value) / ChannelSettings.STICK_RATE_125_OR_150);
            Paint stickPaint = new Paint();
            stickPaint.setAntiAlias(true);
            stickPaint.setStyle(Style.STROKE);
            stickPaint.setColor(getResources().getColor(R.color.color_orange));
            stickPaint.setStrokeWidth(3.0f);
            Canvas canvas = getHolder().lockCanvas();
            if (canvas != null) {
                Path path = new Path();
                path.moveTo((float) stickCoordinate, (float) mAxis.getAxisY());
                path.lineTo((float) stickCoordinate, (float) (mAxis.getAxisY() + mAxis_height));
                canvas.drawColor(0, Mode.CLEAR);
                canvas.drawBitmap(this.mCurveBitmap, 0.0f, 0.0f, null);
                canvas.drawPath(path, stickPaint);
                getHolder().unlockCanvasAndPost(canvas);
            }
            this.mStickLastValue = value;
        }
    }

    public void refreshStick(int value) {
        if (this.mDrawer == null || this.mDrawer.mHandler == null) {
            Log.w(TAG, "Drawer Thread Handler is null!!");
            if (this.mEarlyMessage == null) {
                this.mEarlyMessage = new EarlyMessage(value);
                return;
            }
            return;
        }
        Message mDraw_msg = this.mDrawer.mHandler.obtainMessage();
        mDraw_msg.what = 1;
        mDraw_msg.arg1 = value;
        this.mDrawer.mHandler.sendMessage(mDraw_msg);
    }

    public void sethorizontalLines(int lines) {
        this.mAxis_hor_lines = lines;
    }

    public void setLongitudinalLines(int lines) {
        this.mAxis_lon_lines = lines;
    }

    public void setOutputValueMax(int outputMax) {
        this.mOutputMax = outputMax;
        refresh();
    }

    public void setOutputValueMin(int outputMin) {
        this.mOutputMin = outputMin;
        refresh();
    }

    public static int getAxisHeight() {
        return mAxis_height;
    }

    public static int getAxisWidth() {
        return mAxis_width;
    }

    protected void onCurveChanges() {
        if (this.mStickDisplayed) {
            refreshStick(this.mStickLastValue);
        }
    }

    public void setOnCurveTouchListener(OnCurveTouchListener onCurveTouchListener) {
        this.mOnCurveTouchListener = onCurveTouchListener;
    }

    public void refresh() {
        mAxis = null;
        mAxis = new CurveAxis(0, 0, mAxis_width, mAxis_height, this.mAxis_hor_lines, this.mAxis_lon_lines, this.mOutputMax, this.mOutputMin, this.mCoordinateDisplayed, this.mIsSwitch);
        setBackgroundDrawable(mAxis);
        onRefresh();
    }

    public float convertChannelValueToUserValue(float ch_value) {
        return ((float) this.mOutputMin) + ((((float) (this.mOutputMax - this.mOutputMin)) * ch_value) / 4095.0f);
    }

    protected float convertUserValueToChannelValue(float user_Value) {
        return ((user_Value - ((float) this.mOutputMin)) * 4095.0f) / ((float) (this.mOutputMax - this.mOutputMin));
    }

    protected int countRealPositonX(float user_value) {
        return (int) (((float) mAxis.getAxisX()) + (((100.0f + user_value) * ((float) mAxis_width)) / 200.0f));
    }

    protected int countRealPositonY(float user_value) {
        return (int) (((float) (mAxis.getAxisY() + mAxis_height)) - (((user_value - ((float) this.mOutputMin)) * ((float) mAxis_height)) / ((float) (this.mOutputMax - this.mOutputMin))));
    }

    protected int[] getAllPointsPosition() {
        int[] value = new int[this.mAllPoint.length];
        for (int i = 0; i < this.mAllPoint.length; i++) {
            value[i] = (mAxis.getAxisY() + mAxis_height) - this.mAllPoint[i].y;
        }
        return value;
    }

    public CurveAxis getCurveAxis() {
        return mAxis;
    }

    public void setOnAllPointsUpdateListener(onAllPointsUpdateListener allPointsUpdateListener) {
        this.mAllPointsUpdateListener = allPointsUpdateListener;
    }

    public static int[] convertValueXtoPosition(int axisX, int axisWidth, float[] value) {
        int[] pos = new int[value.length];
        for (int i = 0; i < value.length; i++) {
            pos[i] = (int) (((float) axisX) + (((value[i] + 100.0f) * ((float) axisWidth)) / 200.0f));
        }
        return pos;
    }

    public static int[] convertValueYtoPosition(int axisY, int axisHeight, float maxValue, float minValue, float[] value) {
        int[] pos = new int[value.length];
        for (int i = 0; i < value.length; i++) {
            pos[i] = (int) (((float) (axisY + axisHeight)) - (((value[i] - minValue) * ((float) axisHeight)) / (maxValue - minValue)));
        }
        return pos;
    }
}

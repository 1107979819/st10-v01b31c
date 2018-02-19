package com.yuneec.curve;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class CurveAxis extends Drawable {
    public static final int AXIS_LEFT_OFFSET = 40;
    public static final int AXIS_TOP_OFFSET = 10;
    private static final String[] horizontal_ordinate = new String[]{"-100", "-50", "0", "50", "100"};
    private int mAxisY_max;
    private int mAxisY_min;
    private String[] mAxisY_value = null;
    private boolean mCoordinateDisplayed;
    private int mHorLine;
    private Point[] mHorLinesPoint = null;
    private boolean mIsSwitch;
    private int mLonLine;
    private Point[] mLonLinesPoint = null;
    private int mX;
    private int mXLength;
    private int mY;
    private int mYLength;

    public CurveAxis(int x, int y, int length, int line, int axisY_max, int axisY_min, boolean coordinateDisplayed, boolean isSwitch) {
        this.mCoordinateDisplayed = coordinateDisplayed;
        if (this.mCoordinateDisplayed) {
            this.mX = x + 40;
            this.mY = y + 10;
        } else {
            this.mX = 1;
            this.mY = 1;
        }
        this.mIsSwitch = isSwitch;
        this.mXLength = length;
        this.mYLength = length;
        this.mHorLine = line;
        this.mLonLine = line;
        this.mAxisY_max = axisY_max;
        this.mAxisY_min = axisY_min;
        this.mHorLinesPoint = new Point[line];
        this.mLonLinesPoint = new Point[line];
        this.mAxisY_value = new String[line];
        countLinePosition(this.mHorLine, this.mLonLine);
    }

    public CurveAxis(int x, int y, int xLength, int yLength, int horLine, int lonLine, int lon_max, int lon_min, boolean coordinateDisplayed, boolean isSwitch) {
        this.mCoordinateDisplayed = coordinateDisplayed;
        if (this.mCoordinateDisplayed) {
            this.mX = x + 40;
            this.mY = y + 10;
        } else {
            this.mX = 1;
            this.mY = 1;
        }
        this.mIsSwitch = isSwitch;
        this.mXLength = xLength;
        this.mYLength = yLength;
        this.mHorLine = horLine;
        this.mLonLine = lonLine;
        this.mAxisY_max = lon_max;
        this.mAxisY_min = lon_min;
        this.mHorLinesPoint = new Point[horLine];
        this.mLonLinesPoint = new Point[lonLine];
        this.mAxisY_value = new String[horLine];
        countLinePosition(this.mHorLine, this.mLonLine);
    }

    private void countLinePosition(int horLine, int lonLine) {
        int i;
        int horStartY = this.mY;
        int lonStartX = this.mX;
        int temp = this.mAxisY_max;
        for (i = 0; i < this.mHorLinesPoint.length; i++) {
            this.mHorLinesPoint[i] = new Point();
            this.mHorLinesPoint[i].x = this.mX;
            this.mHorLinesPoint[i].y = horStartY;
            horStartY += this.mYLength / (this.mHorLine - 1);
            this.mAxisY_value[i] = new String();
            this.mAxisY_value[i] = String.valueOf(temp - (((this.mAxisY_max - this.mAxisY_min) * i) / (this.mHorLine - 1)));
        }
        for (i = 0; i < this.mLonLinesPoint.length; i++) {
            this.mLonLinesPoint[i] = new Point();
            this.mLonLinesPoint[i].x = lonStartX;
            this.mLonLinesPoint[i].y = this.mY;
            lonStartX += this.mXLength / (this.mLonLine - 1);
        }
    }

    public void draw(Canvas canvas) {
        Canvas canvas2;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.STROKE);
        int lonEndY = (this.mY + this.mYLength) - 1;
        int i = 0;
        while (i < this.mLonLine) {
            if (i == 0 || i == this.mLonLine / 2 || i == this.mLonLine - 1) {
                paint.setColor(-1);
                paint.setStrokeWidth(1.0f);
            } else {
                paint.setColor(-7829368);
                paint.setStrokeWidth(0.8f);
            }
            int lonStartX = this.mLonLinesPoint[i].x;
            int lonEndX = lonStartX;
            canvas.drawLine((float) lonStartX, (float) this.mLonLinesPoint[i].y, (float) lonEndX, (float) lonEndY, paint);
            if (this.mCoordinateDisplayed) {
                if (!this.mIsSwitch) {
                    canvas2 = canvas;
                    canvas2.drawText(horizontal_ordinate[i], (float) (lonEndX - (getStringWidth(horizontal_ordinate[i], paint) / 2)), (float) (lonEndY + 20), paint);
                } else if (i == 0) {
                    canvas2 = canvas;
                    canvas2.drawText("0", (float) (lonEndX - (getStringWidth("0", paint) / 2)), (float) (lonEndY + 20), paint);
                } else if (i == this.mLonLine / 2) {
                    canvas2 = canvas;
                    canvas2.drawText("1", (float) (lonEndX - (getStringWidth("1", paint) / 2)), (float) (lonEndY + 20), paint);
                } else if (i == this.mLonLine - 1) {
                    canvas2 = canvas;
                    canvas2.drawText("2", (float) (lonEndX - (getStringWidth("2", paint) / 2)), (float) (lonEndY + 20), paint);
                }
            }
            i++;
        }
        int horEndX = this.mX + this.mXLength;
        i = 0;
        while (i < this.mHorLine) {
            if (i == 0 || i == this.mHorLine / 2 || i == this.mHorLine - 1) {
                paint.setColor(-1);
                paint.setStrokeWidth(1.0f);
            } else {
                paint.setColor(-7829368);
                paint.setStrokeWidth(0.8f);
            }
            int horStartX = this.mHorLinesPoint[i].x;
            int horStartY = this.mHorLinesPoint[i].y;
            canvas.drawLine((float) horStartX, (float) horStartY, (float) horEndX, (float) horStartY, paint);
            if (this.mCoordinateDisplayed) {
                canvas2 = canvas;
                canvas2.drawText(this.mAxisY_value[i], (float) ((horStartX - getStringWidth(this.mAxisY_value[i], paint)) - 10), (float) (horStartY + 3), paint);
            }
            i++;
        }
    }

    private int getStringWidth(String str, Paint paint) {
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        return rect.width();
    }

    public int getAxisX() {
        return this.mX;
    }

    public int getAxisY() {
        return this.mY;
    }

    public int getHeight() {
        return this.mYLength;
    }

    public int getWidth() {
        return this.mXLength;
    }

    public Point[] getHorLinesPosition() {
        return this.mHorLinesPoint;
    }

    public void setAlpha(int alpha) {
    }

    public void setColorFilter(ColorFilter cf) {
    }

    public int getOpacity() {
        return 0;
    }
}

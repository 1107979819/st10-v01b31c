package com.appunite.ffmpeg;

import android.annotation.TargetApi;
import android.os.Build.VERSION;
import android.view.View;
import android.view.View.MeasureSpec;
import com.yuneec.flightmode15.Utilities;

public class ViewCompat {
    public static final int MEASURED_SIZE_MASK = 16777215;
    public static final int MEASURED_STATE_MASK = -16777216;
    public static final int MEASURED_STATE_TOO_SMALL = 16777216;

    public static int resolveSize(int size, int measureSpec) {
        if (VERSION.SDK_INT >= 11) {
            return View.resolveSize(size, measureSpec);
        }
        return resolveSizeAndState(size, measureSpec, 0) & MEASURED_SIZE_MASK;
    }

    @TargetApi(11)
    public static int resolveSizeAndState(int size, int measureSpec, int childMeasuredState) {
        if (VERSION.SDK_INT >= 11) {
            return View.resolveSizeAndState(size, measureSpec, childMeasuredState);
        }
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case Utilities.FLAG_HOMEKEY_DISPATCHED /*-2147483648*/:
                if (specSize >= size) {
                    result = size;
                    break;
                }
                result = specSize | MEASURED_STATE_TOO_SMALL;
                break;
            case 0:
                result = size;
                break;
            case 1073741824:
                result = specSize;
                break;
        }
        return (MEASURED_STATE_MASK & childMeasuredState) | result;
    }
}

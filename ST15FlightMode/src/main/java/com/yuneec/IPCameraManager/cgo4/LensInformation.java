package com.yuneec.IPCameraManager.cgo4;

import android.util.Log;
import com.yuneec.flightmode15.Utilities;

public class LensInformation {
    private static final double FLOAT_CHANGE = 100000.0d;
    public String avMax;
    public String avMin;
    public String calcType;
    public String elecOperated;
    public String focusLensMax;
    public String focusLensMin;
    public String infiPos;
    public boolean isOk;
    public String lensMounting;
    public String mfAvail;
    public String tvMax;
    public String tvMin;

    public LensInformation(String response) {
        String[] info = response.split(",");
        if (info.length != 12) {
            Log.e("LensInformation", "invalid informations");
            return;
        }
        this.isOk = info[0].equals("ok");
        if (this.isOk) {
            this.avMax = parseAVrange(info[1]);
            this.avMin = parseAVrange(info[2]);
            this.tvMax = parseTVrange(info[3]);
            this.tvMin = parseTVrange(info[4]);
            this.calcType = info[5];
            this.elecOperated = info[6];
            this.focusLensMax = info[7];
            this.focusLensMin = info[8];
            this.mfAvail = info[9];
            this.infiPos = info[10];
            this.lensMounting = info[11];
        }
    }

    private String parseAVrange(String value) {
        String[] subValue = value.split("/");
        if (subValue.length != 2) {
            return null;
        }
        float decimalValue = ((float) Integer.parseInt(subValue[0])) / ((float) Integer.parseInt(subValue[1]));
        return String.format("F%.1f", new Object[]{Double.valueOf(Math.pow(2.0d, (double) (decimalValue / Utilities.K_MAX)))});
    }

    private String parseTVrange(String value) {
        String[] subValue = value.split("/");
        if (subValue.length != 2) {
            return null;
        }
        double convertValue = Math.pow(2.0d, (double) (-(((float) Integer.parseInt(subValue[0])) / ((float) Integer.parseInt(subValue[1])))));
        if (convertValue < 1.0d) {
            return toFraction(convertValue) + " s";
        }
        return String.format("%.1f s", new Object[]{Double.valueOf(convertValue)});
    }

    private static String toFraction(double d) {
        StringBuilder sb = new StringBuilder();
        if (d < 0.0d) {
            sb.append('-');
            d = -d;
        }
        sb.append(1).append("/");
        if (d >= 0.25d) {
            sb.append(Math.round(10.0d / d) / 10);
        } else {
            sb.append(Math.round(FLOAT_CHANGE / (d * FLOAT_CHANGE)));
        }
        return sb.toString();
    }
}

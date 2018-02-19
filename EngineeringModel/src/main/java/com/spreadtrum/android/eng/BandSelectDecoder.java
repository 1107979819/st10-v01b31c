package com.spreadtrum.android.eng;

import java.util.HashMap;
import java.util.Map.Entry;

/* compiled from: DebugParam */
class BandSelectDecoder {
    private static BandSelectDecoder mInstance;
    private HashMap<Integer, Integer> mDecoderMap = new HashMap();

    private BandSelectDecoder() {
        this.mDecoderMap.put(Integer.valueOf(0), Integer.valueOf(1));
        this.mDecoderMap.put(Integer.valueOf(1), Integer.valueOf(2));
        this.mDecoderMap.put(Integer.valueOf(2), Integer.valueOf(4));
        this.mDecoderMap.put(Integer.valueOf(3), Integer.valueOf(8));
        this.mDecoderMap.put(Integer.valueOf(4), Integer.valueOf(3));
        this.mDecoderMap.put(Integer.valueOf(5), Integer.valueOf(9));
        this.mDecoderMap.put(Integer.valueOf(6), Integer.valueOf(10));
        this.mDecoderMap.put(Integer.valueOf(7), Integer.valueOf(12));
        this.mDecoderMap.put(Integer.valueOf(8), Integer.valueOf(5));
        this.mDecoderMap.put(Integer.valueOf(9), Integer.valueOf(11));
        this.mDecoderMap.put(Integer.valueOf(10), Integer.valueOf(13));
        this.mDecoderMap.put(Integer.valueOf(11), Integer.valueOf(6));
        this.mDecoderMap.put(Integer.valueOf(12), Integer.valueOf(14));
        this.mDecoderMap.put(Integer.valueOf(13), Integer.valueOf(7));
        this.mDecoderMap.put(Integer.valueOf(14), Integer.valueOf(15));
    }

    public static BandSelectDecoder getInstance() {
        if (mInstance == null) {
            mInstance = new BandSelectDecoder();
        }
        return mInstance;
    }

    public int getBandsFromCmdParam(int cmd) {
        return ((Integer) this.mDecoderMap.get(Integer.valueOf(cmd))).intValue();
    }

    public int getCmdParamFromBands(int bands) {
        for (Entry<Integer, Integer> entry : this.mDecoderMap.entrySet()) {
            if (((Integer) entry.getValue()).intValue() == bands) {
                return ((Integer) entry.getKey()).intValue();
            }
        }
        return -1;
    }
}

package com.spreadtrum.android.eng;

public class EngWifieut {

    static class PtestCw {
        public int amplitude;
        public int band;
        public int channel;
        public int frequency;
        public int frequencyOffset;
        public int sFactor;

        PtestCw() {
        }
    }

    static class PtestRx {
        public int band;
        public int channel;
        public int filteringEnable;
        public int frequency;
        public int sFactor;

        PtestRx() {
        }
    }

    static class PtestTx {
        public int band;
        public int channel;
        public String destMacAddr;
        public int enablelbCsTestMode;
        public int interval;
        public int length;
        public int powerLevel;
        public int preamble;
        public int rate;
        public int sFactor;

        PtestTx() {
        }
    }

    private native int ptestBtStart();

    private native int ptestBtStop();

    private native int ptestCw(PtestCw ptestCw);

    private native int ptestDeinit();

    private native int ptestInit();

    private native int ptestRx(PtestRx ptestRx);

    private native void ptestSetValue();

    private native int ptestTx(PtestTx ptestTx);

    static {
        System.loadLibrary("engmodeljni");
    }

    public int testCw(PtestCw cw) {
        return ptestCw(cw);
    }

    public int testTx(PtestTx tx) {
        return ptestTx(tx);
    }

    public int testRx(PtestRx rx) {
        return ptestRx(rx);
    }

    public int testInit() {
        return ptestInit();
    }

    public int testDeinit() {
        return ptestDeinit();
    }

    public void testSetValue(int val) {
        ptestSetValue();
    }

    public int testBtStart() {
        return ptestBtStart();
    }

    public int testBtStop() {
        return ptestBtStop();
    }
}

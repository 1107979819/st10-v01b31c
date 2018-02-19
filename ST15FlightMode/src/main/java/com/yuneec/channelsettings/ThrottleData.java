package com.yuneec.channelsettings;

public class ThrottleData {
    public static final int POINTS_NUM = 5;
    public String cut_sw;
    public int cut_value1;
    public int cut_value2;
    public boolean expo;
    public long id;
    public String sw;
    public ThrCurve[] thrCurve = new ThrCurve[3];

    public class ThrCurve {
        public float[] curvePoints = new float[5];
        public long id;
        public int sw_state;
    }

    public ThrottleData() {
        this.thrCurve[0] = new ThrCurve();
        this.thrCurve[1] = new ThrCurve();
        this.thrCurve[2] = new ThrCurve();
    }
}

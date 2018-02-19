package com.yuneec.channelsettings;

public class DRData {
    public CurveParams[] curveparams = new CurveParams[3];
    public String func;
    public long id;
    public String sw;

    public class CurveParams {
        public float expo1;
        public float expo2;
        public long id;
        public float offset;
        public float rate1;
        public float rate2;
        public int sw_state;
    }

    public DRData() {
        this.curveparams[0] = new CurveParams();
        this.curveparams[1] = new CurveParams();
        this.curveparams[2] = new CurveParams();
    }
}

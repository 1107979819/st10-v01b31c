package com.yuneec.database;

public class ModelBrief {
    public long _id;
    public int analog_min;
    public int f_mode_key;
    public int fpv;
    public int iconResourceId;
    public String name;
    public int switch_min;
    public int type;

    public String toString() {
        return "(" + this._id + "," + this.name + "," + this.iconResourceId + "," + this.type + "," + this.fpv + "," + this.f_mode_key + ")";
    }
}

package com.yuneec.IPCameraManager;

public class Cameras {
    private int id;
    private String p_codep;
    private String p_codev;
    private String p_dr;
    private String p_f;
    private String p_intervalp;
    private String p_intervalv;
    private String p_n;
    private String p_name;
    private String p_t1;
    private String p_t2;
    private String p_type;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.p_name;
    }

    public void setName(String name) {
        this.p_name = name;
    }

    public String getType() {
        return this.p_type;
    }

    public void setType(String type) {
        this.p_type = type;
    }

    public String getDr() {
        return this.p_dr;
    }

    public void setDr(String dr) {
        this.p_dr = dr;
    }

    public String getF() {
        return this.p_f;
    }

    public void setF(String f) {
        this.p_f = f;
    }

    public String getN() {
        return this.p_n;
    }

    public void setN(String n) {
        this.p_n = n;
    }

    public String getT1() {
        return this.p_t1;
    }

    public void setT1(String t1) {
        this.p_t1 = t1;
    }

    public String getT2() {
        return this.p_t2;
    }

    public void setT2(String t2) {
        this.p_t2 = t2;
    }

    public String getIntervalp() {
        return this.p_intervalp;
    }

    public void setIntervalp(String intervalp) {
        this.p_intervalp = intervalp;
    }

    public String getCodep() {
        return this.p_codep;
    }

    public void setCodep(String codep) {
        this.p_codep = codep;
    }

    public String getIntervalv() {
        return this.p_intervalv;
    }

    public void setIntervalv(String intervalv) {
        this.p_intervalv = intervalv;
    }

    public String getCodev() {
        return this.p_codev;
    }

    public void setCodev(String codev) {
        this.p_codev = codev;
    }
}

package com.yuneec.flight_settings;

public class WifiCipherType {
    public static final int PROTO_ESS = 2;
    public static final int PROTO_WPS = 2;
    public static final int WIFICIPHER_INVALID = 3;
    public static final int WIFICIPHER_NOPASS = 2;
    public static final int WIFICIPHER_WEP = 0;
    public static final int WIFICIPHER_WPA = 1;
    private String capabilities;
    private int connectType;
    private int groupCipher;
    private int keyMgmt;
    private int pairwiseCiphers;
    private int protocol;

    public WifiCipherType(String capabilities) {
        this.capabilities = capabilities;
        resolve();
    }

    public int getGroupCipher() {
        return this.groupCipher;
    }

    public int getKeyMgmt() {
        return this.keyMgmt;
    }

    public int getProtocol() {
        return this.protocol;
    }

    public int getPairwiseCiphers() {
        return this.pairwiseCiphers;
    }

    public void resolve() {
        if (!this.capabilities.equals("")) {
            String tmp2;
            String tmp1 = this.capabilities.split("]")[0];
            if (tmp1.contains("[")) {
                tmp2 = tmp1.substring(1, tmp1.length());
            } else {
                tmp2 = tmp1;
            }
            String[] str = tmp2.split("-");
            for (String println : str) {
                System.out.println(println);
            }
            if (str.length >= 3) {
                if (str[2].contains("WEP40")) {
                    this.groupCipher = 0;
                } else if (str[2].contains("WEP104")) {
                    this.groupCipher = 1;
                } else if (str[2].contains("TKIP")) {
                    this.groupCipher = 2;
                    this.keyMgmt = 1;
                } else if (str[2].contains("CCMP")) {
                    this.groupCipher = 3;
                    this.keyMgmt = 2;
                } else if (str[2].contains("NONE")) {
                    this.keyMgmt = 0;
                }
            }
            if (str.length >= 2) {
                if (str[1].contains("PSK")) {
                    this.keyMgmt = 1;
                } else if (str[1].contains("EAP")) {
                    this.keyMgmt = 2;
                } else if (str[1].contains("IEEE8021X")) {
                    this.keyMgmt = 3;
                } else {
                    this.keyMgmt = 0;
                }
            }
            if (str.length >= 1) {
                if (str[0].contains("RSN")) {
                    this.keyMgmt = 1;
                    this.connectType = 1;
                } else if (str[0].contains("WPS")) {
                    this.keyMgmt = 2;
                    this.connectType = 2;
                } else {
                    this.keyMgmt = 0;
                    this.connectType = 1;
                }
            }
            if (str.length >= 0 && str[0].contains("ESS")) {
                this.keyMgmt = 2;
                this.connectType = 2;
            }
        }
    }

    public int getType() {
        return this.connectType;
    }
}

package com.yuneec.flight_settings;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiConnect {
    WifiManager wifiManager;

    public WifiConnect(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    public boolean Connect(String SSID, String Password, int type) {
        if (!this.wifiManager.isWifiEnabled()) {
            return false;
        }
        WifiConfiguration wifiConfig = CreateWifiInfo(SSID, Password, type);
        Log.v("SIFI", " -- " + wifiConfig);
        if (wifiConfig == null) {
            return false;
        }
        WifiConfiguration tempConfig = IsExsits(SSID);
        if (tempConfig != null) {
            this.wifiManager.removeNetwork(tempConfig.networkId);
        }
        return this.wifiManager.enableNetwork(this.wifiManager.addNetwork(wifiConfig), false);
    }

    private WifiConfiguration IsExsits(String SSID) {
        for (WifiConfiguration existingConfig : this.wifiManager.getConfiguredNetworks()) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    public WifiConfiguration CreateWifiInfo(String SSID, String Password, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (type == 2) {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(0);
            config.wepTxKeyIndex = 0;
        }
        if (type == 0) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(1);
            config.allowedGroupCiphers.set(3);
            config.allowedGroupCiphers.set(2);
            config.allowedGroupCiphers.set(0);
            config.allowedGroupCiphers.set(1);
            config.allowedKeyManagement.set(0);
            config.wepTxKeyIndex = 0;
        }
        if (type == 1) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(0);
            config.allowedGroupCiphers.set(3);
            config.allowedGroupCiphers.set(2);
            config.allowedGroupCiphers.set(0);
            config.allowedGroupCiphers.set(1);
            config.allowedKeyManagement.set(1);
            config.allowedKeyManagement.set(2);
            config.allowedPairwiseCiphers.set(2);
            config.allowedPairwiseCiphers.set(1);
            config.allowedProtocols.set(1);
            config.allowedProtocols.set(0);
            config.status = 2;
        }
        return config;
    }
}

package com.yuneec.flight_settings;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import java.util.ArrayList;
import java.util.List;

public class BindWifiManage {
    private List<ScanResult> scanResultList;
    private List<WifiConfiguration> wifiConfigList;
    private WifiInfo wifiInfo;
    private WifiLock wifiLock;
    private WifiManager wifiManager;

    public BindWifiManage(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
        this.wifiInfo = wifiManager.getConnectionInfo();
    }

    public boolean getWifiStatus() {
        return this.wifiManager.isWifiEnabled();
    }

    public boolean openWifi() {
        if (this.wifiManager.isWifiEnabled()) {
            return false;
        }
        return this.wifiManager.setWifiEnabled(true);
    }

    public boolean closeWifi() {
        if (this.wifiManager.isWifiEnabled()) {
            return this.wifiManager.setWifiEnabled(false);
        }
        return true;
    }

    public void lockWifi() {
        this.wifiLock.acquire();
    }

    public void unLockWifi() {
        if (!this.wifiLock.isHeld()) {
            this.wifiLock.release();
        }
    }

    public void createWifiLock() {
        this.wifiLock = this.wifiManager.createWifiLock("fly");
    }

    public void connectConfiguration(int index) {
        if (index <= this.wifiConfigList.size()) {
            this.wifiManager.enableNetwork(((WifiConfiguration) this.wifiConfigList.get(index)).networkId, true);
        }
    }

    public void startScan() {
        this.wifiManager.startScan();
        this.scanResultList = this.wifiManager.getScanResults();
        this.wifiConfigList = this.wifiManager.getConfiguredNetworks();
    }

    public List<ScanResult> getWifiList() {
        return this.scanResultList;
    }

    public List<WifiConfiguration> getWifiConfigList() {
        return this.wifiConfigList;
    }

    public StringBuilder lookUpscan() {
        StringBuilder scanBuilder = new StringBuilder();
        for (int i = 0; i < this.scanResultList.size(); i++) {
            scanBuilder.append("编号：" + (i + 1));
            scanBuilder.append(((ScanResult) this.scanResultList.get(i)).toString());
            scanBuilder.append("\n");
        }
        return scanBuilder;
    }

    public int getLevel(int NetId) {
        return ((ScanResult) this.scanResultList.get(NetId)).level;
    }

    public String getMac() {
        return this.wifiInfo == null ? "" : this.wifiInfo.getMacAddress();
    }

    public String getBSSID() {
        return this.wifiInfo == null ? null : this.wifiInfo.getBSSID();
    }

    public String getSSID() {
        return this.wifiInfo == null ? null : this.wifiInfo.getSSID();
    }

    public int getCurrentNetId() {
        return (this.wifiInfo == null ? null : Integer.valueOf(this.wifiInfo.getNetworkId())).intValue();
    }

    public String getwifiInfo() {
        return this.wifiInfo == null ? null : this.wifiInfo.toString();
    }

    public int getIP() {
        return (this.wifiInfo == null ? null : Integer.valueOf(this.wifiInfo.getIpAddress())).intValue();
    }

    public boolean addNetWordLink(WifiConfiguration config) {
        return this.wifiManager.enableNetwork(this.wifiManager.addNetwork(config), true);
    }

    public boolean disableNetWordLick(int NetId) {
        this.wifiManager.disableNetwork(NetId);
        return this.wifiManager.disconnect();
    }

    public boolean removeNetworkLink(int NetId) {
        return this.wifiManager.removeNetwork(NetId);
    }

    public void hiddenSSID(int NetId) {
        ((WifiConfiguration) this.wifiConfigList.get(NetId)).hiddenSSID = true;
    }

    public void displaySSID(int NetId) {
        ((WifiConfiguration) this.wifiConfigList.get(NetId)).hiddenSSID = false;
    }

    public ArrayList<String> getSSIDList() {
        ArrayList<String> mSSIDList = new ArrayList();
        if (this.scanResultList != null) {
            for (ScanResult scanResult : this.scanResultList) {
                mSSIDList.add(scanResult.SSID);
            }
        }
        return mSSIDList;
    }
}

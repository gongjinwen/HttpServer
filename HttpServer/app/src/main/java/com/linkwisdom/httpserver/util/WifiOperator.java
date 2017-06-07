package com.linkwisdom.httpserver.util;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.text.TextUtils;
import android.util.Log;

import com.linkwisdom.httpserver.MyApplication;

import java.util.ArrayList;
import java.util.List;

public class WifiOperator {
    private final String TAG = "WifiOperator";

    public static final int WIFI_CIPHER_NOPASS = 1;
    public static final int WIFI_CIPHER_WEP = 2;
    public static final int WIFI_CIPHER_WPA = 3;

    private static Context context;
    private static WifiOperator wifiOperator;
    private WifiManager wifiManager;
    private WifiLock wifiLock;

    private static List<ScanResult> scanResultList = new ArrayList<>();//存储扫描到的WiFi网络
    private static List<WifiConfiguration> wifiConfigurationList = new ArrayList<>();//存储系统配置好的WiFi网络

    private WifiOperator() {
        if (context != null) {
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }
    }

    public static WifiOperator getInstance() {
        if(context == null) {
            context = MyApplication.getContext();
        }

        if (wifiOperator == null && context != null) {
            wifiOperator = new WifiOperator();
        }

        if(scanResultList == null){
            scanResultList = new ArrayList<>();
        }
        if(wifiConfigurationList == null) {
            wifiConfigurationList = new ArrayList<>();
        }
        return wifiOperator;
    }


    /**
     * 查看WIFI当前是否处于打开状态
     *
     * @return true 处于打开状态；false 处于非打开状态(包括UnKnow状态)。
     */
    public boolean isWifiClosed() {
        int wifiState = getWifiState();
        if (wifiState == WifiManager.WIFI_STATE_DISABLED || wifiState == WifiManager.WIFI_STATE_DISABLING) {
            return true;
        }
        return false;
    }

    /**
     * 查看WIFI当前是否处于关闭状态
     *
     * @return true 处于关闭状态；false 处于非关闭状态(包括UNKNOW状态)
     */
    public boolean isWifiOpened() {
        int wifiState = getWifiState();
        if (wifiState == WifiManager.WIFI_STATE_ENABLED || wifiState == WifiManager.WIFI_STATE_ENABLING) {
            return true;
        }
        return false;
    }

    /**
     * 如果WIFI当前处于关闭状态，则打开WIFI
     */
    public void openWifi() {
        if (wifiManager != null && isWifiClosed()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 如果WIFI当前处于打开状态，则关闭WIFI
     */
    public void closeWifi() {
        if (wifiManager != null && isWifiOpened()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 获取当前Wifi的状态编码
     *
     * @return WifiManager.WIFI_STATE_ENABLED，WifiManager.WIFI_STATE_ENABLING，
     *         WifiManager.WIFI_STATE_DISABLED，WifiManager.WIFI_STATE_DISABLING，
     *         WifiManager.WIFI_STATE_UnKnow 中间的一个
     */
    public int getWifiState() {
        if (wifiManager != null) {
            return wifiManager.getWifiState();
        }
        return 0;
    }

    /**
     * 获取已经配置好的Wifi网络
     *
     * @return
     */
    public List<WifiConfiguration> getSavedWifiConfiguration() {
        return wifiConfigurationList;
    }

    /**
     * 获取扫描到的网络的信息
     *
     * @return
     */
    public List<ScanResult> getWifiScanResult() {
        return scanResultList;
    }

    /**
     * 执行一次Wifi的扫描
     */
    public synchronized void startScan() {
        if (wifiManager != null) {
            wifiManager.startScan();
//            scanResultList = wifiManager.getScanResults();
//            wifiConfigurationList = wifiManager.getConfiguredNetworks();

            List<ScanResult> scanList = wifiManager.getScanResults();
            Log.i(TAG, "//////== httpserver : startScan  wifiList:" + scanList.size());
            if(scanList != null && scanList.size() > 0){
                scanResultList.clear();
                for (ScanResult scan: scanList){
                    scanResultList.add(scan);
                }
            }

            List<WifiConfiguration> mWifiList = wifiManager.getConfiguredNetworks();
            if(mWifiList!=null && mWifiList.size()>0){
                wifiConfigurationList.clear();
                for(WifiConfiguration wific: mWifiList){
                    wifiConfigurationList.add(wific);
                }
            }
        }
    }

    /**
     * @param wifiList
     */
    public void setScanWifiList(ArrayList<ScanResult> wifiList) {
        if(wifiList != null && scanResultList != null) {
            scanResultList.clear();
            for (ScanResult scan: wifiList){
                scanResultList.add(scan);
            }
        }
    }

    /**
     * 通过netWorkId来连接一个已经保存好的Wifi网络
     *
     * @param netWorkId
     */
    public boolean connetionConfiguration(int netWorkId) {
        if (wifiManager != null && configurationNetWorkIdCheck(netWorkId)) {
            return wifiManager.enableNetwork(netWorkId, true);
        }
        return false;
    }

    /**
     * 断开一个指定ID的网络
     */
    public void disconnectionConfiguration(int netWorkId) {
        wifiManager.disableNetwork(netWorkId);
        wifiManager.disconnect();
    }

    /**
     * 检测尝试连接某个网络时，查看该网络是否已经在保存的队列中间
     *
     * @param netWorkId
     * @return
     */
    private boolean configurationNetWorkIdCheck(int netWorkId) {
        for (WifiConfiguration temp : wifiConfigurationList) {
            if (temp.networkId == netWorkId) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取Wifi的数据
     *
     * @return
     */
    public WifiInfo getWifiConnectionInfo() {
        if(wifiManager != null) {
            return wifiManager.getConnectionInfo();
        }
        return null;
    }

    /**
     * 锁定WIFI，使得在熄屏状态下，仍然可以使用WIFI
     */
    public void acquireWifiLock() {
        if (wifiLock != null) {
            wifiLock.acquire();
        }
    }

    /**
     * 解锁WIFI
     */
    public void releaseWifiLock() {
        if (wifiLock != null) {
            if (wifiLock.isHeld()) {
                wifiLock.release();
            }
        }
    }

    /**
     * 创建一个WifiLock
     */
    public void createWifiLock() {
        if (wifiManager != null) {
            wifiLock = wifiManager.createWifiLock("wifiLock");
        }
    }

    /**
     * 保存一个新的网络
     *
     * @param _wifiConfiguration
     */
    public int addNetWork(WifiConfiguration _wifiConfiguration) {
        int netWorkId = -1;
        if (_wifiConfiguration != null && wifiManager != null) {
            netWorkId = wifiManager.addNetwork(_wifiConfiguration);
            Log.i(TAG, "//////== httpserver : addNetWork netWorkId：" + netWorkId);
            startScan();
        }
        return netWorkId;
    }

    /**
     * 保存并连接到一个新的网络
     *
     * @param _wifiConfiguration
     */
    public int addNetWorkAndConnect(WifiConfiguration _wifiConfiguration) {
        int netWorkId = addNetWork(_wifiConfiguration);
        if (netWorkId != -1) {
            wifiConfigurationList.add(_wifiConfiguration);
            boolean boo = connetionConfiguration(netWorkId);
            if(!boo) {
                netWorkId = -1;
            }
        }
        return netWorkId;
    }

    /**
     * 获取当前连接状态中的Wifi的信号强度
     *
     * @return
     */
    public int getConnectedWifiLevel() {
        WifiInfo wifiInfo = getWifiConnectionInfo();
        if (wifiInfo != null) {
            String connectedWifiSSID = wifiInfo.getSSID();
            if (scanResultList != null) {
                for (ScanResult temp : scanResultList) {
                    if (temp.SSID.replace("\"", "").equals(connectedWifiSSID.replace("\"", ""))) {
                        return temp.level;
                    }
                }
            }
        }
        return 1;
    }

    /**
     * 删除一个已经保存的网络
     *
     * @param netWorkId
     */
    public void remoteNetWork(int netWorkId) {
        if (wifiManager != null) {
            wifiManager.removeNetwork(netWorkId);
        }
    }


    /**
     * 连接一个WIFI
     *
     * @param ssid
     * @param password
     * @param wifiCipherType
     */
    public int addNetWorkAndConnect(String ssid, String password, int wifiCipherType) {
        if (wifiManager != null) {
            Log.i(TAG, "//////== httpserver : addNetWorkAndConnect 00000000000");
            WifiConfiguration wifiConfig = createWifiConfiguration(ssid, password, wifiCipherType);
            WifiConfiguration temp = isWifiConfigurationSaved(wifiConfig);
            if (temp != null) {
                Log.i(TAG, "//////== httpserver : addNetWorkAndConnect 11111111111111");
                wifiManager.removeNetwork(temp.networkId);
            }
            int id = addNetWorkAndConnect(wifiConfig);
            Log.i(TAG, "//////== httpserver : addNetWorkAndConnect 222222222222  :" + id);
            return id;
        }
        return -1;
    }

    private WifiConfiguration isWifiConfigurationSaved(WifiConfiguration wifiConfig) {
        if (wifiConfigurationList == null) {
            this.startScan();
        }
        for (WifiConfiguration temp : wifiConfigurationList) {
            if(temp.SSID == null){
                Log.i(TAG, "//////== httpserver : temp.SSID is null");
                continue;
            }
            if (temp.SSID.equals(wifiConfig.SSID)) {
                return temp;
            }
        }
        return null;
    }

    public WifiConfiguration createWifiConfiguration(String SSID, String Password, int Type) {
        WifiConfiguration configuration = new WifiConfiguration();
        configuration.allowedAuthAlgorithms.clear();
        configuration.allowedGroupCiphers.clear();
        configuration.allowedKeyManagement.clear();
        configuration.allowedPairwiseCiphers.clear();
        configuration.allowedProtocols.clear();
        configuration.SSID = "\"" + SSID + "\"";

        if (Type == WIFI_CIPHER_NOPASS) {
            Log.i(TAG, "//////== httpserver : createWifiConfiguration  NONE");
            configuration.wepKeys[0] = "\"" + "\"";
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            configuration.wepTxKeyIndex = 0;
        } else  if (Type == WIFI_CIPHER_WEP) {
            Log.i(TAG, "//////== httpserver : createWifiConfiguration  WEP");
            configuration.hiddenSSID = true;
            configuration.wepKeys[0] = "\"" + Password + "\"";
            configuration.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            configuration.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            configuration.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);
            configuration.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP40);
            configuration.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            configuration.allowedKeyManagement
                    .set(WifiConfiguration.KeyMgmt.NONE);
            configuration.wepTxKeyIndex = 0;
        } else if (Type == WIFI_CIPHER_WPA) {
            Log.i(TAG, "//////== httpserver : createWifiConfiguration  WPA");
            configuration.preSharedKey = "\"" + Password + "\"";
            configuration.hiddenSSID = true;
            configuration.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            configuration.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.TKIP);
            configuration.allowedKeyManagement
                    .set(WifiConfiguration.KeyMgmt.WPA_PSK);
            configuration.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            configuration.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.CCMP);
            configuration.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            configuration.status = WifiConfiguration.Status.ENABLED;
        }
        return configuration;
    }

    /**
     * 获取wifi加密方式
     * @param ssid
     * @return
     */
    public int getCipherType(String ssid) {
        List<ScanResult> list = getWifiScanResult();
        for (ScanResult scResult : list) {
            if (!TextUtils.isEmpty(scResult.SSID) && scResult.SSID.equals(ssid)) {
                String capabilities = scResult.capabilities;
                Log.i(TAG, "//////== httpserver : getCipherType  SSID:" + ssid + "  " + capabilities);
                return getCipherTypeTo(capabilities);
            }
        }
        return WIFI_CIPHER_WPA;
    }

    public int getCipherTypeTo(String capabilities){
        int cipherType = WIFI_CIPHER_WPA;
        if (!TextUtils.isEmpty(capabilities)) {
            if (capabilities.contains("WPA") || capabilities.contains("wpa")) {
                cipherType = WIFI_CIPHER_WPA;
            } else if (capabilities.contains("WEP") || capabilities.contains("wep")) {
                cipherType = WIFI_CIPHER_WEP;
            } else if (capabilities.contains("ESS") || capabilities.equals("")) {
                cipherType = WIFI_CIPHER_NOPASS;
            }
        }
        return cipherType;
    }

}

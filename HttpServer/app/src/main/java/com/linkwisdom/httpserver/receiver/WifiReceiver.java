package com.linkwisdom.httpserver.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.linkwisdom.httpserver.DialogActivity;
import com.linkwisdom.httpserver.handler.ConnectHandler;
import com.linkwisdom.httpserver.service.HttpService;
import com.linkwisdom.httpserver.util.Utility;

public class WifiReceiver extends BroadcastReceiver {

    private static String TAG = "WifiReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if(intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)){
            //signal strength changed
        } else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){//wifi连接上与否
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//            Log.i(TAG, "//////== httpserver : WifiReceiver  网络状态改变 ：" + info.getState());
            if(info.getState().equals(NetworkInfo.State.DISCONNECTED)){
                Log.i(TAG, "//////== httpserver : WifiReceiver  wifi网络连接断开 ");
            } else if (info.getState().equals(NetworkInfo.State.CONNECTED)){
                WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID();

                //获取当前wifi名称
                Log.i(TAG, "//////== httpserver : WifiReceiver 连接到网络: " + ssid);

//                context.startService(new Intent(context, HttpService.class));
            }

        } else if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){//wifi打开与否
            int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);

            if(wifistate == WifiManager.WIFI_STATE_DISABLED){
                System.out.println("系统关闭wifi");
                Log.i(TAG, "//////== httpserver : WifiReceiver 系统关闭wifi" );
            }
            else if(wifistate == WifiManager.WIFI_STATE_ENABLED){
                System.out.println("系统开启wifi");
                Log.i(TAG, "//////== httpserver : WifiReceiver 系统已开启wifi" );
//                context.startService(new Intent(context, HttpService.class));
            }
        } else if("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction())){
            int state = intent.getIntExtra("wifi_state",  0);
            //便携式热点的状态为：10-正在关闭；11-已关闭；12-正在开启；13-已开启
            Log.i(TAG, "//////== httpserver : WifiReceiver 热点状态：" + state);
            if(state == 13){
                context.startService(new Intent(context, HttpService.class));
            }
        }
    }

}

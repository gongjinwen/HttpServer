package com.linkwisdom.httpserver.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.util.Log;

import com.linkwisdom.httpserver.util.WifiOperator;

import java.util.ArrayList;

public class OpenWifiListReceiver extends BroadcastReceiver {
    private final String TAG = "OpenWifiListReceiver";
    private final String OPEN_ACTION = "A9.android.action.open.wifi_list";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(OPEN_ACTION.equals(action)) {
            Log.i(TAG, "//////== httpserver : 收到广播：" + action);
            Bundle build = intent.getBundleExtra("openAp");
            ArrayList<ScanResult> wifiList = build.getParcelableArrayList("wifi");

            if(wifiList != null) {
                Log.i(TAG, "//////== httpserver : 获取到wifi列表：" + wifiList.size());
                WifiOperator.getInstance().setScanWifiList(wifiList);
            } else {
                Log.i(TAG, "//////== httpserver : wifi is null ");
            }
        }
    }

}

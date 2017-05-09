package com.linkwisdom.httpserver.handler;


import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.util.Log;

import com.google.gson.Gson;
import com.linkwisdom.httpserver.DialogActivity;
import com.linkwisdom.httpserver.util.WiFiInfoBean;
import com.linkwisdom.httpserver.util.WifiOperator;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GetWifiInfoHandler implements HttpRequestHandler {

    private final String TAG = "GetWifiInfoHandler";
    private Context context = null;

    public GetWifiInfoHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response,
                       HttpContext httpContext) throws HttpException, IOException {

        Log.i(TAG, "//////== httpserver : GetWifiInfoHandler. 收到获取WiFi信息的请求！");

        Intent intentAct = new Intent();
        intentAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentAct.setClass(context, DialogActivity.class);
        context.startActivity(intentAct);

        String json = getWifiInfoToJson();
        StringEntity entity = new StringEntity(json);
        response.setEntity(entity);
        response.setStatusCode(HttpStatus.SC_OK);
    }

    private String getWifiInfoToJson() {
        List<ScanResult> list = WifiOperator.getInstance().getWifiScanResult();
        Log.i(TAG, "//////== httpserver : getWifiInfoHandler :" + list.size());
        List<WiFiInfoBean> wifiInfo = new ArrayList<>();
        for(ScanResult scanResult : list) {
            String ssid = scanResult.SSID;
            if(ssid == null || ssid.length() < 1){
                continue;
            }
            WiFiInfoBean info = new WiFiInfoBean();
            info.setSsid_name(ssid);
            int type = WifiOperator.getInstance().getCipherTypeTo(scanResult.capabilities);
            info.setSsid_type(type);
            wifiInfo.add(info);
//            Log.i(TAG, "//////== httpserver : GetWifiInfoHandler:" + scanResult.SSID + "/" + type);
        }
        Gson gson = new Gson();
        String json = gson.toJson(wifiInfo);

        if(json != null){
            Log.i(TAG, "//////== httpserver : GetWifiInfoHandler. ： \n" + json.toString());
        }

        return json.toString();
    }

}

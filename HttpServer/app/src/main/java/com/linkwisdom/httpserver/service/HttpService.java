package com.linkwisdom.httpserver.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import com.linkwisdom.httpserver.DialogActivity;
import com.linkwisdom.httpserver.MainActivity;
import com.linkwisdom.httpserver.R;
import com.linkwisdom.httpserver.server.WebServer;
import com.linkwisdom.httpserver.util.Utility;
import com.linkwisdom.httpserver.util.WifiOperator;

public class HttpService extends Service {
    private static String TAG = "HttpService";
    private static final int NOTIFICATION_STARTED_ID = 1100;

    private WebServer webServer = null;

    public HttpService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "//////== httpserver : HttpService onCreate");
        webServer = new WebServer(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(webServer == null){
            webServer = new WebServer(this);
        }
        if (webServer != null) {
            webServer.startThread();
        }
        Log.i(TAG, "//////== httpserver : HttpService onStartCommand");
        // 构建Notification
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class);

        String url = "http://" + Utility.getIp(this) + ":" + WebServer.serverPort;

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                .setContentTitle("HttpServer")// 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText(url)// 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        Notification notification = builder.build(); // 获取构建好的Notification
//        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音

        startForeground(NOTIFICATION_STARTED_ID, notification);// 开始前台服务

        WifiOperator.setContext(this);
        WifiOperator.getInstance().startScan();//扫描wifi

        //弹出对话框，扫描获取WiFi列表
        Intent intentAct = new Intent();
        intentAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intentAct.setClass(getApplicationContext(), DialogActivity.class);
        startActivity(intentAct);

//        android.provider.Settings.System.putString(getContentResolver(), android.provider.Settings.System.WIFI_USE_STATIC_IP, "0");
//        android.provider.Settings.System.putString(getContentResolver(), android.provider.Settings.System.WIFI_STATIC_DNS1, "192.168.0.2");
//        android.provider.Settings.System.putString(getContentResolver(), android.provider.Settings.System.WIFI_STATIC_DNS2, "192.168.0.3");
//        android.provider.Settings.System.putString(getContentResolver(), android.provider.Settings.System.WIFI_STATIC_GATEWAY, "192.168.0.1");
//        android.provider.Settings.System.putString(getContentResolver(), android.provider.Settings.System.WIFI_STATIC_NETMASK, "255.255.255.0");
//        android.provider.Settings.System.putString(getContentResolver(), android.provider.Settings.System.WIFI_STATIC_IP, "1");

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "//////== httpserver : HttpService onDestroy");
        if(webServer != null) {
            webServer.stopThread();
        }
        stopForeground(true);// 停止前台服务--参数：表示是否移除之前的通知
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

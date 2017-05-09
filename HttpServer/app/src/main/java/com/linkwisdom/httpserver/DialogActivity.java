package com.linkwisdom.httpserver;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.linkwisdom.httpserver.util.WifiOperator;

/**
 * 扫描获取WiFi列表需要在前台，所以弹出对话框进行WiFi扫描
 * 该对话框为全透明
 */
public class DialogActivity extends Activity {
    private final String TAG = "DialogActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.activity_dialog);

        WifiOperator.setContext(DialogActivity.this);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 100){
                    DialogActivity.this.finish();
                }
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                WifiOperator.getInstance().startScan();//扫描wifi
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                WifiOperator.getInstance().startScan();//扫描wifi
                int wifiListSize = WifiOperator.getInstance().getWifiScanResult().size();
                Log.i(TAG, "//////== httpserver : DialogActivity wifiListSize:" + wifiListSize);
                handler.sendEmptyMessage(100);
            }
        }).start();

    }
}

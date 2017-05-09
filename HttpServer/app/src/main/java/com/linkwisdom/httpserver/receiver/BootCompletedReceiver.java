package com.linkwisdom.httpserver.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.linkwisdom.httpserver.service.HttpService;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        context.startService(new Intent(context, HttpService.class));
    }
}

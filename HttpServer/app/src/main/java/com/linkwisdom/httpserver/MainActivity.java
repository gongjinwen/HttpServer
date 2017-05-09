package com.linkwisdom.httpserver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.linkwisdom.httpserver.server.WebServer;
import com.linkwisdom.httpserver.service.HttpService;
import com.linkwisdom.httpserver.util.Utility;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private String TAG = "MainActivity";
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.switchButton).setOnClickListener(this);
        textView = (TextView)findViewById(R.id.textView);

        setInfoText();

        startService(new Intent(this, HttpService.class));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.switchButton:
                Intent intent = new Intent(this, HttpService.class);
                startService(intent);
                setInfoText();
                break;
        }
    }


    private void setInfoText() {

        String text = "服务已开启"
                + "\nhttp://"
                + Utility.getIp(this) //Utility.getLocalIpAddress()
                + ":"
                + WebServer.serverPort
                + "\n"
                + Utility.getLocalIpAddress(this)
                + "\n"
                + Utility.getLocalIpAddress2();

        textView.setText(text);
    }

}

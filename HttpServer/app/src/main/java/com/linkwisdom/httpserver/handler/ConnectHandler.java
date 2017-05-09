package com.linkwisdom.httpserver.handler;


import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.linkwisdom.httpserver.server.WebServer;
import com.linkwisdom.httpserver.util.Utility;
import com.linkwisdom.httpserver.util.WifiOperator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.List;

public class ConnectHandler implements HttpRequestHandler {
    private final String TAG = "ConnectHandler";
    private Context context = null;

    static final String CONNECT_ACTION = "android.intent.action.BOOT_COMPLETED_CONNECT";

    private String name=null, ssid = null, password=null, type = null;

    public ConnectHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response,
                       HttpContext httpContext) throws HttpException, IOException {

        Log.i(TAG, "//////== httpserver : ConnectHandler. 收到连接请求");

        String uriString = request.getRequestLine().getUri();
        Uri uri = Uri.parse(uriString);

        try {
            name = URLDecoder.decode(uri.getQueryParameter("name"));
            ssid = URLDecoder.decode(uri.getQueryParameter("network"));
            password = URLDecoder.decode(uri.getQueryParameter("password"));
            type = URLDecoder.decode(uri.getQueryParameter("ap_type"));
        } catch (Exception e){
            e.fillInStackTrace();
        }
        Log.i(TAG, "//////== httpserver : ConnectHandler. ssid:" + ssid + "  /" + type);

        if(ssid==null) {
            return;
        }

        String contentType = "text/html";
        HttpEntity entity = new EntityTemplate(new ContentProducer() {
            public void writeTo(final OutputStream outstream)
                    throws IOException {
                OutputStreamWriter writer = new OutputStreamWriter(outstream,
                        "UTF-8");
                String resp = createHTML();
                writer.write(resp);
                writer.flush();
            }
        });

        ((EntityTemplate) entity).setContentType(contentType);

        response.setEntity(entity);
        response.setStatusCode(HttpStatus.SC_OK);

        new Thread(new Runnable() {
            @Override
            public void run() {

                //如果当前已连接网线，不执行后续操作
                if(Utility.isEthernetConnected(context)){
                    Log.i(TAG, "//////== httpserver : ConnectHandler 已插入网线：");
                    return;
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //发送广播
                Intent intent = new Intent();
                intent.setAction(CONNECT_ACTION);
                context.sendBroadcast(intent);
                Log.i(TAG, "//////== httpserver : ConnectHandler 发送广播：" + CONNECT_ACTION);

                boolean connect = true;//连接WiFi标志位
                Utility.playHint(context, 0);//播放音频（正在连接）
                int i = 0;
                while (i < 40) {//循环40秒
                    try {
                        Thread.sleep(1000);//每次循环休眠1秒
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    if(connect) {//判断WiFi连接标志
                        boolean isWifiOpened = WifiOperator.getInstance().isWifiOpened();
                        // Log.i(TAG, "//////== httpserver : wifi打开状态：" + isWifiOpened);
                        if (isWifiOpened) {//判断WiFi是否打开
                            connectWifi();//连接wifi
                            connect = false;
                        }
                    }

                    //判断WiFi连接状态
                    if(Utility.getWifiState(context).equals(NetworkInfo.State.CONNECTED)){
                        WifiInfo wifiInfo = WifiOperator.getInstance().getWifiConnectionInfo();
                        Log.i(TAG, "//////== httpserver:ConnectHandler wifi连接成功!!!");
                        if(wifiInfo != null){
                            String cSSID = wifiInfo.getSSID();//获取到的已连接的SSID
                            String tagSSID = "\"" + ssid + "\"";
                            if(cSSID !=null && cSSID.equals(tagSSID)) {
                                Utility.playHint(context, 1);//播放音频（连接成功）
                                break;
                            }
                        }
                    }

                    if(i == 39) {
                        //循环到最后，表示连接失败
                        Utility.playHint(context, 2);//播放音频（连接失败）
                    }
                    i++;
                }

            }
        }).start();
    }

    private void connectWifi() {
        if(ssid != null || password != null) {
            Log.i(TAG, "//////== httpserver : ConnectHandler. connectWifi：" + ssid);
            WifiOperator.setContext(context);
            //查询WiFi的加密形式
            int wifiCipherType = WifiOperator.getInstance().getCipherType(ssid);

            Log.i(TAG, "//////== httpserver : ConnectHandler   wifiType: "+ wifiCipherType);

            int netId = WifiOperator.getInstance().addNetWorkAndConnect(ssid, password, wifiCipherType);

            Log.i(TAG, "//////== httpserver : ConnectHandler connectWifi netId:" + netId);
        } else {
            Log.i(TAG, "//////== httpserver : ConnectHandler ，ssid or password is null " );
        }
    }

    private String createHTML() {
        StringBuffer body = new StringBuffer();

        echo_html_begin(body);
        echo_head(body);

        body.append("<body>\n");


        body.append("<div id=\"main\" class=\"container\">\n");
        body.append("<div class=\"panel panel-default\">\n");
        body.append("<div class=\"panel-heading\">\n");
        body.append("<h1 id=\"title\" class=\"text-center\">设置成功</h1>\n");
        body.append("</div>\n");

        body.append("<div id=\"connect-notice\" class=\"panel-body\">\n");
        body.append("<p>设置成功，设备正在连接网络 <strong id=\"network-name\">" + ssid +"</strong>...</p>\n");

        body.append("<ul>\n");
        body.append("<li>\n");
        body.append("<p>如果设备语音提示“连接已成功”，请将手机的连接到网络 <strong id=\"network-name\">" + ssid +"</strong></p>\n");
        body.append("</li>\n");

        body.append("<li>\n");
        body.append("<p>如果设备语言提示“连接失败”，请手机的连接到网络：“Domigo XXXONE” <a href=\"setting\">重试</a></p>");
        body.append("</li>\n");
        body.append("</ul>");
        body.append("</div>");
        body.append("</div>");
        body.append("</div>");

        echo_use_js(body);

        body.append("</body>\n");
        body.append("</html>\n");

        return body.toString();
    }

    void echo_html_begin(StringBuffer body) {
        body.append("<!DOCTYPE html>\n");
        body.append("<html lang=\"en\">\n");
        return;
    }

    void echo_head(StringBuffer body) {
        body.append("<head>\n");
        body.append("<meta charset=\"UTF-8\">\n");
        body.append("<title>设置成功</title>\n");
        body.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">\n");
        body.append("<link rel=\"stylesheet\" href=\"/css/bootstrap.min.css\">\n");
        body.append("<link rel=\"stylesheet\" href=\"/css/style.css\">\n");
        body.append("</head>\n");

        return;
    }

    void echo_use_js(StringBuffer body) {
        String js_jquery = "http://" + Utility.getIp(context) + ":" + WebServer.serverPort + WebServer.JS_JQUERY;
        String js_bootstrap = "http://" + Utility.getIp(context) + ":" + WebServer.serverPort + WebServer.JS_BOOTSTRAP;
        String js_main = "http://" + Utility.getIp(context) + ":" + WebServer.serverPort + WebServer.JS_MAIN;

        body.append("<script type=\"text/javascript\" src=\"" + js_jquery + "\"></script>\n");
        body.append("<script type=\"text/javascript\" src=\"" + js_bootstrap + "\"></script>\n");
        body.append("<script type=\"text/javascript\" src=\"" + js_main + "\"></script>\n");

        return;
    }

}

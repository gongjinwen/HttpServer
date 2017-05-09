package com.linkwisdom.httpserver.server;

import android.content.Context;
import android.util.Log;

import com.linkwisdom.httpserver.handler.ConnectHandler;
import com.linkwisdom.httpserver.handler.CssBootstrapHandler;
import com.linkwisdom.httpserver.handler.CssStyleHandler;
import com.linkwisdom.httpserver.handler.GetWifiInfoHandler;
import com.linkwisdom.httpserver.handler.JsBootstrapHandler;
import com.linkwisdom.httpserver.handler.JsJqueryHandler;
import com.linkwisdom.httpserver.handler.JsMainHandler;
import com.linkwisdom.httpserver.handler.SettingsHandler;
import com.linkwisdom.httpserver.util.Utility;

import org.apache.http.HttpException;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class WebServer extends Thread{
    private static final String TAG = "WebServer";
    private static final String SERVER_NAME = "WebServer";

    private static final String ALL_PATTERN = "*";
    private static final String SETTING_URL = "/setting";
    private static final String CONNECT_URL = "/connect";
    private static final String CONNECT_URL_1 = "/cgi-bin/set_ssid";
    private static final String GET_WIFI_LIST_URL = "/get_wifi_list";

    public static final String CSS_STYLE = "/css_style";
    public static final String CSS_BOOTSTRAP = "/css_bootstrap";
    public static final String JS_JQUERY = "/js_jquery";
    public static final String JS_BOOTSTRAP = "/js_bootstrap";
    public static final String JS_MAIN = "/js_main";

    private boolean isRunning = false;
    private Context context = null;
    public static int serverPort = 8080;

    private BasicHttpProcessor httpproc = null;
    private BasicHttpContext httpContext = null;
    private HttpService httpService = null;
    private HttpRequestHandlerRegistry registry = null;

    public WebServer(Context context) {
        super(SERVER_NAME);
        this.setContext(context);

        httpproc = new BasicHttpProcessor();
        httpContext = new BasicHttpContext();

        httpproc.addInterceptor(new ResponseDate());
        httpproc.addInterceptor(new ResponseServer());
        httpproc.addInterceptor(new ResponseContent());
        httpproc.addInterceptor(new ResponseConnControl());

        httpService = new HttpService(httpproc,
                new DefaultConnectionReuseStrategy(),
                new DefaultHttpResponseFactory());

        registry = new HttpRequestHandlerRegistry();

        // 创建HTTP参数
        HttpParams params = new BasicHttpParams();
        params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
                .setParameter(CoreProtocolPNames.ORIGIN_SERVER,"WebServer/1.1");
        // 设置HTTP参数
        httpService.setParams(params);

        //设置界面
        SettingsHandler settingsHandler = new SettingsHandler(context);
        registry.register(ALL_PATTERN, settingsHandler);
        registry.register(SETTING_URL, settingsHandler);

        //连接请求
        ConnectHandler connectHandler = new ConnectHandler(context);
        registry.register(CONNECT_URL, connectHandler);
        registry.register(CONNECT_URL_1, connectHandler);

        //获取WiFi信息请求
        registry.register(GET_WIFI_LIST_URL, new GetWifiInfoHandler(context));

        //css与js
        registry.register(CSS_STYLE, new CssStyleHandler(context));
        registry.register(CSS_BOOTSTRAP, new CssBootstrapHandler(context));
        registry.register(JS_JQUERY, new JsJqueryHandler(context));
        registry.register(JS_BOOTSTRAP, new JsBootstrapHandler(context));
        registry.register(JS_MAIN, new JsMainHandler(context));

        httpService.setHandlerResolver(registry);
    }

    @Override
    public void run() {
        super.run();
        Log.i(TAG, "//////== httpserver : WebServer start url：" + "http://" + Utility.getIp(context) + ":" + WebServer.serverPort);
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);

            serverSocket.setReuseAddress(true);

            while (isRunning) {
                try {
                    final Socket socket = serverSocket.accept();
                    DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();

                    serverConnection.bind(socket, new BasicHttpParams());

                    httpService.handleRequest(serverConnection, httpContext);

                    serverConnection.shutdown();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (HttpException e) {
                    e.printStackTrace();
                }
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void startThread() {
        if (!isRunning) {
            isRunning = true;
            super.start();
        }
    }
    public synchronized void stopThread() {
        isRunning = false;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

}

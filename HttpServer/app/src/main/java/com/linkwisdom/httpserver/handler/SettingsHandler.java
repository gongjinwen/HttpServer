package com.linkwisdom.httpserver.handler;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.util.Log;

import com.linkwisdom.httpserver.DialogActivity;
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
import java.util.List;


public class SettingsHandler implements HttpRequestHandler {
    private final String TAG = "SettingsHandler";
    private Context context = null;

    public SettingsHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response,
                       HttpContext httpContext) throws HttpException, IOException {

        Log.i(TAG, "//////== httpserver : SettingsHandler.");

//        Intent intentAct = new Intent();
//        intentAct.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intentAct.setClass(context, DialogActivity.class);
//        context.startActivity(intentAct);

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
    }


    private String createHTML() {
        StringBuffer body = new StringBuffer();
        //body.append("Content-type:text/html\n\n");
        echo_html_begin(body);

        echo_head(body);
        body.append("<body><div id=\"main\" class=\"container\"><div class=\"panel panel-default\">\n");

        echo_panel_head(body);

        echo_panel_body(body);


        body.append("</div></div>\n");

        echo_use_js(body);

        body.append("</body>\n");
        echo_html_end(body);

        return body.toString();
    }
    private void echo_html_begin(StringBuffer body) {
        body.append("<!DOCTYPE html>\n");
        body.append("<html lang=\"en\">\n");
        return;
    }

    private void echo_head(StringBuffer body) {
        String css_bootstrap = "http://" + Utility.getIp(context) + ":" + WebServer.serverPort + WebServer.CSS_BOOTSTRAP;
        String css_style = "http://" + Utility.getIp(context) + ":" + WebServer.serverPort + WebServer.CSS_STYLE;
        body.append("<head>\n");
        body.append("<meta charset=\"UTF-8\">\n");
        body.append("<title>设备设置</title>\n");
        body.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">\n");
        body.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + css_bootstrap +"\">\n");
        body.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + css_style + "\">\n");
        body.append("</head>\n");

        return;
    }

    private void echo_panel_head(StringBuffer body) {
        body.append("<div class=\"panel-heading\">\n");
        body.append("<h1 id=\"title\" class=\"text-center\">设置</h1>\n");
        body.append("</div>\n");

        return;
    }

    private void echo_panel_body(StringBuffer body) {
        body.append("<div class=\"panel-body\">\n");

        echo_panel_body_form(body);

        body.append("</div>\n");

        return;
    }

    void echo_panel_body_form(StringBuffer body) {
        body.append("<form action=\"/connect\" method=\"get\" role=\"form\">\n");
//        echo_panel_body_form_ssid(body);

        echo_panel_body_form_devname(body);

        echo_panel_body_form_network(body);

        echo_panel_body_form_netpasswd(body);

        body.append("<div class=\"form-group hidden\">\n");
        body.append("<input id=\"ap_type\" name=\"ap_type\" type=\"text\" value=\"1\" class=\"form-control\" >\n");//表单隐藏域（用于区分wifi是否需要密码：0不需要 1需要）
        body.append("</div>\n");

        body.append("<input id=\"connect\" type=\"submit\" value=\"连接\" class=\"btn btn-primary btn-lg btn-block\">\n");

        body.append("</form>\n");
    }

    private void echo_panel_body_form_ssid(StringBuffer body) {
        String ssid_value = "";

        body.append("<div class=\"form-group\">\n");
        body.append("<label class=\"control-label\" for=\"ssid\">SSID:</label>\n");
        body.append("<p class=\"form-control-static\">"+ ssid_value +"</p>\n");
        body.append("</div>\n");

        return;
    }

    private void echo_panel_body_form_devname(StringBuffer body) {
        body.append("<div id=\"device-name-group\" class=\"form-group\">\n");
        body.append("<label class=\"control-label\" for=\"name\">设备名称：</label>\n");
        body.append("<div class=\"input-group\">\n");
        body.append("<input id=\"device-name\" type=\"text\" class=\"form-control\" name=\"name\" placeholder=\"请选择或者输入名称\">\n");
        body.append("<div class=\"input-group-btn\">\n");
        body.append("<button type=\"button\" class=\"btn btn-default dropdown-toggle\" data-toggle=\"dropdown\"><span class=\"caret\"></span></button>\n");
        body.append("<ul class=\"dropdown-menu centerDropdown\">\n");
        body.append("<li><a class=\"name-item\" href=\"#\">客厅</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">卧室</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">书房</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">餐厅</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">办公室</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">客房</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">浴室</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">走廊</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">家庭活动</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">车库</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">花园</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">健身房</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">门厅</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">厨房</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">便携式</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">娱乐室</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">工作室</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">游泳池</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">露台</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">影音房</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">儿童房</a></li>\n");
        body.append("<li><a class=\"name-item\" href=\"#\">其他</a></li>\n");
        body.append("</ul>\n");
        body.append("</div>\n");
        body.append("</div>\n");
        body.append("</div>\n");

        return;

    }

    private void echo_panel_body_form_network(StringBuffer body) {
        body.append("<div id=\"network-group\" class=\"form-group\">\n");
        body.append("<label for=\"network\" class=\"control-label\">需要连接的网络：</label>\n");
        body.append("<select id=\"network\" name=\"network\" class=\"form-control\">\n");
        body.append("<option value=\"\" selected disabled>请选择网络</option>\n");
        get_wifi_list(body);
        body.append("</select>\n");
        body.append("</div>\n");

        return;
    }

    private void get_wifi_list(StringBuffer body) {
        WifiOperator.setContext(context);
        List<ScanResult> list = WifiOperator.getInstance().getWifiScanResult();
        Log.i(TAG, "//////== httpserver : SettingsHandler wifiList :" + list.size());
        for(ScanResult r : list) {
            String id = r.SSID;
            String ssid = r.SSID;
            if(ssid == null || ssid.length() < 1){
                continue;
            }
            int type = WifiOperator.getInstance().getCipherTypeTo(r.capabilities);
            if (type == WifiOperator.WIFI_CIPHER_NOPASS) {
                body.append("<option id=\""+ id +"\" value=\""+ ssid +"\">"+ ssid +"</option>\n");
            } else {
                body.append("<option id=\""+ id +"\" value=\""+ ssid +"\" data-password>"+ ssid +"</option>\n");
            }
        }
    }

    void echo_panel_body_form_netpasswd(StringBuffer body) {
        body.append("<div id=\"password-group\" class=\"form-group hidden\">\n");
        body.append("<label for=\"password\" class=\"control-label\">网络密码：</label>\n");
        body.append("<input id=\"password\" type=\"password\" name=\"password\" class=\"form-control\" placeholder=\"请输入密码\">\n");
        body.append("</div>\n");

        return;
    }

    private void echo_use_js(StringBuffer body) {
        String js_jquery = "http://" + Utility.getIp(context) + ":" + WebServer.serverPort + WebServer.JS_JQUERY;
        String js_bootstrap = "http://" + Utility.getIp(context) + ":" + WebServer.serverPort + WebServer.JS_BOOTSTRAP;
        String js_main = "http://" + Utility.getIp(context) + ":" + WebServer.serverPort + WebServer.JS_MAIN;

        body.append("<script type=\"text/javascript\" src=\"" + js_jquery + "\"></script>\n");
        body.append("<script type=\"text/javascript\" src=\"" + js_bootstrap + "\"></script>\n");
        body.append("<script type=\"text/javascript\" src=\"" + js_main + "\"></script>\n");

        return;
    }

    private void echo_html_end(StringBuffer body) {
        body.append("</html>\n");
        return;
    }

}

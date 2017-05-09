package com.linkwisdom.httpserver.handler;


import android.content.Context;

import com.linkwisdom.httpserver.R;
import com.linkwisdom.httpserver.util.Utility;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class JsJqueryHandler implements HttpRequestHandler {

    private Context context = null;

    public JsJqueryHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response,
                       HttpContext httpContext) throws HttpException, IOException {
        String contentType = "text/javascript";
        HttpEntity entity = new EntityTemplate(new ContentProducer() {
            public void writeTo(final OutputStream outstream)
                    throws IOException {
                OutputStreamWriter writer = new OutputStreamWriter(outstream,
                        "UTF-8");
                String resp = Utility.openHTMLString(context, R.raw.js_jquery_min);
                writer.write(resp);
                writer.flush();
            }
        });

        ((EntityTemplate) entity).setContentType(contentType);

        response.setEntity(entity);
    }

}

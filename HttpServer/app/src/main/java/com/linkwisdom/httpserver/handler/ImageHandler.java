package com.linkwisdom.httpserver.handler;


import android.content.Context;

import com.linkwisdom.httpserver.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageHandler implements HttpRequestHandler {

    private Context context = null;

    public ImageHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response,
                       HttpContext httpContext) throws HttpException, IOException {

        HttpEntity entity = new EntityTemplate(new ContentProducer() {
            public void writeTo(final OutputStream outstream)
                    throws IOException {
                InputStream input = context.getResources().openRawResource(R.raw.eg_tulip);
                int ch;
                while ((ch = input.read()) != -1) {
                    outstream.write(ch);
                }
                outstream.flush();
            }
        });

        String contentType = "file";
        ((EntityTemplate) entity).setContentType(contentType);

        response.setEntity(entity);
    }

}

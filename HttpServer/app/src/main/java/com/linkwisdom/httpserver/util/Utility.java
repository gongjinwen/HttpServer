package com.linkwisdom.httpserver.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.linkwisdom.httpserver.R;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


public class Utility {
    private static String TAG = "Utility";

    /**
     * 获取IP地址
     * @param context
     * @return
     */
    public static String getIp(Context context) {
        String ip = getLocalIpAddress(context);
        if(ip == null || ip.length() < 1) {
            ip = getLocalIpAddress2();
        }
        return ip;
    }


    public static String getLocalIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        if (ipAddress == 0)
            return null;
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }

    public static String getLocalIpAddress2() {
        try{
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while(en.hasMoreElements()){
                NetworkInterface nif = en.nextElement();
                Enumeration<InetAddress> enumIpAddr = nif.getInetAddresses();
                while(enumIpAddr.hasMoreElements()){
                    InetAddress mInetAddress = enumIpAddr.nextElement();
                    if(!mInetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(mInetAddress.getHostAddress())){
                        return mInetAddress.getHostAddress().toString();
                    }
                }
            }
        }catch(SocketException ex){
            Log.e("MyFeiGeActivity", "获取本地IP地址失败");
        }

        return null;
    }

    public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the Reader.read(char[]
		 * buffer) method. We iterate until the Reader return -1 which means
		 * there's no more data to read. We use the StringWriter class to
		 * produce the string.
		 */
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is,
                        "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return writer.toString();
        } else {
            return "";
        }
    }

    public static String openHTMLString(Context context, int id) {
        InputStream is = context.getResources().openRawResource(id);

        return Utility.convertStreamToString(is);
    }

    /**
     *  从assets目录中复制整个文件夹内容
     *  @param  context  Context 使用CopyFiles类的Activity
     *  @param  oldPath  String  原文件路径  如：/aa
     *  @param  newPath  String  复制后路径  如：xx:/bb/cc
     */
    public static void copyFilesFassets(Context context,String oldPath,String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File file = new File(newPath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFassets(context,oldPath + "/" + fileName,newPath+"/"+fileName);
                }
            } else {//如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount=0;
                while((byteCount=is.read(buffer))!=-1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 播放音频
     * @param context
     * @param type
     */
    public static void playHint(Context context, int type) {
        Log.i(TAG, "//////== httpserver : 播放音频" + type);
        try {
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.wav_domigo_connect);
            mediaPlayer.reset();
            switch (type){
                case 0://正在连接
                    mediaPlayer = MediaPlayer.create(context, R.raw.wav_domigo_connect);
                    break;
                case 1://成功
                    mediaPlayer = MediaPlayer.create(context, R.raw.wav_domigo_success);
                    break;
                case 2://失败
                    mediaPlayer = MediaPlayer.create(context, R.raw.wav_domigo_failure);
                    break;
            }
            mediaPlayer.start();//开始播放

        } catch (Exception e){
            Log.e(TAG, "//////== httpserver : 播放音频异常" + type);
            e.printStackTrace();
        }
    }

    /**
     * 此方法用于判断当前设备是否插入网线
     * @param context
     * @return
     */
    public static boolean isEthernetConnected(Context context) {
        boolean isConnected = false;
        ConnectivityManager conn =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conn != null) {
            NetworkInfo networkInfo = conn.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
            if(networkInfo != null){
                isConnected = networkInfo.isConnected();
            }
        }
        return isConnected;
    }

    public static NetworkInfo.State getWifiState(Context context) {
        NetworkInfo.State state = NetworkInfo.State.DISCONNECTED;
        ConnectivityManager conn =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conn != null) {
            NetworkInfo networkInfo = conn.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if(networkInfo != null){
                state = networkInfo.getState();
            }
        }
        return state;
    }

}

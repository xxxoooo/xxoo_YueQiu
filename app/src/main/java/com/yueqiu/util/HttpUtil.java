package com.yueqiu.util;

import android.util.Log;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * Http请求帮助类
 * Created by yinfeng on 14/12/18.
 */
public class HttpUtil {

    private static final String TAG = "HttpUtil";

    private static final int READLENGTH = 1024;

    private static final String HTTP = "http://hxu0480201.my3w.com/index.php/v1";

    private static final String CHARSET = "utf-8";

    /**
     * 发送请求到服务器
     * @param url 请求地址。
     * @param map 请求的参数，封装在map中，如果没有参数设置为null。
     * @param method 请求的方式，如果为null或者为“”，则默认为get请求
     * @return
     */
    public static <T> String urlClient(String url,Map<String,T> map,String method)
    {
        Log.d("wy","cacaca");
        if(null == url || "".equals(url))
        {
            throw new NullPointerException("url is null!");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(HTTP).append(url);
        boolean flag = (null == map || 0 == map.size()) ? false : true;
        if(flag)
        {
            sb.append("?");
            Iterator iter = map.entrySet().iterator();
            while (iter.hasNext())
            {
                Map.Entry<String, T> entry = (Map.Entry<String, T>)iter.next();
                try {
                    sb.append(entry.getKey()).append("=").
                            append(URLEncoder.encode(String.valueOf(entry.getValue()), CHARSET)).
                            append("&");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        log(sb.toString());
        try {
            URL urls = new URL(sb.toString());
            HttpURLConnection conn = (HttpURLConnection)urls.openConnection();
            String requestMethod = (null == method || "".equals(method)) ? "GET" : method;
            conn.setRequestMethod(requestMethod);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();
            StringBuilder result = new StringBuilder();
            if(conn.getResponseCode() == 200)
            {
                InputStream in = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(in);
                byte [] data = new byte[1024];
                while(bis.read(data) > 0)
                {
                    result.append(new String(data));
                }
            }
            conn.disconnect();
            return result.toString().trim();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void log(String msg)
    {
        Log.i(TAG, msg);
    }
}

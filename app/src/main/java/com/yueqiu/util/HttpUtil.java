package com.yueqiu.util;

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
 * Created by yinfeng on 14/12/18.
 */
public class HttpUtil {

    private static final int READLENGTH = 1024;

    private static final String CHARSET = "utf-8";
    /**
     * 发送请求到服务器
     * @param url 请求地址。
     * @param map 请求的参数，封装在map中，如果没有参数设置为null。
     * @param method 请求的方式，如果为null或者为“”，则默认为get请求
     * @return
     */
    public static String urlClient(String url,Map<String,String> map,String method)
    {
        if(null == url || "".equals(url))
        {
            throw new NullPointerException("url is null!");
        }
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        boolean flag = (null == map || 0 == map.size()) ? false : true;
        if(flag)
        {
            sb.append("?");
            Iterator iter = map.entrySet().iterator();
            while (iter.hasNext())
            {
                Map.Entry<String, String> entry = (Map.Entry<String, String>)iter.next();
                try {
                    sb.append(entry.getKey()).append("=").
                            append(URLEncoder.encode(entry.getValue(), CHARSET)).
                            append("&");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        try {
            URL urls = new URL(sb.toString());
            HttpURLConnection conn = (HttpURLConnection)urls.openConnection();
            String requestMethod = (null == method || "".equals(method)) ? "GET" : method;
            conn.setRequestMethod(requestMethod);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();
            int code = conn.getResponseCode();
            if(code != 200)
            {
                return null;
            }

            InputStream in = conn.getInputStream();
            byte [] data = new byte[READLENGTH];
            StringBuffer result = new StringBuffer();
            while(in.read(data) > 0)
            {
                result.append(new String(data));
            }
            in.close();
            return result.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}

package com.yueqiu.util;

import android.text.TextUtils;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.yueqiu.constant.HttpConstants;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;


public class HttpUtil
{

    private static final String TAG = "HttpUtil";
    private static final String TAG_1 = "http_outofbounds_debug";

    private static final int READLENGTH = 1024;

    private static final String HTTP = "http://app.chuangyezheluntan.com/index.php/v1";

    private static final String CHARSET = "utf-8";

    /**
     * 发送请求到服务器
     *
     * @param url    请求地址。
     * @param map    请求的参数，封装在map中，如果没有参数设置为null。
     * @param method 请求的方式，如果为null或者为“”，则默认为get请求
     * @return
     */
    public static <T> String urlClient(String url,Map<String,T> map,String method)
    {
        if(null == url || "".equals(url))
        {
            throw new NullPointerException("url is null!");
        }
        String realResult = null;
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
                try
                {
                    sb.append(entry.getKey()).append("=").
                            append(URLEncoder.encode(String.valueOf(entry.getValue()), CHARSET)).
                            append("&");
                } catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        log(sb.toString());

        try
        {
            URL urls = new URL(sb.toString());
            HttpURLConnection conn = (HttpURLConnection)urls.openConnection();
            String requestMethod = (null == method || "".equals(method)) ? "GET" : method;
            conn.setRequestMethod(requestMethod);
            conn.setConnectTimeout(3000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();
            StringBuilder result = new StringBuilder();
            if (conn.getResponseCode() == 200) {
                InputStream in = conn.getInputStream();
//                BufferedInputStream bis = new BufferedInputStream(in);
                byte[] data = new byte[1024];
                while ( -1 != in.read(data,0,data.length)) {
                    result.append(new String(data));
                 }
                in.close();
            }

            conn.disconnect();
            return result.toString().trim();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SocketTimeoutException e){
            realResult = "{\"code\":1011}";
        } catch (IOException e) {
            e.printStackTrace();
        } catch (final Exception e)
        {
            e.printStackTrace();
            Log.d(TAG_1, " finally exception happened here, and the reason are : " + e.toString());
        }
        return realResult;
    }


    public static <T> void requestHttp(String url,Map<String,T> map,String method,JsonHttpResponseHandler responseHandler){

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        if(map !=null ) {
            Iterator iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, T> entry = (Map.Entry<String, T>) iter.next();
                params.put(entry.getKey(), entry.getValue());
            }
        }

        if(method.equals(HttpConstants.RequestMethod.GET) || TextUtils.isEmpty(method)){
            client.setTimeout(6000);
            client.get(HttpConstants.BASE_URL + url,params,responseHandler);

        }
        if(method.equals(HttpConstants.RequestMethod.POST)){
            client.setTimeout(6000);
            client.post(HttpConstants.BASE_URL + url,params,responseHandler);
        }

    }

    //TODO:测试验证码的方法，等到所有接口地址都部署到新服务器地址上删去该方法
    public static <T> void testHttp(String url,Map<String,T> map,String method,JsonHttpResponseHandler responseHandler){

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        if(map !=null ) {
            Iterator iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, T> entry = (Map.Entry<String, T>) iter.next();
                params.put(entry.getKey(), entry.getValue());
            }
        }

        if(method.equals(HttpConstants.RequestMethod.GET) || TextUtils.isEmpty(method)){
            client.get("http://www.pinruiwenhua.com/app/index.php/v1/" + url,params,responseHandler);

        }
        if(method.equals(HttpConstants.RequestMethod.POST)){
            client.post("http://www.pinruiwenhua.com/app/index.php/v1/" + url,params,responseHandler);
        }

    }

    public static  void dpRequestHttp(String baseUrl,String relativeUrl,String appKey,String appSecret,Map<String,String> map,JsonHttpResponseHandler responseHandler){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        String sign = sign(appKey, appSecret, map);
        try {
            params.put("appkey",new String(appKey.getBytes(), "UTF-8"));
            params.put("sign",new String(sign.getBytes(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Iterator iter = map.entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry<String, String> entry = (Map.Entry<String, String>)iter.next();
            try {
                params.put(entry.getKey(),new String(entry.getValue().getBytes(),"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
       client.get(baseUrl+relativeUrl,params,responseHandler);
    }

    /**
     * 以下的这个方法是专为SearchActivity当中的SearchRoomFragment(即球厅Fragment)当中的列表,
     * 需要注意的是如果以下的请求使用HttpUrlConnection的话，是请求不到数据的，而且服务器端会报的
     * 错误也是10015，也就是请求方法错误(所以我们直接使用的是Apache HttpClient进行数据的请求)
     *
     * @param baseUrl
     * @param relativeUrl
     * @param map
     * @param <T>
     * @return 返回球厅Fragment当中的商家的列表信息
     */
    public static <T> String dpUrlClient(String baseUrl, String relativeUrl, String appKey, String appSecret, Map<String, T> map)
    {
        if (null == relativeUrl || "".equals(relativeUrl)) {
            throw new NullPointerException("url is null!");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(baseUrl).append(relativeUrl);
        Log.d(TAG, " the full request url are : " + sb.toString());

        boolean flag = (null == map || 0 == map.size()) ? false : true;
        if (flag) {
            sb.append("?");
            sb.append(getQueryString(appKey, appSecret, (Map<String, String>) map));
            Log.d("filter_param_test", " dp request --> the finally request url are : " + sb.toString());
        }

        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(sb.toString());
        HttpResponse response;
        try {
            response = client.execute(request);
            Log.d(TAG, "response->" + response.getStatusLine().getStatusCode());
            String result = EntityUtils.toString(response.getEntity());

            Log.d(TAG, "Wrong one : " + result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 用于将大众点评提供的appKey以及appSecret拼接成完整的请求参数
     * 因为大众点评是需要appKey和appSecret验证的
     * 另外就是大众点评的请求的url需要采用UTF-8进行编码
     *
     * @param appKey
     * @param appSecret
     * @param requestParams
     * @return
     */
    private static String getQueryString(String appKey, String appSecret, Map<String, String> requestParams)
    {
        String sign = sign(appKey, appSecret, requestParams);

        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("appkey=").append(new String(appKey.getBytes(), "UTF-8")).append("&sign=").append(new String(sign.getBytes(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
            try {
                stringBuilder.append('&').append(entry.getKey()).append('=').append(new String(entry.getValue().getBytes(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        String queryStr = stringBuilder.toString();
        return queryStr;
    }

    private static String sign(String appKey, String appSecret, Map<String, String> requestParams)
    {
        // 将请求参数排序
        String[] keyArray = requestParams.keySet().toArray(new String[0]);
        Arrays.sort(keyArray);

        // 将所有的请求参数拼接到一起
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(appKey);
        for (String key : keyArray) {
            stringBuilder.append(key).append(requestParams.get(key));
        }

        stringBuilder.append(appSecret);

        String codec = stringBuilder.toString();

        String sign = new String(Hex.encodeHex(DigestUtils.sha(codec))).toUpperCase();

        return sign;
    }


    public static InputStream getInputStream(String url)
    {
        try {
            URL urls = new URL(url);
            HttpURLConnection connection = (HttpURLConnection)urls.openConnection();
            connection.setDoInput(true);
            connection.connect();
            return connection.getInputStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void log(String msg)
    {
        Log.i("wy", "msg is ->"+ msg);
    }
}

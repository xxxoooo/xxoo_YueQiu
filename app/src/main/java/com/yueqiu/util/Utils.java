package com.yueqiu.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.util.Log;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.constant.PublicConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by yinfeng on 14/12/26.
 */
public class Utils {

    private static final String TAG = "Utils";

    private static SharedPreferences mSharedPreferences;


    public static void getOrUpdateUserBaseInfo(Context context, Map<String,String> map)
    {
        mSharedPreferences = context.getSharedPreferences(PublicConstant.USERBASEUSER,
                                                            Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        Iterator iterator = map.entrySet().iterator();
        while(iterator.hasNext())
        {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
            editor.putString(entry.getKey(),entry.getValue());
        }
        editor.commit();

        YueQiuApp.sUserInfo.setImg_url(map.get(PublicConstant.IMG_URL));
        YueQiuApp.sUserInfo.setAccount(map.get(PublicConstant.USER_NAME));
        YueQiuApp.sUserInfo.setUser_id(Integer.valueOf(map.get(PublicConstant.USER_ID)));
        YueQiuApp.sUserInfo.setPhone(map.get(PublicConstant.PHONE));
    }

    public static void removeUserBaseInfo(Context context)
    {
        mSharedPreferences = context.getSharedPreferences(PublicConstant.USERBASEUSER,
                                                            Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.commit();
    }


    /*
     * 解析json
     */
    public static JSONObject parseJson(String result)
    {
        JSONObject object = null;
        try{
            if(result != null) {
                object = new JSONObject(result);
            }else{
                object = new JSONObject();
                object.put("code",1010);
                object.put("msg","error");
                object.put("result",null);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * 得到sd卡真实路径
     * @return
     */
    public static String getSDCardPath()
    {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    private static void log(String msg)
    {
        Log.i(TAG,"----"+msg+"----");
    }


    /**
     * 检测当前网络是否可用
     * @param context
     * @return
     */
    public static boolean networkAvaiable(Context context)
    {
        ConnectivityManager connManager = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connManager.getActiveNetworkInfo() != null)
            return connManager.getActiveNetworkInfo().isAvailable();
        return false;
    }

    /**
     * String类型转换为Date
     * @param currTime
     * @param formatType
     * @return
     */
    public static Date stringToDate(String currTime, String formatType)throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = formatter.parse(currTime);
        return date;
    }
    /**
     * Date类型转换为String
     */
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType).format(data);
    }
    /**
     *long类型转换为Date类型
     */
    public static Date longToDate(long currTime,String formatType) throws ParseException {
        Date old = new Date(currTime);
        String time = dateToString(old, formatType);
        Date date = stringToDate(time, formatType);
        return date;
    }

    /**
     * long类型时间转换为String类型
     */
    public static String longToString(long currTime,String formatType) throws ParseException {
        Date date = longToDate(currTime, formatType);
        String strTime = dateToString(date, formatType);
        return strTime;
    }

    /**
     * Date类型转换为long
     */
    public static long dateToLong(Date date) {
        return date.getTime();
    }
    /**
     * String类型转换为long
     */
    public static long stringToLong(String strTime, String formatType)
            throws ParseException {
        Date date = stringToDate(strTime, formatType); // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            long currentTime = dateToLong(date); // date类型转成long类型
            return currentTime;
        }
    }

}

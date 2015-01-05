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
            object = new JSONObject(result);
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


}

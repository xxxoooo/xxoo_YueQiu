package com.yueqiu.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

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
    public static void getOrUpdateUserBaseInfo(Context context,Map<String,String> map)
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
            log(object.toString());
            if(Integer.valueOf(object.get("code").toString()) != 1001);
               return null;
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


}

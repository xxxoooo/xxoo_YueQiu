package com.yueqiu.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.util.Log;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.PublicConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by yinfeng on 14/12/26.
 */
public class Utils {

    private static final String TAG = "Utils";

    //user info local data serializer file name
    private static final String USER_INFO_FILE_NAME = "userInfo.json";
    private static SharedPreferences mSharedPreferences;


    public static void getOrUpdateUserBaseInfo(Context context, Map<String, String> map) {
        mSharedPreferences = context.getSharedPreferences(PublicConstant.USERBASEUSER,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
            editor.putString(entry.getKey(), entry.getValue());
        }
        editor.commit();

        YueQiuApp.sUserInfo.setImg_url(map.get(PublicConstant.IMG_URL));
        YueQiuApp.sUserInfo.setAccount(map.get(PublicConstant.USER_NAME));
        YueQiuApp.sUserInfo.setUser_id(Integer.valueOf(map.get(PublicConstant.USER_ID)));
    }

    public static void removeUserBaseInfo(Context context) {
        mSharedPreferences = context.getSharedPreferences(PublicConstant.USERBASEUSER,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.commit();
    }


    /*
     * 解析json
     */
    public static JSONObject parseJson(String result) {
        JSONObject object = null;
        try {
            object = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    /**
     * 得到sd卡真实路径
     *
     * @return
     */
    public static String getSDCardPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    private static void log(String msg) {
        Log.i(TAG, "----" + msg + "----");
    }


    /**
     * 检测当前网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean networkAvaiable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager.getActiveNetworkInfo() != null)
            return connManager.getActiveNetworkInfo().isAvailable();
        return false;
    }

    /**
     * 更新我的资料
     */
    public static void updateMyProfile(Context context, UserInfo userInfo) throws IOException, JSONException {
        JSONArray array = new JSONArray();
        array.put(userInfo.toJSON());

        Writer writer = null;
        try {
            OutputStream out = context.openFileOutput(USER_INFO_FILE_NAME, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(array.toString());
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    /**
     * 从本地获取我的资料
     *
     * @return
     */
    public static UserInfo getMyProfileFromLocal(Context context) {
        UserInfo userInfo = null;
        BufferedReader reader = null;
        try {
            InputStream is = context.openFileInput(USER_INFO_FILE_NAME);
            reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonString = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            JSONArray array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();

            userInfo = new UserInfo(array.getJSONObject(0));


        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.toString());
        } finally {
            if (reader != null)
                try {
                    reader.close();
                }catch (IOException e) {
                    Log.e(TAG, "IOException: " + e.toString());
                }
        }
        return userInfo;
    }
}

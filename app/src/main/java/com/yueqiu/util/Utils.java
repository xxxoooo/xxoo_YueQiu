package com.yueqiu.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.PublicConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        YueQiuApp.sUserInfo.setPhone(map.get(PublicConstant.PHONE));
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
<<<<<<< HEAD
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
    /*
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

    /**
     * 设置FragmentActivity的Menu文字的颜色为白色
     */
    public static void setFragmentActivityMenuColor(FragmentActivity context){
        final LayoutInflater layoutInflater = context.getLayoutInflater();
        final LayoutInflater.Factory existingFactory = layoutInflater.getFactory();
        try {
            Field field = LayoutInflater.class.getDeclaredField("mFactorySet");
            field.setAccessible(true);
            field.setBoolean(layoutInflater, false);
            context.getLayoutInflater().setFactory(new LayoutInflater.Factory() {
                @Override
                public View onCreateView(String name, final Context context, AttributeSet attrs) {
                    if (name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuItemView")
                            || name.equalsIgnoreCase("com.android.internal.view.menu.ActionMenuItemView")){
                        View view = null;
                        // if a factory was already set, we use the returned view
                        if (existingFactory != null) {
                            view = existingFactory.onCreateView(name, context, attrs);
                            if (view == null) {
                                try {
                                    view = layoutInflater.createView(name, null, attrs);
                                    final View finalView = view;
                                    if (view instanceof TextView) {

                                        new Handler().post(new Runnable() {
                                            public void run() {
                                                ((TextView) finalView).setTextColor(context.getResources().getColor(R.color.white));
                                            }
                                        });
                                    }

                                    return finalView;
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        return view;
                    }
                    return null;
                }
            });
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    /**
     * 设置普通Activity的Menu文字的颜色为白色
     */
    public static void setActivityMenuColor(final Activity activity){
        activity.getLayoutInflater().setFactory(
                new android.view.LayoutInflater.Factory() {
                    public View onCreateView(String name, Context context,AttributeSet attrs) {
                        // 指定自定义inflate的对象
                        if (name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuItemView")
                                || name.equalsIgnoreCase("com.android.internal.view.menu.ActionMenuItemView")) {
                            try {
                                LayoutInflater f = activity.getLayoutInflater();
                                final View view = f.createView(name, null,attrs);
                                if(view instanceof TextView) {
                                    new Handler().post(new Runnable() {
                                        public void run() {
                                            // 设置背景图片
                                            //view.setBackgroundResource(R.color.login_btn_normal);
                                            ((TextView) view).setTextColor(activity.getResources().getColor(R.color.white));
                                        }
                                    });
                                }
                                return view;
                            } catch (InflateException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }
                }
        );
    }

}

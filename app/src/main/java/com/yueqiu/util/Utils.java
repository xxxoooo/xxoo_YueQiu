package com.yueqiu.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.PublicConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.RandomAccessFile;
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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final String TAG = "Utils";

    //user info local data serializer file name
    public static final String USER_INFO_FILE_NAME = "userInfo.json";
    private static SharedPreferences mSharedPreferences;


    public static void getOrUpdateUserBaseInfo(Context context, Map<String, String> map) {
        mSharedPreferences = context.getSharedPreferences(PublicConstant.USERBASEUSER,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
            if (entry.getKey() != DatabaseConstant.UserTable.PASSWORD)
                editor.putString(entry.getKey(), entry.getValue());
        }
        editor.commit();


        YueQiuApp.sUserInfo.setImg_url(map.get(DatabaseConstant.UserTable.IMG_URL));
        YueQiuApp.sUserInfo.setUsername(map.get(DatabaseConstant.UserTable.USERNAME));
        YueQiuApp.sUserInfo.setUser_id(Integer.valueOf(map.get(DatabaseConstant.UserTable.USER_ID)));
        YueQiuApp.sUserInfo.setPhone(map.get(DatabaseConstant.UserTable.PHONE));

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
            if (result != null) {
                object = new JSONObject(result);
            } else {
                object = new JSONObject();
                object.put("code", 1010);
                object.put("msg", "error");
                object.put("result", null);
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
     * String类型转换为Date
     *
     * @param currTime
     * @param formatType
     * @return
     */
    public static Date stringToDate(String currTime, String formatType) throws ParseException {
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
     * long类型转换为Date类型
     */
    public static Date longToDate(long currTime, String formatType) throws ParseException {
        Date old = new Date(currTime);
        String time = dateToString(old, formatType);
        Date date = stringToDate(time, formatType);
        return date;
    }

    /**
     * long类型时间转换为String类型
     */
    public static String longToString(long currTime, String formatType) throws ParseException {
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
    public static void updateJSONData(Context context, JSONHelper jsonHelper, String path) throws IOException, JSONException {
        //JSONArray array = new JSONArray();
        //array.put(jsonHelper.toJSON());

        JSONObject object = jsonHelper.toJSON();

        FileWriter writer = null;
        try {
            //OutputStream out = context.openFileOutput(path, Context.MODE_PRIVATE);
            File file = context.getFileStreamPath(path);
            RandomAccessFile randomFile = new RandomAccessFile(file, "rw");
            long randomLength = randomFile.length();
            if (randomLength == 0) {
                randomFile.seek(randomLength);
                randomFile.writeBytes("[");
            } else if (randomLength == 1) {
                randomFile.seek(randomLength);
                randomFile.writeBytes(object.toString() + "]");
            } else {
                randomFile.seek(randomLength - 1);
                randomFile.writeBytes(object.toString() + "]");
            }

            randomFile.close();

        } finally {
            if (writer != null)
                writer.close();
        }
    }

    /**
     * 直接保存RESTFUL获取的资料JSON数据到本地
     *
     * @param context
     * @param array
     * @throws IOException
     * @throws JSONException
     */
    public static void saveMyProfileJSONFromService(Context context, JSONArray array) throws IOException, JSONException {

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
    public static JSONArray getJSONFromLocal(Context context) {
        BufferedReader reader = null;
        JSONArray array = null;
        try {
            InputStream is = context.openFileInput(USER_INFO_FILE_NAME);
            reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            array = (JSONArray) new JSONTokener(jsonString.toString()).nextValue();


        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.toString());
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException: " + e.toString());
                }
        }
        return array;
    }

    /**
     * 设置FragmentActivity的Menu文字的颜色为白色
     */
    public static void setFragmentActivityMenuColor(FragmentActivity context) {
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
                            || name.equalsIgnoreCase("com.android.internal.view.menu.ActionMenuItemView")) {
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
    public static void setActivityMenuColor(final Activity activity) {
        activity.getLayoutInflater().setFactory(
                new android.view.LayoutInflater.Factory() {
                    public View onCreateView(String name, Context context, AttributeSet attrs) {
                        // 指定自定义inflate的对象
                        if (name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuItemView")
                                || name.equalsIgnoreCase("com.android.internal.view.menu.ActionMenuItemView")) {
                            try {
                                LayoutInflater f = activity.getLayoutInflater();
                                final View view = f.createView(name, null, attrs);
                                if (view instanceof TextView) {
                                    new Handler().post(new Runnable() {
                                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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

    /**
     * 在EditText中插入表情图片
     *
     * @param sourceStr
     */
    public static SpannableStringBuilder addImgIntoEditText(Context context, String sourceStr, String replaceStr, int drawableResId) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(sourceStr);
        Pattern pattern = Pattern.compile(replaceStr);
        Matcher matcher = pattern.matcher(sourceStr);

        Drawable drawable = context.getResources().getDrawable(drawableResId);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        while (matcher.find()) {
            ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
            spannable.setSpan(span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    /**
     * 将JSONObject转换成相应对象
     * 如果有浮点型的数据，都使用double
     *
     * @param clazz
     * @param object
     * @return
     */
    public static <T> T mapingObject(Class<T> clazz, JSONObject object) {
        T t = null;
        try {
            t = clazz.newInstance();
            Method[] methods = clazz.getDeclaredMethods();
            Field[] fields = clazz.getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {
                if (!object.isNull(fields[i].getName())) {
                    Object o = object.get(fields[i].getName());
                    if (o != null) {
                        fields[i].setAccessible(true);
                        fields[i].set(t, toRealObject(o));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    /**
     * 类型转换
     *
     * @param o
     * @return
     */
    private static Object toRealObject(Object o) {
        if (o instanceof Integer)
            return (Integer) o;
        else if (o instanceof Short)
            return (Short) o;
        else if (o instanceof Long)
            return (Long) o;
        else if (o instanceof Double)
            return (Double) o;
        else if (o instanceof String)
            return (String) o;
        else if (o instanceof Boolean)
            return (Boolean) o;
        else if (o instanceof Byte)
            return (Byte) o;
        return o;
    }


    //    private final class ProImageGetter implements Html.ImageGetter{
//
//        @Override
//        public Drawable getDrawable(String source) {
//            // 获取到资源id
//            int id = Integer.parseInt(source);
//            Drawable drawable = getResources().getDrawable(id);
//            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
//            return drawable;
//        }
//    }


}

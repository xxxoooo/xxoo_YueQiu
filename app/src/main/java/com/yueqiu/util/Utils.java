package com.yueqiu.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.PublicConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final String TAG = "Utils";
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


    /**
     * 解析json
     */
    public static JSONObject parseJson(String result) {
        JSONObject object;
        try {
            if (result != null) {
                object = new JSONObject(result);
            } else {
                object = new JSONObject();
                object.put("code", 1010);
                object.put("msg", "网络请求错误");
                object.put("result", null);
            }
        } catch (JSONException e) {
            object = new JSONObject();
        } catch (NumberFormatException e){
            object = new JSONObject();
        }
        return object;
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

    /**
     * 获取当前时间，并将当前时间转化为yyyy-mm-dd hh-mm的格式
     * @return
     */
    public static String getNowTime()
    {
        SimpleDateFormat sdp = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        return sdp.format(new Date());
    }

    /**
     * 弹出底部对话框,并初始化所有view
     * @param context
     * @return
     */
    public static Dialog showSheet(Context context) {
        final Dialog dlg = new Dialog(context, R.style.ActionSheet);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.dialog_share, null);
        final int cFullFillWidth = 10000;
        layout.setMinimumWidth(cFullFillWidth);


        Window window = dlg.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = 0;
        final int cMakeBottom = -1000;
        lp.y = cMakeBottom;
        lp.gravity = Gravity.BOTTOM;
        dlg.onWindowAttributesChanged(lp);
        //dlg.setCanceledOnTouchOutside(false);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        int width = point.x;
        int height = point.y;
        dlg.setContentView(layout);
        dlg.getWindow().setLayout(width, height/2);

        TextView tvYueqiu = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_yuqeiufirend);
        TextView tvYueqiuCircle = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_yueqiucircle);
        TextView tvFriendCircle = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_friendcircle);
        TextView tvWeichat = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_weichat);
        TextView tvQQZone = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_qqzone);
        TextView tvTencentWeibo = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_qqweibo);
        TextView tvSinaWeibo = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_sinaweibo);
        TextView tvRenren = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_renren);
        TextView btnCancel = (Button) dlg.findViewById(R.id.btn_search_dating_detailed_cancel);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.btn_search_dating_detailed_cancel:
                        dlg.dismiss();
                        break;
                    case R.id.img_search_dating_detail_share_yuqeiufirend:
                        break;
                    case R.id.img_search_dating_detail_share_yueqiucircle:
                        break;
                    case R.id.img_search_dating_detail_share_weichat:
                        break;
                    case R.id.img_search_dating_detail_share_qqzone:
                        break;
                    case R.id.img_search_dating_detail_share_qqweibo:
                        break;
                    case R.id.img_search_dating_detail_share_sinaweibo:
                        break;
                    case R.id.img_search_dating_detail_share_renren:
                        break;
                }
            }
        };
        tvYueqiu.setOnClickListener(listener);
        tvYueqiuCircle.setOnClickListener(listener);
        tvFriendCircle.setOnClickListener(listener);
        tvFriendCircle.setOnClickListener(listener);
        tvWeichat.setOnClickListener(listener);
        tvQQZone.setOnClickListener(listener);
        tvRenren.setOnClickListener(listener);
        tvSinaWeibo.setOnClickListener(listener);
        tvTencentWeibo.setOnClickListener(listener);
        btnCancel.setOnClickListener(listener);

        return dlg;
    }

    /**
     * dp转px
     * @param context
     * @param value
     * @return
     */
    public static int dip2px(Context context,float value){
        float scaleing=context.getResources().getDisplayMetrics().density;
        return (int) (value*scaleing+0.5f);
    }

    /**
     * px转dp
     * @param context
     * @param value
     * @return
     */
    public static int px2dip(Context context,float value){
        float scaling=context.getResources().getDisplayMetrics().density;
        return (int) (value/scaling+0.5f);
    }

//    public static  void showToast(Activity activity,String msg) {
//
//        TypedValue value = new TypedValue();
//        int actionBarHeight = 0;
//        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, value, true)) {
//            actionBarHeight= TypedValue.complexToDimensionPixelSize(value.data, activity.getResources().getDisplayMetrics());
//        }
//
//        LayoutInflater inflater = activity.getLayoutInflater();
//
//        View layout = inflater.inflate(R.layout.custom_toast_layout,
//                (ViewGroup) activity.findViewById(R.id.toast_layout_root));
//
//        Button button = (Button) layout.findViewById(R.id.bt_for_toast);
//        button.setText(msg);
//
//        Toast toast = new Toast(activity);
//        toast.setDuration(Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.TOP, 0, actionBarHeight);
//        toast.setView(layout);
//        toast.show();
//    }
    public static void showToast(Context context,String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }

    public static <T extends View> T $(Activity activity,int resId){
        T t = (T) activity.findViewById(resId);
        return t;
    }

    public static void dismissInputMethod(Context context, EditText ed) {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ed.getWindowToken(), 0);
    }
}

package com.yueqiu.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
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

import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QzoneShare;
import com.tencent.open.t.Weibo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.yueqiu.R;
import com.yueqiu.YueQiuApp;
import com.yueqiu.activity.WeiboShareActionCompleteActivity;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{

    private static final String TAG = "Utils";
    private static SharedPreferences mSharedPreferences;


    public static void getOrUpdateUserBaseInfo(Context context, Map<String, String> map)
    {
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

    public static void removeUserBaseInfo(Context context)
    {
        mSharedPreferences = context.getSharedPreferences(PublicConstant.USERBASEUSER,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.commit();
    }


    /**
     * 解析json
     */
    public static JSONObject parseJson(String result)
    {
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
        } catch (NumberFormatException e) {
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
    public static boolean networkAvaiable(Context context)
    {
        if (null != context) {
            ConnectivityManager connManager = (ConnectivityManager) context.
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connManager.getActiveNetworkInfo() != null)
                return connManager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }

    /**
     * String类型转换为Date
     *
     * @param currTime
     * @param formatType
     * @return
     */
    public static Date stringToDate(String currTime, String formatType) throws ParseException
    {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = formatter.parse(currTime);
        return date;
    }

    /**
     * Date类型转换为String
     */
    public static String dateToString(Date data, String formatType)
    {
        return new SimpleDateFormat(formatType).format(data);
    }

    /**
     * long类型转换为Date类型
     */
    public static Date longToDate(long currTime, String formatType) throws ParseException
    {
        Date old = new Date(currTime);
        String time = dateToString(old, formatType);
        Date date = stringToDate(time, formatType);
        return date;
    }

    /**
     * long类型时间转换为String类型
     */
    public static String longToString(long currTime, String formatType) throws ParseException
    {
        Date date = longToDate(currTime, formatType);
        String strTime = dateToString(date, formatType);
        return strTime;
    }

    /**
     * Date类型转换为long
     */
    public static long dateToLong(Date date)
    {
        return date.getTime();
    }

    /**
     * String类型转换为long
     */
    public static long stringToLong(String strTime, String formatType)
            throws ParseException
    {
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
    public static void setFragmentActivityMenuColor(FragmentActivity context)
    {
        final LayoutInflater layoutInflater = context.getLayoutInflater();
        final LayoutInflater.Factory existingFactory = layoutInflater.getFactory();
        try {
            Field field = LayoutInflater.class.getDeclaredField("mFactorySet");
            field.setAccessible(true);
            field.setBoolean(layoutInflater, false);
            context.getLayoutInflater().setFactory(new LayoutInflater.Factory()
            {
                @Override
                public View onCreateView(String name, final Context context, AttributeSet attrs)
                {
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

                                        new Handler().post(new Runnable()
                                        {
                                            public void run()
                                            {
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
    public static void setActivityMenuColor(final Activity activity)
    {
        activity.getLayoutInflater().setFactory(
                new android.view.LayoutInflater.Factory()
                {
                    public View onCreateView(String name, Context context, AttributeSet attrs)
                    {
                        // 指定自定义inflate的对象
                        if (name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuItemView")
                                || name.equalsIgnoreCase("com.android.internal.view.menu.ActionMenuItemView")) {
                            try {
                                LayoutInflater f = activity.getLayoutInflater();
                                final View view = f.createView(name, null, attrs);
                                if (view instanceof TextView) {
                                    new Handler().post(new Runnable()
                                    {
                                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                                        public void run()
                                        {
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
    public static SpannableStringBuilder addImgIntoEditText(Context context, String sourceStr, String replaceStr, int drawableResId)
    {
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
    public static <T> T mapingObject(Class<T> clazz, JSONObject object)
    {
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
    private static Object toRealObject(Object o)
    {
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
     *
     * @return
     */
    public static String getNowTime()
    {
        SimpleDateFormat sdp = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        return sdp.format(new Date());
    }

    public static Dialog showSheet(Context context)
    {
        return showSheet(context, null);
    }

    /**
     * 弹出底部对话框,并初始化所有view
     *
     * @param context 这个参数一定要是一个完整的Activity实例，因为我们需要通过这个context实例来启动微博分享的Activity
     * @return
     */
    public static Dialog showSheet(final Context context, Intent intent)
    {
        // 创建用于实现微信分享的实例
        final WeChatShareManager weChatShareManager = WeChatShareManager.getInstance(context, intent);
        final RenRenShareManager renRenShareManager = RenRenShareManager.getInstance(context);
        // 创建用于QQ微博和QQ空间的分享的API实例,以下是关于QQ分享的限制的几点，我们需要到时候以合适的方式提醒以下用户
        // TODO: 腾讯微博分享需要安装QQ客户端
        // TODO: 但是QQ客户端如果安装上会导致分享到QQ空间无法实现(目前没有报出认出任何有关于这两者之间的冲突)
        final Tencent tencent = Tencent.createInstance(HttpConstants.QQ_ZONE_APP_KEY, context);
        // 用于判断QQ的客户端是否已经安装了，如果已经安装的话，是不支持分享到QQ空间的
        final boolean isQQClientInstalled = isPackageInstalled("com.tencent.mobileqq", context);

        // 用于实现腾讯微博分享的回调
        final IUiListener tencentWeiboListener = new IUiListener()
        {
            @Override
            public void onComplete(Object response)
            {
                try {
                    JSONObject result = (JSONObject) response;
                    int ret = result.getInt("ret");
                    if (result.has("data")) {
                        JSONObject data = result.getJSONObject("data");
                        if (data.has("id")) {
                            String lastAddedTweetId = data.getString("id");
                            Log.e(TAG, " ret : " + ret + " data = " + data + " time = " + lastAddedTweetId);
                        }
                    }
                    if (ret == 0) {
                        Log.e(TAG, " successful by sending one tencent weibo to tencent weibo ");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(UiError uiError)
            {
                Log.e(TAG, " error by sharing message to tencent weibo ");
            }

            @Override
            public void onCancel()
            {
                Log.e(TAG, " sharing to tencent weibo has been cancelled ");
            }
        };

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
        dlg.getWindow().setLayout(width, height / 2);

//        TextView tvYueqiu = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_yuqeiufirend);
//        TextView tvYueqiuCircle = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_yueqiucircle);

        TextView tvFriendCircle = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_friendcircle);
        TextView tvWeichat = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_weichat);
        TextView tvQQZone = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_qqzone);
        TextView tvTencentWeibo = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_qqweibo);
        TextView tvSinaWeibo = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_sinaweibo);
        TextView tvRenren = (TextView) dlg.findViewById(R.id.img_search_dating_detail_share_renren);
        TextView btnCancel = (Button) dlg.findViewById(R.id.btn_search_dating_detailed_cancel);

        View.OnClickListener listener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (v.getId()) {
                    case R.id.btn_search_dating_detailed_cancel:
                        dlg.dismiss();
                        break;
//                    case R.id.img_search_dating_detail_share_yuqeiufirend:
//                        break;
//                    case R.id.img_search_dating_detail_share_yueqiucircle:
//                        break;

                    case R.id.img_search_dating_detail_share_friendcircle:
                        // 这是分享到朋友圈的实现(分享到朋友圈即直接分享到微信的timeLine上面)
                        // 我们需要分享的内容包括球厅的图片，球厅的价格以及球厅的活动信息说明
                        if (null != weChatShareManager) {
                            Log.d("wechat_share", " share to we chat ");
//                            weChatShareManager.shareByWeChat(weChatShareManager.new SharePicContent(R.drawable.ic_launcher),
//                                    WeChatShareManager.WECHAT_SHARE_WAY_PIC);
                            // 如果我们这里只是单纯的分享文字的话，那么分先的这段文字是无法编辑的，我们希望的是用户可以定制自己所发送的内容的
                            // 所以我们选择发送图片，因为发送图片的话，用户还可以编辑自己所发送的文本
                            // TODO: 但是可惜的是用户无法编辑自己所发送的图片了，因为图片是我们这里写死的,如果需要改进，就在这里改进一下
//                            weChatShareManager.shareByWeChat(
//                                    weChatShareManager.new ShareTextContent(context.getString(R.string.renren_share_content)),
//                                    true);
                            weChatShareManager.shareByWeChat(weChatShareManager.new SharePicContent(R.drawable.ic_launcher),
                                    true);
                        }
                        break;
                    case R.id.img_search_dating_detail_share_weichat:
                        // 分享到微信的指定好友那里
                        if (null != weChatShareManager) {
                            weChatShareManager.shareByWeChat(
                                    weChatShareManager.new ShareTextContent(context.getString(R.string.renren_share_content)),
                                    false);
                        }
                        break;
                    case R.id.img_search_dating_detail_share_qqzone:
                        final Bundle shareParmas = new Bundle();
                        final int shareType = QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT;
                        shareParmas.putString(QzoneShare.SHARE_TO_QQ_TITLE, context.getString(R.string.renren_share_content));
                        shareParmas.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, context.getString(R.string.renren_share_content));
                        if (shareType != QzoneShare.SHARE_TO_QZONE_TYPE_APP)
                        {
                            // 当我们使用QQ客户端app分享时，是不支持链接分享的
                            shareParmas.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, HttpConstants.DEFAULT_DIRECT_URL);
                        }
                        // TODO: 以下的图片的URL是我们添加的静态的URL，在正式发布时，我们最好修改一下(例如改成球厅的图片的URL)
                        ArrayList<String> imgUrls = new ArrayList<String>();
                        imgUrls.add("http://pic.sc.chinaz.com/files/pic/pic9/201502/apic9702.jpg");

                        shareParmas.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imgUrls);

                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Log.d(TAG, " share to qqzone ");
                                tencent.shareToQzone((Activity) context, shareParmas, new QQZoonBaseUiListener());
                            }
                        }).start();

                        break;
                    case R.id.img_search_dating_detail_share_qqweibo:
                        // 用于实现腾讯微博的分享
                        if (!tencent.isSessionValid()) {
                            tencent.login((Activity) context, "all", new QQWeiboBaseUiListener()
                            {
                                @Override
                                protected void doComplete(JSONObject response)
                                {
                                    try {
                                        String token = response.getString(Constants.PARAM_ACCESS_TOKEN);
                                        String expires = response.getString(Constants.PARAM_EXPIRES_IN);
                                        String openId = response.getString(Constants.PARAM_OPEN_ID);
                                        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires) && !TextUtils.isEmpty(openId)) {
                                            tencent.setAccessToken(token, expires);
                                            tencent.setOpenId(openId);
                                        }
                                        final Weibo weibo = new Weibo(context, tencent.getQQToken());
                                        final String sharedContent = context.getString(R.string.renren_share_content);
                                        weibo.sendText(sharedContent, tencentWeiboListener);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                        // 当前的Session是合法的
                        final boolean ready = tencent.isSessionValid() && tencent.getQQToken().getOpenId() != null;
                        if (!ready) {
                            Toast.makeText(context, context.getString(R.string.need_to_login_first), Toast.LENGTH_SHORT).show();
                        } else {
                            final Weibo weibo = new Weibo(context, tencent.getQQToken());
                            String content = context.getString(R.string.renren_share_content) + System.currentTimeMillis();
                            weibo.sendText(content, tencentWeiboListener);
                            Toast.makeText(context, context.getString(R.string.tencent_weibo_share_success), Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case R.id.img_search_dating_detail_share_sinaweibo:
                        ((Activity) context).startActivity(new Intent((Activity) context, WeiboShareActionCompleteActivity.class));
                        break;
                    case R.id.img_search_dating_detail_share_renren:
                        if (null != renRenShareManager && renRenShareManager.isRenrenClientInstalled())
                        {
                            // 以下我们采用分享的是测试图片
                            Bitmap sharedBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
                            Log.d("renren_share", "Share to renren site ");
                            renRenShareManager.shareToRenren(context.getString(R.string.renren_share_content), sharedBitmap);
                        } else
                        {
                            // 人人客户端在没有安装时是无法支持正常分享的
                            Toast.makeText(context, context.getString(R.string.renren_need_to_install_first), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };
//        tvYueqiu.setOnClickListener(listener);
//        tvYueqiuCircle.setOnClickListener(listener);

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

    private static boolean isPackageInstalled(final String pkgName, Context context)
    {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, " exception happened : " + e.toString());
            return false;
        }
    }

    private static class QQZoonBaseUiListener implements IUiListener
    {

        /**
         * 以下的三个方法是用于监听腾讯的分享的回调监听方法
         */
        @Override
        public void onComplete(Object o)
        {
            Log.d(TAG, " share complete : " + o.toString());
        }

        @Override
        public void onError(UiError uiError)
        {
            Log.d(TAG, " share error : " + uiError.errorMessage + " error code : " + uiError.errorCode);
        }

        @Override
        public void onCancel()
        {
            Log.d(TAG, " share cancelled");

        }
    }


    private static class QQWeiboBaseUiListener implements IUiListener
    {
        @Override
        public void onComplete(Object response)
        {
            if (null == response) {
                return;
            }
            JSONObject jsonResponse = (JSONObject) response;
            if (null != jsonResponse && jsonResponse.length() == 0) {
                return;
            }
            doComplete((JSONObject) response);
        }

        protected void doComplete(JSONObject values)
        {

        }

        @Override
        public void onError(UiError uiError)
        {

        }

        @Override
        public void onCancel()
        {

        }
    }

    /**
     * dp转px
     *
     * @param context
     * @param value
     * @return
     */
    public static int dip2px(Context context, float value)
    {
        float scaleing = context.getResources().getDisplayMetrics().density;
        return (int) (value * scaleing + 0.5f);
    }

    /**
     * px转dp
     *
     * @param context
     * @param value
     * @return
     */
    public static int px2dip(Context context, float value)
    {
        float scaling = context.getResources().getDisplayMetrics().density;
        return (int) (value / scaling + 0.5f);
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
    public static void showToast(Context context, String msg)
    {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static <T extends View> T $(Activity activity, int resId)
    {
        T t = (T) activity.findViewById(resId);
        return t;
    }

    /**
     * 隐藏软键盘
     *
     * @param context
     * @param ed
     */
    public static void dismissInputMethod(Context context, EditText ed)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ed.getWindowToken(), 0);
    }
}

package com.yueqiu;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

import com.gotye.api.GotyeAPI;
import com.gotye.api.GotyeStatusCode;
import com.gotye.api.GotyeUser;
import com.gotye.api.listener.LoginListener;
import com.yueqiu.bean.FavorInfo;
import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.bean.Identity;
import com.yueqiu.bean.PlayIdentity;
import com.yueqiu.bean.PlayInfo;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.im.CrashApplication;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.HttpConstants;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.util.AppUtil;
import com.yueqiu.util.HttpUtil;
import com.yueqiu.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by wangyun on 15/1/4.
 */
public class YueQiuApp extends Application implements LoginListener {
    private static final String TAG = "YueQiuApp";
    public static UserInfo sUserInfo = new UserInfo();
    private SharedPreferences mSharedPreferences;

    // 由于Volley的官方推荐构建方式是定义成全局的Singleton模式，用于保存唯一的RequestQueue来加速图片的加载，所以我们在这里创建了全局的Context
    private static Context sAppContext;

    /**
     * 用于存放从数据库中查询到的全部Group信息
     */
    public static Map<Integer, GroupNoteInfo> sGroupDbMap = new LinkedHashMap<Integer, GroupNoteInfo>();
    /**
     * 用于存放从数据库中查询到的全部Publish信息
     */
    public static Map<Identity, PublishedInfo> sPublishMap = new LinkedHashMap<Identity, PublishedInfo>();

    /**
     * 用于存放从数据库中查询得到的全部Favor信息
     */
    public static Map<Identity, FavorInfo> sFavorMap = new LinkedHashMap<Identity, FavorInfo>();

    /**
     * 用于存放数据库中查询得到的全部Activitie信息
     */
    public static Map<PlayIdentity, PlayInfo> sPlayMap = new LinkedHashMap<PlayIdentity, PlayInfo>();


    //IM APPKEY
    public static final String APPKEY = "007b7931-bd77-4aec-876f-47f6f9b58db2";
    public static final String PACKAGENAME = "com.yueqiu";


    //登录状态
    public static final int LOGOUT_SUCCESS = 0;
    public static final int LOGOUT_FAILED = 1;

    public static int sKeyboardHeight;

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case YueQiuApp.LOGOUT_SUCCESS:
                    resetUSerInfo();
                    jumpToIndexPage();
                    Utils.showToast(getAppContext(), getAppContext().getString(R.string.logout_success));
                    break;
                case YueQiuApp.LOGOUT_FAILED:
                    Utils.showToast(getAppContext(), getAppContext().getString(R.string.logout_failed));
                    break;
            }
        }
    };

    public void resetUSerInfo() {
        YueQiuApp.sUserInfo.setImg_url("");
        YueQiuApp.sUserInfo.setUsername(getAppContext().getString(R.string.guest));
        YueQiuApp.sUserInfo.setUser_id(0);
        YueQiuApp.sUserInfo.setPhone("");
        YueQiuApp.sUserInfo.setNick("");
        YueQiuApp.sUserInfo.setDistrict("");
        YueQiuApp.sUserInfo.setLevel(-1);
        YueQiuApp.sUserInfo.setBall_type(-1);
        YueQiuApp.sUserInfo.setBallArm(-1);
        YueQiuApp.sUserInfo.setUsedType(-1);
        YueQiuApp.sUserInfo.setBallAge("");
        YueQiuApp.sUserInfo.setIdol("");
        YueQiuApp.sUserInfo.setIdol_name("");

    }

    private void jumpToIndexPage() {
        String str = AppUtil.getCurrentActivityName(sAppContext);
//        if (!str.equals("com.yueqiu.BilliardNearbyActivity")) {
            Intent intent = new Intent(this, BilliardNearbyActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
//        } else {
//            //更新首页的UI
//
//        }

    }

    @Override
    public void onCreate() {
        super.onCreate();

        //以下两句用于IM
        //异常拦截记录
        CrashApplication.getInstance(this).onCreate();
        //初始化
        GotyeAPI.getInstance().init(getApplicationContext(), APPKEY, PACKAGENAME);

        mSharedPreferences = getSharedPreferences(PublicConstant.USERBASEUSER, Context.MODE_PRIVATE);

        sUserInfo.setUsername(mSharedPreferences.getString(DatabaseConstant.UserTable.USERNAME, getString(R.string.guest)));
        sUserInfo.setUser_id(Integer.valueOf(mSharedPreferences.getString(DatabaseConstant.UserTable.USER_ID, "0")));
        sUserInfo.setImg_url(mSharedPreferences.getString(DatabaseConstant.UserTable.IMG_URL, ""));
        sUserInfo.setTitle(mSharedPreferences.getString(DatabaseConstant.UserTable.TITLE, getString(R.string.nearby_billiard_mate_str)));
        sUserInfo.setPhone(mSharedPreferences.getString(DatabaseConstant.UserTable.PHONE, ""));

        sAppContext = getApplicationContext();
        registerListener();
    }

    public static Context getAppContext() {
        return sAppContext;
    }

    public void logout() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(DatabaseConstant.UserTable.USER_ID, String.valueOf(YueQiuApp.sUserInfo.getUser_id()));
        String result = HttpUtil.urlClient(HttpConstants.LogoutConstant.URL,
                map, HttpConstants.RequestMethod.GET);
        try {
            JSONObject resultJson = new JSONObject(result);
            int rtCode = resultJson.getInt("code");
            if (rtCode == HttpConstants.ResponseCode.NORMAL) {
                mHandler.sendEmptyMessage(YueQiuApp.LOGOUT_SUCCESS);
            } else {
                mHandler.sendEmptyMessage(YueQiuApp.LOGOUT_FAILED);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * IM 登出
     *
     * @param code 状态码 参见 {@link com.gotye.api.GotyeStatusCode}
     */
    @Override
    public void onLogout(int code) {
        if (YueQiuApp.sUserInfo.getUser_id() == 0)
            return;//已经退出
        if (code == GotyeStatusCode.CODE_FORCELOGOUT) {
            if (Utils.networkAvaiable(this)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        logout();
                    }
                }).start();
            } else {
                Toast.makeText(this, getString(R.string.network_not_available), Toast.LENGTH_SHORT).show();
            }
//            registerListener();
            Toast.makeText(this, getString(R.string.im_login_other_device), Toast.LENGTH_SHORT).show();
        } else if (code == GotyeStatusCode.CODE_NETWORD_DISCONNECTED) {
            Toast.makeText(this, getString(R.string.im_user_offline), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * IM 登录
     *
     * @param code             状态码 参见 {@link com.gotye.api.GotyeStatusCode}
     * @param currentLoginUser 当前登录用户
     */
    @Override
    public void onLogin(int code, GotyeUser currentLoginUser) {
    }

    public void registerListener() {
        GotyeAPI.getInstance().addListerer(this);
    }

}

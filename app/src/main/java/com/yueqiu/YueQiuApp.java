package com.yueqiu;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.gotye.api.GotyeAPI;
import com.yueqiu.bean.FavorInfo;
import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.bean.Identity;
import com.yueqiu.bean.PlayIdentity;
import com.yueqiu.bean.PlayInfo;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.chatbar.CrashApplication;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.PublicConstant;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by wangyun on 15/1/4.
 */
public class YueQiuApp extends Application {

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


    public static final String APPKEY = "007b7931-bd77-4aec-876f-47f6f9b58db2";
    public static final String PACKAGENAME = "com.yueqiu";


    @Override
    public void onCreate() {
        super.onCreate();

        //异常拦截记录
        CrashApplication.getInstance(this).onCreate();
        //初始化
        GotyeAPI.getInstance().init(getApplicationContext(), APPKEY, PACKAGENAME);

        mSharedPreferences = getSharedPreferences(PublicConstant.USERBASEUSER, Context.MODE_PRIVATE);

        sUserInfo.setUsername(mSharedPreferences.getString(DatabaseConstant.UserTable.USERNAME, getString(R.string.guest)));
        sUserInfo.setUser_id(Integer.valueOf(mSharedPreferences.getString(DatabaseConstant.UserTable.USER_ID, "0")));
        sUserInfo.setImg_url(mSharedPreferences.getString(DatabaseConstant.UserTable.IMG_URL, ""));
        sUserInfo.setTitle(mSharedPreferences.getString(DatabaseConstant.UserTable.TITLE, getString(R.string.search_billiard_mate_str)));
        sUserInfo.setPhone(mSharedPreferences.getString(DatabaseConstant.UserTable.PHONE, ""));

        sAppContext = getApplicationContext();

    }

    public static Context getAppContext() {
        return sAppContext;
    }


}

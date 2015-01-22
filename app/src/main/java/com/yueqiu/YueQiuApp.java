package com.yueqiu;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.yueqiu.bean.FavorInfo;
import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.bean.Identity;
import com.yueqiu.bean.PublishedInfo;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.dao.DaoFactory;
import com.yueqiu.dao.PublishedDao;
import com.yueqiu.db.DBUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by wangyun on 15/1/4.
 */
public class YueQiuApp extends Application
{

    public static UserInfo sUserInfo = new UserInfo();
    private SharedPreferences mSharedPreferences;

    // 由于Volley的官方推荐构建方式是定义成全局的Singleton模式，用于保存唯一的RequestQueue来加速图片的加载，所以我们在这里创建了全局的Context
    private static Context sAppContext;

    /**
     * 用于存放从数据库中查询到的全部Group信息
     */
    public static Map<Integer,GroupNoteInfo> sGroupDbMap = new LinkedHashMap<Integer, GroupNoteInfo>();
    /**
     * 用于存放从数据库中查询到的全部Publish信息
     */
    public static Map<Identity,PublishedInfo> sPublishMap = new LinkedHashMap<Identity, PublishedInfo>();

    /**
     * 用于存放从数据库中查询得到的全部Favor信息
     */
    public static Map<Identity,FavorInfo> sFavorMap = new LinkedHashMap<Identity, FavorInfo>();

    @Override
    public void onCreate()
    {
        super.onCreate();

        mSharedPreferences = getSharedPreferences(PublicConstant.USERBASEUSER, Context.MODE_PRIVATE);

        sUserInfo.setUsername(mSharedPreferences.getString(DatabaseConstant.UserTable.USERNAME, getString(R.string.guest)));
        sUserInfo.setUser_id(Integer.valueOf(mSharedPreferences.getString(DatabaseConstant.UserTable.USER_ID, "0")));
        sUserInfo.setImg_url(mSharedPreferences.getString(DatabaseConstant.UserTable.IMG_URL, ""));
        sUserInfo.setTitle(mSharedPreferences.getString(DatabaseConstant.UserTable.TITLE, getString(R.string.search_billiard_mate_str)));
        sUserInfo.setPhone(mSharedPreferences.getString(DatabaseConstant.UserTable.PHONE, ""));

        sAppContext = getApplicationContext();

    }

    public static Context getAppContext()
    {
        return sAppContext;
    }


}

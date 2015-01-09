package com.yueqiu;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.DatabaseConstant;
import com.yueqiu.constant.PublicConstant;
import com.yueqiu.db.DBUtils;

/**
 * Created by wangyun on 15/1/4.
 */
public class YueQiuApp extends Application {

    public static UserInfo sUserInfo = new UserInfo();
    public static GroupNoteInfo sGroupInfo = new GroupNoteInfo();
    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        mSharedPreferences = getSharedPreferences(PublicConstant.USERBASEUSER,Context.MODE_PRIVATE);
        sUserInfo.setAccount(mSharedPreferences.getString(DatabaseConstant.UserTable.ACCOUNT,getString(R.string.guest)));
        sUserInfo.setUser_id(Integer.valueOf(mSharedPreferences.getString(DatabaseConstant.UserTable.USER_ID,"0")));
        sUserInfo.setImg_url(mSharedPreferences.getString(DatabaseConstant.UserTable.IMG_URL,""));
        sUserInfo.setTitle(mSharedPreferences.getString(DatabaseConstant.UserTable.TITLE,getString(R.string.search_billiard_mate_str)));
        sUserInfo.setPhone(mSharedPreferences.getString(DatabaseConstant.UserTable.PHONE,""));

    }

}

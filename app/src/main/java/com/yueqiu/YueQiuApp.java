package com.yueqiu;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.yueqiu.bean.GroupNoteInfo;
import com.yueqiu.bean.UserInfo;
import com.yueqiu.constant.PublicConstant;

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
        sUserInfo.setAccount(mSharedPreferences.getString(PublicConstant.USER_NAME,getString(R.string.guest)));
        sUserInfo.setUser_id(Integer.valueOf(mSharedPreferences.getString(PublicConstant.USER_ID,"0")));
        sUserInfo.setImg_url(mSharedPreferences.getString(PublicConstant.IMG_URL,""));
        sUserInfo.setTitle(mSharedPreferences.getString(PublicConstant.TITLE,getString(R.string.search_billiard_mate_str)));
        sUserInfo.setPhone(mSharedPreferences.getString(PublicConstant.PHONE,""));



    }

}

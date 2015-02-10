package com.yueqiu.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by scguo on 14/12/30.
 *
 * 用于展示SearchActivity当中约球Fragment当中的ListView的具体条目展开之后
 * 的Activity当中对应的“已参加人员”的GridView当中的列出来的表格信息
 *
 */
public class NearbyDatingDetailedAlreadyBean
{
    private String mUserId;
    private String mUserPhoto;
    private String mUserName;

    public NearbyDatingDetailedAlreadyBean(String userId, String photo, String name)
    {
        this.mUserId = userId;
        this.mUserPhoto = photo;
        this.mUserName = name;
    }

    public String getUserId()
    {
        return mUserId;
    }

    public void setUserId(String id)
    {
        this.mUserId = id;
    }

    public String getUserPhoto()
    {
        return mUserPhoto;
    }

    public void setUserPhoto(String photo)
    {
        this.mUserPhoto = photo;
    }

    public String getUserName()
    {
        return mUserName;
    }

    public void setUserName(String name)
    {
        this.mUserName = name;
    }

    // TODO: 当前服务器端还没有具体的定义每一个参加活动的人员的具体的需要具备的字段的定义的值
    public NearbyDatingDetailedAlreadyBean(JSONObject jsonObject)
    {
        try {
            this.mUserName = jsonObject.getString("");
            this.mUserPhoto = jsonObject.getString("");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}

package com.yueqiu.bean;

import android.widget.BaseExpandableListAdapter;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by scguo on 14/12/25.
 *
 * 用于SearchActivity当中的教练的Bean文件
 */
public class SearchCoauchSubFragmentCoauchBean
{
    private String mUserPhoto;
    private String mUserName;
    private String mUserGender;
    private String mUserDistance;
    private String mUserLevel;
    private String mBilliardKind;

    public SearchCoauchSubFragmentCoauchBean(String photo, String name, String gender, String distance, String level, String kind)
    {
        this.mUserPhoto = photo;
        this.mUserName = name;
        this.mUserGender = gender;
        this.mUserDistance = distance;
        this.mUserLevel = level;
        this.mBilliardKind = kind;

    }

    public String getUserPhoto()
    {
        return mUserPhoto;
    }

    public void setUserPhoto(String photo)
    {
        this.mUserPhoto = photo;
    }

    public String getUserGender()
    {
        return mUserGender;
    }

    public void setUserGender(String gender)
    {
        this.mUserGender = gender;
    }

    public String getUserDistance()
    {
        return mUserDistance;
    }

    public void setUserDistance(String distance)
    {
        this.mUserDistance = distance;
    }

    public String getUserLevel()
    {
        return mUserLevel;
    }

    public void setUserLevel(String level)
    {
        this.mUserLevel = level;
    }

    public String getmBilliardKind()
    {
        return mBilliardKind;
    }

    public void setBilliardKind(String billiardKind)
    {
        this.mBilliardKind = billiardKind;
    }

    public String getUserName()
    {
        return mUserName;
    }

    public void setUserName(String userName)
    {
        this.mUserName = userName;
    }


    // 提供将我们得到的Json数据转换成Java bean的util方法
    private static final String JSON_IMG_URL = "img_url"; // 头像的url
    private static final String JSON_RANGE = "range"; // 距离多少米
    private static final String JSON_LEVEL = "level"; // 资质
    private static final String JSON_CLASS = "class"; // 球种
    private static final String JSON_SEX = "sex"; // 性别
    private static final String JSON_USERNAME = "username"; // 昵称
    private static final String JSON_USER_ID = "user_id"; // 用户id(这个字段我们在这里暂时用不到)

    public JSONObject toJson() throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_USERNAME, mUserName);
        jsonObject.put(JSON_SEX, mUserGender);
        jsonObject.put(JSON_LEVEL, mUserLevel);
        jsonObject.put(JSON_IMG_URL, mUserPhoto);
        jsonObject.put(JSON_CLASS, mBilliardKind);
        jsonObject.put(JSON_RANGE, mUserDistance);

        return jsonObject;
    }

    // TODO: 其实我们是可以直接使用GSON的，这样我们就不用每一个Java bean文件都对应于一个单独的将Json数据解析成Javabean的方法
    // 将json数据解析成我们这里教练所对应的bean文件
    public SearchCoauchSubFragmentCoauchBean parseJson(JSONObject jsonObject) throws JSONException
    {
        mUserPhoto = String.valueOf(jsonObject.get(JSON_IMG_URL));
        mUserName = String.valueOf(jsonObject.get(JSON_USERNAME));
        mUserGender = String.valueOf(jsonObject.get(JSON_SEX));
        mUserDistance = String.valueOf(jsonObject.get(JSON_RANGE));
        mUserLevel = String.valueOf(jsonObject.get(JSON_LEVEL));
        mBilliardKind = String.valueOf(jsonObject.get(JSON_CLASS));

        return new SearchCoauchSubFragmentCoauchBean(mUserPhoto, mUserName, mUserGender, mUserDistance, mUserLevel, mBilliardKind);
    }

}

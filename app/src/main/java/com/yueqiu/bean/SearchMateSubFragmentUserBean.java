package com.yueqiu.bean;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.font.TextAttribute;

/**
 * Created by scguo on 14/12/25.
 * <p/>
 * 这是用于创建SearchActivity当中的球友Fragment当中的用户的Bean
 * 主要是用于创建ListView当中的item
 */
public class SearchMateSubFragmentUserBean
{
    private String mUserNickName;
    private String mUserGender;
    private String mUserDistrict;
    private String mUserDistance;

    // the url for the user photo image
    // and we use this Url to get the user photo directly, without through some
    // other http request
    private String mUserPhotoUrl;

    public SearchMateSubFragmentUserBean()
    {}

    public SearchMateSubFragmentUserBean(String userPhotoUrl, String nickName, String userGender, String userDistrict, String userDistance)
    {
        this.mUserDistance = userDistance;
        this.mUserPhotoUrl = userPhotoUrl;
        this.mUserDistrict = userDistrict;
        this.mUserGender = userGender;
        this.mUserNickName = nickName;
    }

    public void setUserNickName(String name)
    {
        this.mUserNickName = name;
    }

    public void setUserGender(String gender)
    {
        this.mUserGender = gender;
    }

    public void setUserDistrict(String district)
    {
        this.mUserDistrict = district;
    }

    public void setUserDistance(String distance)
    {
        this.mUserDistance = distance;
    }

    public void setUserPhotoUrl(String url)
    {
        this.mUserPhotoUrl = url;
    }


    public String getUserNickName()
    {
        return mUserNickName;
    }

    public String getUserGender()
    {
        return mUserGender;
    }

    public String getUserDistrict()
    {
        return mUserDistrict;
    }

    public String getUserDistance()
    {
        return mUserDistance;
    }

    public String getUserPhotoUrl()
    {
        return mUserPhotoUrl;
    }

    private static final String JSON_USER_ID = "user_id";
    private static final String JSON_SEX = "sex";
    private static final String JSON_RANGE = "range";
    private static final String JSON_DISTRICT = "district";
    private static final String JSON_IMG_URL = "img_url";
    private static final String JSON_USERNAME = "username";

    public JSONObject toJson() throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_USERNAME, mUserNickName);
        jsonObject.put(JSON_DISTRICT, mUserDistrict);
        jsonObject.put(JSON_SEX, mUserGender);
        jsonObject.put(JSON_IMG_URL, mUserPhotoUrl);
        jsonObject.put(JSON_RANGE, mUserDistance);

        return jsonObject;
    }

    public SearchMateSubFragmentUserBean(JSONObject jsonObject) throws JSONException
    {
        Log.d("search_mate_bean", "the json object we need to convert are : " + jsonObject.toString());
        mUserPhotoUrl = jsonObject.getString(JSON_IMG_URL);
//        Log.d("search_mate_bean", "the user photo are : " + mUserPhotoUrl);
        mUserDistrict = jsonObject.getString("district");
        Log.d("search_mate_bean", "the user district are : " + mUserDistrict);
        mUserGender = jsonObject.getString(JSON_SEX);
        Log.d("search_mate_bean", " the sex : " + mUserGender);
        mUserNickName = jsonObject.getString(JSON_USERNAME);
        Log.d("search_mate_bean", " the name : " + mUserNickName);
        mUserDistance = jsonObject.getString(JSON_RANGE);
        Log.d("search_mate_bean", " the range are : " + mUserDistance);



    }

    @Override
    public String toString()
    {
        return "the photo url are :" + mUserPhotoUrl + "; name : " + mUserNickName + "; gender: " + mUserGender +
                "; district : " + mUserDistrict + "; and distance : " + mUserDistance;
    }
}
























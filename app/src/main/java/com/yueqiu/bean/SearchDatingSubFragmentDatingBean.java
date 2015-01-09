package com.yueqiu.bean;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by scguo on 14/12/25.
 *
 * 这是用于创建SearchActivity当中的约球Fragment当中的用户的Bean
 * 主要是用于创建ListView当中的item
 *
 */
public class SearchDatingSubFragmentDatingBean
{
    private String mUserPhoto;
    private String mUserName;
    private String mUserDeclare;
    private String mUserDistance;

    public SearchDatingSubFragmentDatingBean(String photo, String name, String declare, String distance)
    {
        this.mUserDeclare = declare;
        this.mUserPhoto = photo;
        this.mUserDistance = distance;
        this.mUserName = name;
    }


    public String getUserPhoto()
    {
        return mUserPhoto;
    }

    public void setUserPhoto(String userPhoto)
    {
        this.mUserPhoto = userPhoto;
    }

    public String getUserName()
    {
        return mUserName;
    }

    public void setUserName(String name)
    {
        this.mUserName = name;
    }

    public String getUserDeclare()
    {
        return mUserDeclare;
    }

    public void setUserDeclare(String declare)
    {
        this.mUserDeclare = declare;
    }

    public String getUserDistance()
    {
        return mUserDistance;
    }

    public void setUserDistance(String distance)
    {
        this.mUserDistance = distance;
    }


    private static final String JSON_USER_ID = "user_id";
    private static final String JSON_USERNAME = "username";
    private static final String JSON_CONTENT = "content";
    private static final String JSON_RANGE = "range";
    private static final String JSON_IMG_URL = "img_url";

    public JSONObject toJson() throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_USERNAME, mUserName);
        jsonObject.put(JSON_CONTENT, mUserDeclare);
        jsonObject.put(JSON_RANGE, mUserDistance);
        jsonObject.put(JSON_IMG_URL, mUserPhoto);

        return jsonObject;
    }

    public SearchDatingSubFragmentDatingBean parseJson(JSONObject jsonObject) throws JSONException
    {
        mUserPhoto = String.valueOf(jsonObject.get(JSON_IMG_URL));
        mUserName = String.valueOf(jsonObject.get(JSON_USERNAME));
        mUserDeclare = String.valueOf(jsonObject.get(JSON_CONTENT));
        mUserDistance = String.valueOf(jsonObject.get(JSON_RANGE));

        return new SearchDatingSubFragmentDatingBean(mUserPhoto, mUserName, mUserDeclare, mUserDistance);
    }


}

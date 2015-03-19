package com.yueqiu.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by scguo on 14/12/25.
 * <p/>
 * 这是用于创建SearchActivity当中的球友Fragment当中的用户的Bean
 * 主要是用于创建ListView当中的item
 */
public class NearbyMateSubFragmentUserBean implements Parcelable
{
    private static final String TAG = "NearbyMateSubFragmentUserBean";

    private String mUserId;
    private String mUserNickName;
    private String mUserGender;
    private String mUserDistrict;
    private String mUserDistance;

    // the url for the user photo image
    // and we use this Url to get the user photo directly, without through some
    // other http request
    private String mUserPhotoUrl;

    public NearbyMateSubFragmentUserBean()
    {}

    public NearbyMateSubFragmentUserBean(String userId, String userPhotoUrl, String nickName, String userGender, String userDistrict, String userDistance)
    {
        this.mUserId = userId;
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

    public String getUserId()
    {
        return mUserId;
    }

    public void setUserId(String userId)
    {
        this.mUserId = userId;
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

    public NearbyMateSubFragmentUserBean(JSONObject jsonObject) throws JSONException
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

    @Override
    public int hashCode()
    {
        int hash = 3;
        try
        {
            hash = 17 * mUserId.hashCode();
        } catch (final Exception e)
        {
            Log.d(TAG, " exception happened while we parse the iD value we get : " + e.toString());
        }
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        boolean result = false;

        if (object == null ||  object.getClass() != this.getClass())
        {
            result = false;
        } else
        {
            NearbyMateSubFragmentUserBean thatObj = (NearbyMateSubFragmentUserBean) object;
            if (thatObj.getUserId().equals(this.getUserId()))
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents()
    {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mUserId);
        dest.writeString(mUserDistance);
        dest.writeString(mUserPhotoUrl);
        dest.writeString(mUserDistrict);
        dest.writeString(mUserGender);
        dest.writeString(mUserNickName);
    }

    public static final Creator<NearbyMateSubFragmentUserBean> CREATOR = new Creator<NearbyMateSubFragmentUserBean>()
    {
        @Override
        public NearbyMateSubFragmentUserBean createFromParcel(Parcel source)
        {
            return new NearbyMateSubFragmentUserBean(source);
        }

        @Override
        public NearbyMateSubFragmentUserBean[] newArray(int size)
        {
            return new NearbyMateSubFragmentUserBean[size];
        }
    };

    public NearbyMateSubFragmentUserBean(Parcel savedData)
    {
        // 我们读取的顺序需要同我们写入Parcel的顺序保持一致，否则我们读到的值就会是乱的
        // 之所以要保持顺序一致是因为我们写入Parcel时并没有根据相应的KEY值来进行插入，所以获取时要按照
        // 最原始的顺序来确定我们读到的值是正确的
        mUserId = savedData.readString();
        mUserDistance = savedData.readString();
        mUserPhotoUrl = savedData.readString();
        mUserDistrict = savedData.readString();
        mUserGender = savedData.readString();
        mUserNickName = savedData.readString();
    }

}
























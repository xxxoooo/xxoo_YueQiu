package com.yueqiu.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.font.TextAttribute;

/**
 * Created by scguo on 14/12/25.
 *
 * 这是用于创建SearchActivity当中的助教Fragment当中的助教的Bean
 * 主要是用于创建ListView当中的item
 */
public class NearbyAssistCoauchSubFragmentBean implements Parcelable
{
    private static final String TAG = "NearbyAssistCoauchSubFragmentBean";

    private String mUserId;
    private String mPhoto;
    private String mName;
    private String mGender;
    // 用于教练的球种
    private String mKinds;
    private String mDistance;

    private String mPrice;

    public NearbyAssistCoauchSubFragmentBean(){}

    public NearbyAssistCoauchSubFragmentBean(String userId, String photo, String name, String gender, String kinds, String price, String distan)
    {
        this.mUserId = userId;
        this.mPhoto = photo;
        this.mName = name;
        this.mGender = gender;
        this.mKinds = kinds;
        this.mDistance = distan;
        this.mPrice = price;
    }

    public String getDistance()
    {
        return mDistance;
    }

    public void setDistance(String dist)
    {
        this.mDistance = dist;
    }

    public String getKinds()
    {
        return mKinds;
    }

    public void setKinds(String kinds)
    {
        this.mKinds = kinds;
    }

    public String getPrice()
    {
        return mPrice;
    }

    public void setPrice(String price)
    {
        this.mPrice = price;
    }

    public String getGender()
    {
        return mGender;
    }

    public void setGender(String gender)
    {
        this.mGender = gender;
    }

    public String getPhoto()
    {
        return mPhoto;
    }

    public void setPhoto(String photo)
    {
        this.mPhoto = photo;
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        this.mName = name;
    }

    private static final String JSON_USER_ID = "user_id"; // 用户id(这个我们在解析成java bean对象的过程当中暂时是用不到的)
    private static final String JSON_USER_NAME = "username"; // 用户名
    private static final String JSON_SEX = "sex"; // 用户性别
    private static final String JSON_CLASS = "class"; // 球种
    private static final String JSON_MONEY = "money"; // 费用
    private static final String JSON_RANGE = "range"; // 距离多少米
    private static final String JSON_IMG_URL = "img_url"; // 头像


    public JSONObject toJson() throws JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_USER_NAME, mName);
        jsonObject.put(JSON_SEX, mGender);
        jsonObject.put(JSON_CLASS, mKinds);
        jsonObject.put(JSON_IMG_URL, mPhoto);
        jsonObject.put(JSON_MONEY, mPrice);
        jsonObject.put(JSON_RANGE, mDistance);

        return jsonObject;
    }

    public NearbyAssistCoauchSubFragmentBean parseJson(JSONObject jsonObject) throws JSONException
    {
        // String photo, String name, String gender, String kinds, String price, String distan
        mUserId = String.valueOf(jsonObject.get(JSON_USER_ID));
        mPhoto = String.valueOf(jsonObject.get(JSON_IMG_URL));
        mName = String.valueOf(jsonObject.get(JSON_USER_NAME));
        mGender = String.valueOf(jsonObject.get(JSON_SEX));
        mKinds = String.valueOf(jsonObject.get(JSON_CLASS));
        mPrice = String.valueOf(jsonObject.get(JSON_MONEY));
        mDistance = String.valueOf(jsonObject.get(JSON_RANGE));

        return new NearbyAssistCoauchSubFragmentBean(mUserId, mPhoto, mName, mGender, mKinds, mPrice, mDistance);
    }


    public String getUserId()
    {
        return mUserId;
    }

    public void setUserId(String userId)
    {
        this.mUserId = userId;
    }

    // TODO: 我们现在的设计当中暂时还没有涉及到关于set的数据结构，我们现在只是用List来
    // TODO: 存储我们从网络上获取到的数据，如果我们以后采用Set来存储我们的数据，那么我们需要好好设计一下我们的HashCode方法
    @Override
    public int hashCode()
    {
        int hash = 3;
        try
        {
            int userId = Integer.parseInt(this.getUserId());
            Log.d(TAG, "the original user id are : " + userId);
            hash *= userId;
        } catch (final Exception e)
        {
            Log.d(TAG, " exception happened while we parse the userid from the userBean, and the reason goes to : " + e.toString());
        }
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        boolean result = false;
        if (object == null || object.getClass() != this.getClass())
        {
            result = false;
        } else
        {
            NearbyAssistCoauchSubFragmentBean thatObj = (NearbyAssistCoauchSubFragmentBean) object;
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
        dest.writeString(mPhoto);
        dest.writeString(mName);
        dest.writeString(mGender);
        dest.writeString(mKinds);
        dest.writeString(mDistance);
        dest.writeString(mPrice);
    }

    public static final Creator<NearbyAssistCoauchSubFragmentBean> CREATOR = new Creator<NearbyAssistCoauchSubFragmentBean>()
    {
        @Override
        public NearbyAssistCoauchSubFragmentBean createFromParcel(Parcel source)
        {
            return new NearbyAssistCoauchSubFragmentBean(source);
        }

        @Override
        public NearbyAssistCoauchSubFragmentBean[] newArray(int size)
        {
            return new NearbyAssistCoauchSubFragmentBean[size];
        }
    };

    public NearbyAssistCoauchSubFragmentBean(Parcel source)
    {
        this.mUserId = source.readString();
        this.mPhoto = source.readString();
        this.mName = source.readString();
        this.mGender = source.readString();
        this.mKinds = source.readString();
        this.mDistance = source.readString();
        this.mPrice = source.readString();
    }



}

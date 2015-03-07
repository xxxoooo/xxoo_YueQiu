package com.yueqiu.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by scguo on 14/12/25.
 *
 * 用于SearchActivity当中的教练的Bean文件
 */
public class NearbyCoauchSubFragmentCoauchBean implements Parcelable
{
    private static final String TAG = "NearbyCoauchSubFragmentCoauchBean";

    private String mId;
    private String mUserPhoto;
    private String mUserName;
    private String mUserGender;
    private String mUserDistance;
    private String mUserLevel;
    private String mBilliardKind;
    private String mDistrict;

    public NearbyCoauchSubFragmentCoauchBean(){}

    public NearbyCoauchSubFragmentCoauchBean(String id, String photo, String name, String gender, String distance, String level, String kind,String district)
    {
        this.mId = id;
        this.mUserPhoto = photo;
        this.mUserName = name;
        this.mUserGender = gender;
        this.mUserDistance = distance;
        this.mUserLevel = level;
        this.mBilliardKind = kind;
        this.mDistrict = district;
    }

    public String getId()
    {
        return mId;
    }

    public void setId(String id)
    {
        this.mId = id;
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

    public String getDistrict() {
        return mDistrict;
    }

    public void setDistrict(String district) {
        this.mDistrict = district;
    }

    // 提供将我们得到的Json数据转换成Java bean的util方法
    private static final String JSON_IMG_URL = "img_url"; // 头像的url
    private static final String JSON_RANGE = "range"; // 距离多少米
    private static final String JSON_LEVEL = "level"; // 资质
    private static final String JSON_CLASS = "class"; // 球种
    private static final String JSON_SEX = "sex"; // 性别
    private static final String JSON_USERNAME = "username"; // 昵称
    private static final String JSON_USER_ID = "user_id"; // 用户id(这个字段我们在这里暂时用不到)
    private static final String JSON_DISTRICT = "district";

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
    public NearbyCoauchSubFragmentCoauchBean parseJson(JSONObject jsonObject) throws JSONException
    {
        mId = String.valueOf(jsonObject.get(JSON_USER_ID));
        mUserPhoto = String.valueOf(jsonObject.get(JSON_IMG_URL));
        mUserName = String.valueOf(jsonObject.get(JSON_USERNAME));
        mUserGender = String.valueOf(jsonObject.get(JSON_SEX));
        mUserDistance = String.valueOf(jsonObject.get(JSON_RANGE));
        mUserLevel = String.valueOf(jsonObject.get(JSON_LEVEL));
        mBilliardKind = String.valueOf(jsonObject.get(JSON_CLASS));
        mDistrict = String.valueOf(jsonObject.get(JSON_DISTRICT));
        return new NearbyCoauchSubFragmentCoauchBean(mId, mUserPhoto, mUserName, mUserGender, mUserDistance, mUserLevel, mBilliardKind,mDistrict);
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        try
        {
            int userId = Integer.parseInt(this.getId());
            hash *= userId;
        } catch (final Exception e)
        {
            Log.d(TAG, " exception happened while we parse the userId, " + e.toString());
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
            NearbyCoauchSubFragmentCoauchBean thatObj = (NearbyCoauchSubFragmentCoauchBean) object;
            if (thatObj.getId().equals(this.getId()) && thatObj.getUserPhoto().equals(this.getUserPhoto())
                    && thatObj.getUserName().equals(this.getUserName())
                    && thatObj.getUserDistance().equals(this.getUserDistance())
                    && thatObj.getmBilliardKind().equals(this.getmBilliardKind()))
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
        dest.writeString(mId);
        dest.writeString(mUserPhoto);
        dest.writeString(mUserName);
        dest.writeString(mUserGender);
        dest.writeString(mUserDistance);
        dest.writeString(mUserLevel);
        dest.writeString(mBilliardKind);
        dest.writeString(mDistrict);
    }

    public static final Creator<NearbyDatingSubFragmentDatingBean> CREATOR = new Creator<NearbyDatingSubFragmentDatingBean>()
    {
        @Override
        public NearbyDatingSubFragmentDatingBean createFromParcel(Parcel source)
        {
            return new NearbyDatingSubFragmentDatingBean(source);
        }

        @Override
        public NearbyDatingSubFragmentDatingBean[] newArray(int size)
        {
            return new NearbyDatingSubFragmentDatingBean[size];
        }
    };

    public NearbyCoauchSubFragmentCoauchBean(Parcel source)
    {
        this.mId = source.readString();
        this.mUserPhoto = source.readString();
        this.mUserName = source.readString();
        this.mUserGender = source.readString();
        this.mUserDistance = source.readString();
        this.mUserLevel = source.readString();
        this.mBilliardKind = source.readString();
        this.mDistrict = source.readString();
    }

}

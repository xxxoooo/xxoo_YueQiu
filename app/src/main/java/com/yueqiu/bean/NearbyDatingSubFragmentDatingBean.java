package com.yueqiu.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by scguo on 14/12/25.
 *
 * 这是用于创建SearchActivity当中的约球Fragment当中的用户的Bean
 * 主要是用于创建ListView当中的item
 *
 */
public class NearbyDatingSubFragmentDatingBean implements Parcelable
{
    private final static String TAG = "NearbyDatingSubFragmentDatingBean";

    private String mId;
    private String mUserPhoto;
    private String mUserName;
    private String mUserDeclare;
    private String mUserDistance;

    public NearbyDatingSubFragmentDatingBean(){}

    public NearbyDatingSubFragmentDatingBean(String id, String photo, String name, String declare, String distance)
    {
        this.mId = id;
        this.mUserDeclare = declare;
        this.mUserPhoto = photo;
        this.mUserDistance = distance;
        this.mUserName = name;
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


    private static final String JSON_USER_ID = "id";
    private static final String JSON_USERNAME = "username";
    private static final String JSON_CONTENT = "title";
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

    public NearbyDatingSubFragmentDatingBean parseJson(JSONObject jsonObject) throws JSONException
    {
        mId = String.valueOf(jsonObject.get(JSON_USER_ID));
        mUserPhoto = String.valueOf(jsonObject.get(JSON_IMG_URL));
        mUserName = String.valueOf(jsonObject.get(JSON_USERNAME));
        mUserDeclare = String.valueOf(jsonObject.get(JSON_CONTENT));
        mUserDistance = String.valueOf(jsonObject.get(JSON_RANGE));

        return new NearbyDatingSubFragmentDatingBean(mId, mUserPhoto, mUserName, mUserDeclare, mUserDistance);
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        try
        {
            hash = 17 * mId.hashCode();
            hash = 17 * mUserName.hashCode();
            hash = 17 * mUserDeclare.hashCode();
        } catch (final Exception e)
        {
            Log.d(TAG, " exception happened while parse the id value : " + e.toString());
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
            NearbyDatingSubFragmentDatingBean thatObj = (NearbyDatingSubFragmentDatingBean) object;
            if (thatObj.getId().equals(this.getId()) && thatObj.getUserName().equals(this.getUserName())
                    && thatObj.getUserDeclare().equals(this.getUserDeclare()))
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
        dest.writeString(mUserDeclare);
        dest.writeString(mUserPhoto);
        dest.writeString(mUserDistance);
        dest.writeString(mUserName);

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

    public NearbyDatingSubFragmentDatingBean(Parcel source)
    {
        this.mId = source.readString();
        this.mUserDeclare = source.readString();
        this.mUserPhoto = source.readString();
        this.mUserDistance = source.readString();
        this.mUserName = source.readString();
    }

}

package com.yueqiu.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.font.TextAttribute;
import java.io.StringReader;

/**
 * Created by scguo on 14/12/25.
 *
 * 用于控制每一个球厅显示的属性的Bean文件
 * 用于在球厅的ListView item当中的bean文件
 */
public class NearbyRoomSubFragmentRoomBean implements Parcelable
{
    private String mRoomId;
    private String mRoomPhotoUrl;
    private String mRoomName;
    private float mLevel;
    private double mPrice;
    private String mDetailedAddress;
    private String mDistance;

    // 以下增加的是用于球厅详情Activity当中需要的字段的内容(其实只是增加了三个字段，因为剩下的字段我们在前面都有获取)
    private String mRoomPhone;
    private String mRoomTag;
    private String mRoomInfo;
    private String mShopHours;

    // 用于展示球厅详情的url,我们需要通过这个url来加载一个WebView
    private String mRoomDetailePageUrl;

    public NearbyRoomSubFragmentRoomBean(){}

    public NearbyRoomSubFragmentRoomBean(String roomId, String roomPhoto, String roomName,
                                         float level, double price, String address, String distance,
                                         String roomPhone, String roomTag, String roomInfo,
                                         String shopHours, String roomDetailPageUrl)
    {
        this.mRoomId = roomId;
        this.mDetailedAddress = address;
        this.mDistance = distance;
        this.mLevel = level;
        this.mRoomPhotoUrl = roomPhoto;
        this.mPrice = price;
        this.mRoomName = roomName;
        this.mRoomPhone = roomPhone;
        this.mRoomTag = roomTag;
        this.mRoomInfo = roomInfo;
        this.mShopHours = shopHours;
        this.mRoomDetailePageUrl = roomDetailPageUrl;
    }

    /**
     * 这个构造方法用于商家推荐的球厅信息的点击反馈事件，可以看到比上面的那个构造方法少了一个参数，即少了用于
     * 展示球厅详情的WebView的url
     *
     */
    public NearbyRoomSubFragmentRoomBean(String roomId, String roomPhoto, String roomName,
                                         float level, double price, String address, String distance,
                                         String roomPhone, String roomTag, String roomInfo,
                                         String shopHours)
    {
        new NearbyRoomSubFragmentRoomBean(roomId, roomPhoto, roomName,
            level, price, address, distance,
            roomPhone, roomTag, roomInfo,
            shopHours, "");
    }

    public String getRoomDetailPageUrl()
    {
        return this.mRoomDetailePageUrl;
    }

    public void setRoomDetailPageUrl(String url)
    {
        this.mRoomDetailePageUrl = url;
    }


    public String getRoomPhotoUrl()
    {
        return mRoomPhotoUrl;
    }

    public void setRoomPhotoUrl(String roomPhotoUrl)
    {
        this.mRoomPhotoUrl = roomPhotoUrl;
    }

    public String getRoomName()
    {
        return mRoomName;
    }

    public void setRoomName(String roomName)
    {
        this.mRoomName = roomName;
    }

    public float getLevel()
    {
        return mLevel;
    }

    public void setLevel(float level)
    {
        this.mLevel = level;
    }

    public double getPrice()
    {
        return mPrice;
    }

    public void setPrice(double price)
    {
        this.mPrice = price;
    }

    public String getDetailedAddress()
    {
        return mDetailedAddress;
    }

    public void setDetailedAddress(String detailedAddress)
    {
        this.mDetailedAddress = detailedAddress;
    }

    public String getDistance()
    {
        return mDistance;
    }

    public void setDistance(String distance)
    {
        this.mDistance = distance;
    }

    // TODO: 这里需要注意的是，我们需要获取的是来自大众点评的字段数据，所以一下我们获取的并不是正确的。具体的数据的获取key值
    // TODO: 还需要同服务器端进行协商
    private static final String JSON_KEY_NAME = "name";
    private static final String JSON_KEY_PHOTO_URL = "img_url";
    private static final String JSON_KEY_LEVLE = "level"; // 星级的数目
    private static final String JSON_KEY_ADDRESS = "address"; // 详细地址
    private static final String JSON_KEY_RANGE = "range";
    private static final String JSON_KEY_PRICE = "price"; // 球厅的价格

    public NearbyRoomSubFragmentRoomBean(JSONObject initialJson) throws JSONException
    {
        this.mRoomName = initialJson.getString(JSON_KEY_NAME);
        this.mDetailedAddress = initialJson.getString(JSON_KEY_ADDRESS);
        this.mPrice = Double.parseDouble(initialJson.getString(JSON_KEY_PRICE));
        this.mLevel = Float.parseFloat(initialJson.getString(JSON_KEY_LEVLE));
        this.mDistance = initialJson.getString(JSON_KEY_RANGE);
        this.mRoomPhotoUrl = initialJson.getString(JSON_KEY_PHOTO_URL);
    }


    public String getRoomPhone()
    {
        return mRoomPhone;
    }

    public void setRoomPhone(String roomPhone)
    {
        this.mRoomPhone = roomPhone;
    }

    public String getRoomTag()
    {
        return mRoomTag;
    }

    public void setRoomTag(String roomTag)
    {
        this.mRoomTag = roomTag;
    }

    public String getRoomInfo()
    {
        return mRoomInfo;
    }

    public void setRoomInfo(String roomInfo)
    {
        this.mRoomInfo = roomInfo;
    }

    public String getRoomId()
    {
        return mRoomId;
    }

    public void setRoomId(String roomId)
    {
        this.mRoomId = roomId;
    }

    // 我们这里的HashCode的构建方法并不是最符合官方推荐的实现标准，但是用于实现
    // RoomBean List当中重复元素的判断已经可以了
    // 对于更为理想的实现，我们尽量还是参考<Effective Java>和Apache Commons当中实现的HashCodeBuilder的实现
    // 采用两个质数结合Bean文件当中的相关字段构建而成。相对很稳定
    @Override
    public int hashCode()
    {
        int hash = 3;
        // 我们在这里选取的是最不容易重复的四个字段，但是说实话，不太完美
        hash = 7 * hash + this.mRoomName.hashCode();
        hash = 7 * hash + this.mDetailedAddress.hashCode();
        hash = 7 * hash + this.mRoomPhotoUrl.hashCode();
        hash = 7 * hash + this.mRoomPhone.hashCode();
        return hash;
    }

    // 我们需要重写equals方法，否则判断List元素重复的方法会出现异常
    @Override
    public boolean equals(Object object)
    {
        boolean result = false;
        if (object == null || object.getClass() != this.getClass())
        {
            result = false;
        } else
        {
            NearbyRoomSubFragmentRoomBean thatObj = (NearbyRoomSubFragmentRoomBean) object;
            if (this.mRoomName.equals(thatObj.mRoomName) && this.mDetailedAddress.equals(thatObj.mDetailedAddress)
                    && this.mRoomPhotoUrl.equals(thatObj.mRoomPhotoUrl) && this.mRoomPhone.equals(thatObj.mRoomPhone))
            {
                result = true;
            }
        }
        Log.d("room_bean", " and the final result we get are : " + result);
        return result;
    }

    public String getShopHours()
    {
        return mShopHours;
    }

    public void setShopHours(String shopHours)
    {
        this.mShopHours = shopHours;
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
        dest.writeString(mRoomId);
        dest.writeString(mDetailedAddress);
        dest.writeString(mDistance);
        dest.writeFloat(mLevel);
        dest.writeString(mRoomPhotoUrl);
        dest.writeDouble(mPrice);
        dest.writeString(mRoomName);
        dest.writeString(mRoomPhone);
        dest.writeString(mRoomTag);
        dest.writeString(mRoomInfo);
        dest.writeString(mShopHours);
    }

    public static final Creator<NearbyRoomSubFragmentRoomBean> CREATOR = new Creator<NearbyRoomSubFragmentRoomBean>()
    {
        @Override
        public NearbyRoomSubFragmentRoomBean createFromParcel(Parcel source)
        {
            return new NearbyRoomSubFragmentRoomBean(source);
        }

        @Override
        public NearbyRoomSubFragmentRoomBean[] newArray(int size)
        {
            return new NearbyRoomSubFragmentRoomBean[size];
        }
    };

    public NearbyRoomSubFragmentRoomBean(Parcel source)
    {
        this.mRoomId = source.readString();
        this.mDetailedAddress = source.readString();
        this.mDistance = source.readString();
        this.mLevel = source.readFloat();
        this.mRoomPhotoUrl = source.readString();
        this.mPrice = source.readDouble();
        this.mRoomName = source.readString();
        this.mRoomPhone = source.readString();
        this.mRoomTag = source.readString();
        this.mRoomInfo = source.readString();
        this.mShopHours = source.readString();
    }

}

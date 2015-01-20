package com.yueqiu.bean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;

/**
 * Created by scguo on 14/12/25.
 *
 * 用于控制每一个球厅显示的属性的Bean文件
 * 用于在球厅的ListView item当中的bean文件
 */
public class SearchRoomSubFragmentRoomBean
{
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

    public SearchRoomSubFragmentRoomBean(){}

    public SearchRoomSubFragmentRoomBean(String roomPhoto, String roomName, float level, double price, String address, String distance, String roomPhone, String roomTag, String roomInfo)
    {
        this.mDetailedAddress = address;
        this.mDistance = distance;
        this.mLevel = level;
        this.mRoomPhotoUrl = roomPhoto;
        this.mPrice = price;
        this.mRoomName = roomName;
        this.mRoomPhone = roomPhone;
        this.mRoomTag = roomTag;
        this.mRoomInfo = roomInfo;
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

    public SearchRoomSubFragmentRoomBean(JSONObject initialJson) throws JSONException
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
}

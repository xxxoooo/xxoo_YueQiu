package com.yueqiu.adapter.bean;

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

    public SearchRoomSubFragmentRoomBean(){}

    public SearchRoomSubFragmentRoomBean(String roomPhoto, String roomName, float level, double price, String address, String distance)
    {
        this.mDetailedAddress = address;
        this.mDistance = distance;
        this.mLevel = level;
        this.mRoomPhotoUrl = roomPhoto;
        this.mPrice = price;
        this.mRoomName = roomName;
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
}

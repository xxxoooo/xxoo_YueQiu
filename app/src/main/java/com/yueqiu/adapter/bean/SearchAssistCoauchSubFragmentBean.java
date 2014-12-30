package com.yueqiu.adapter.bean;

/**
 * Created by scguo on 14/12/25.
 *
 * 这是用于创建SearchActivity当中的助教Fragment当中的助教的Bean
 * 主要是用于创建ListView当中的item
 */
public class SearchAssistCoauchSubFragmentBean
{
    private String mPhoto;
    private String mName;
    private String mGender;
    // 用于教练的球种
    private String mKinds;
    private String mDistance;

    private String mPrice;

    public SearchAssistCoauchSubFragmentBean(String photo, String name, String gender, String kinds, String price, String distan)
    {
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
}

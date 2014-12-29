package com.yueqiu.bean;

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
}
























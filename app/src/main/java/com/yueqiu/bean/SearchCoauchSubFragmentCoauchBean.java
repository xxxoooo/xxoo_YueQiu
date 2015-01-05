package com.yueqiu.bean;

/**
 * Created by scguo on 14/12/25.
 *
 * 用于SearchActivity当中的教练的Bean文件
 */
public class SearchCoauchSubFragmentCoauchBean
{
    private String mUserPhoto;
    private String mUserName;
    private String mUserGender;
    private String mUserDistance;
    private String mUserLevel;
    private String mBilliardKind;

    public SearchCoauchSubFragmentCoauchBean(String photo, String name, String gender, String distance, String level, String kind)
    {
        this.mUserPhoto = photo;
        this.mUserName = name;
        this.mUserGender = gender;
        this.mUserDistance = distance;
        this.mUserLevel = level;
        this.mBilliardKind = kind;

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
}

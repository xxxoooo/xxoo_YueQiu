package com.yueqiu.adapter.bean;

/**
 * Created by scguo on 14/12/25.
 *
 * 这是用于创建SearchActivity当中的约球Fragment当中的用户的Bean
 * 主要是用于创建ListView当中的item
 *
 */
public class SearchDatingSubFragmentDatingBean
{
    private String mUserPhoto;
    private String mUserName;
    private String mUserDeclare;
    private String mUserDistance;

    public SearchDatingSubFragmentDatingBean(String photo, String name, String declare, String distance)
    {
        this.mUserDeclare = declare;
        this.mUserPhoto = photo;
        this.mUserDistance = distance;
        this.mUserName = name;
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
}

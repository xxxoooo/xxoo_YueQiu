package com.yueqiu.bean;

/**
 * Created by scguo on 14/12/30.
 *
 * 用于展示SearchActivity当中约球Fragment当中的ListView的具体条目展开之后
 * 的Activity当中对应的“已参加人员”的GridView当中的列出来的表格信息
 *
 */
public class SearchDatingDetailedAlreadyBean
{
    private String mUserPhoto;
    private String mUserName;

    public SearchDatingDetailedAlreadyBean(String photo, String name)
    {
        this.mUserPhoto = photo;
        this.mUserName = name;
    }

    public String getUserPhoto()
    {
        return mUserPhoto;
    }

    public void setUserPhoto(String photo)
    {
        this.mUserPhoto = photo;
    }

    public String getUserName()
    {
        return mUserName;
    }

    public void setUserName(String name)
    {
        this.mUserName = name;
    }
}

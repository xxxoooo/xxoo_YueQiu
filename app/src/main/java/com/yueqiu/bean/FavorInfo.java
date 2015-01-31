package com.yueqiu.bean;


import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 15/1/13.
 */
public class FavorInfo implements ISlideMenuBasic{

    private int user_id;
    private String table_id;
    private int type;
    private String title;
    private String content;
    private String createTime;
    private String userName;
    //TODO:不做缓存的话，不需要这个字段，现在先不做缓存，所以去掉
//    private int subType;
    private boolean checked;

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getTable_id() {
        return table_id;
    }

    public void setTable_id(String table_id) {
        this.table_id = table_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
//
//    public int getSubType() {
//        return subType;
//    }
//
//    public void setSubType(int subType) {
//        this.subType = subType;
//    }

    @Override
    public boolean equals(Object o) {
        FavorInfo item = (FavorInfo) o;
        if(this.table_id.equals(item.getTable_id()) && this.type == item.getType()
                && this.user_id == item.getUser_id()){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result  = 37 * result + table_id.hashCode();
        result  = 37 * result + type;
        result  = 37 * result + user_id;
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
    public int describeContents() {
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
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(user_id);
        dest.writeString(table_id);
        dest.writeInt(type);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(createTime);
        dest.writeString(userName);
        Bundle arg = new Bundle();
        arg.putBoolean("checked",checked);
        dest.writeBundle(arg);
    }

    public static Creator<FavorInfo> CREATOR = new Creator<FavorInfo>() {
        @Override
        public FavorInfo createFromParcel(Parcel source) {
            return new FavorInfo(source);
        }

        @Override
        public FavorInfo[] newArray(int size) {
            return new FavorInfo[size];
        }
    };

    public FavorInfo(Parcel in) {
        user_id = in.readInt();
        table_id = in.readString();
        type = in.readInt();
        title = in.readString();
        content = in.readString();
        createTime = in.readString();
        userName = in.readString();
        checked = in.readBundle().getBoolean("checked");
    }

    public FavorInfo() {
    }
}

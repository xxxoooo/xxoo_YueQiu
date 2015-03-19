package com.yueqiu.bean;

import android.os.Bundle;
import android.os.Parcel;

/**
 * Created by wangyun on 15/1/4.
 */
public class PublishedInfo implements ISlideMenuBasic{
    private int user_id;
    private int type;/*发布类型*/
    private String table_id;/*表Id*/
    private String title;/*标题*/
    private String content;/*内容*/
    private String dateTime;/*时间*/
    //TODO:先暂时不需要这个字段
//    private int subType;
    private boolean checked;


    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTable_id() {
        return table_id;
    }

    public void setTable_id(String table_id) {
        this.table_id = table_id;
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

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

//    public int getSubType() {
//        return subType;
//    }
//
//    public void setSubType(int subtype) {
//        this.subType = subtype;
//    }

    @Override
    public boolean equals(Object o) {
        PublishedInfo item = (PublishedInfo) o;
        if(this.table_id.equals(item.getTable_id()) && this.type == item.getType()
                &&this.user_id == item.user_id){
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

    @Override
    public String toString() {
        String str = "table_id->" + table_id + " type->" + type + " title->" + title
               + " content->" + content + " dateTime->" + dateTime;
        return str;
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
        dest.writeInt(type);
        dest.writeString(table_id);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(dateTime);
        Bundle arg = new Bundle();
        arg.putBoolean("checked",checked);
        dest.writeBundle(arg);
    }

    public static final Creator<PublishedInfo> CREATOR = new Creator<PublishedInfo>() {
        @Override
        public PublishedInfo createFromParcel(Parcel source) {
            return new PublishedInfo(source);
        }

        @Override
        public PublishedInfo[] newArray(int size) {
            return new PublishedInfo[size];
        }
    };

    public PublishedInfo(Parcel in) {
        user_id = in.readInt();
        type = in.readInt();
        table_id = in.readString();
        title = in.readString();
        content = in.readString();
        dateTime = in.readString();
        checked = in.readBundle().getBoolean("checked");
    }

    public PublishedInfo() {
    }
}

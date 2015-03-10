package com.yueqiu.bean;

import android.os.Bundle;
import android.os.Parcel;

/**
 * Created by wangyun on 15/2/7.
 */
public class PartInInfo implements ISlideMenuBasic{
    private int user_id;
    private int type;
    private String table_id;
    private String title;
    private String content;
    private String dateTime;
    private String img_url;
    private String username;
    private boolean checked;
    private int rid;

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

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    @Override
    public boolean equals(Object o) {
        PartInInfo info = (PartInInfo) o;
        if(this.table_id.equals(info.getTable_id()) && this.type == info.getType()
                && this.user_id == info.user_id){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + table_id.hashCode();
        result = 37 * result + type;
        result = 37 * result + user_id;
        return result;
    }

    @Override
    public String toString() {
        String str = "table_id->" + table_id + " type->" + type + " title->" + title
                + " content->" + content + " dateTime->" + dateTime;
        return str;
    }

    @Override
    public int describeContents() {
        return 0;
    }

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
        dest.writeInt(rid);
    }

    public static final Creator<PartInInfo> CREATOR = new Creator<PartInInfo>() {
        @Override
        public PartInInfo createFromParcel(Parcel source) {
            return new PartInInfo(source);
        }

        @Override
        public PartInInfo[] newArray(int size) {
            return new PartInInfo[size];
        }
    };

    public PartInInfo(Parcel in){
        user_id = in.readInt();
        type = in.readInt();
        table_id = in.readString();
        title = in.readString();
        content = in.readString();
        dateTime = in.readString();
        checked = in.readBundle().getBoolean("checked");
        rid = in.readInt();
    }

    public PartInInfo(){}
}

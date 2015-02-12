package com.yueqiu.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class PlayInfo implements Parcelable{
    private String table_id;
    private String username;
    private String sex;//男1女2
    private String img_url;
    private String type;
    private String title;
    private String address;
    private String begin_time;
    private String end_time;
    private String model;
    private String content;
    private String create_time;
    private int look_num;
    private String contact;
    private String phone;
    //TODO:已参加人员的list
    public List<UserInfo> mJoinList = new ArrayList<UserInfo>();

    public String getTable_id() {
        return table_id;
    }

    public void setTable_id(String table_id) {
        this.table_id = table_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBegin_time() {
        return begin_time;
    }

    public void setBegin_time(String begin_time) {
        this.begin_time = begin_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getLook_num() {
        return look_num;
    }

    public void setLook_num(int look_num) {
        this.look_num = look_num;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public PlayInfo() {
    }

    public PlayInfo(Parcel in) {
        table_id = in.readString();
        username = in.readString();
        sex = in.readString();
        img_url = in.readString();
        type = in.readString();
        title = in.readString();
        address = in.readString();
        begin_time = in.readString();
        end_time = in.readString();
        model = in.readString();
        content = in.readString();
        create_time = in.readString();
        look_num = in.readInt();
        contact = in.readString();
        phone = in.readString();
    }

    @Override
    public boolean equals(Object o) {
        PlayInfo info = (PlayInfo) o;
        if(this.table_id.equals(info.getTable_id())){
            return true;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + table_id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Activities{" +
                "id='" + table_id + '\'' +
                ", username='" + username + '\'' +
                ", sex='" + sex + '\'' +
                ", img_url='" + img_url + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", address='" + address + '\'' +
                ", begin_time='" + begin_time + '\'' +
                ", end_time='" + end_time + '\'' +
                ", model='" + model + '\'' +
                ", content='" + content + '\'' +
                ", create_time='" + create_time + '\'' +
                ", look_num=" + look_num +
                '}';
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
        dest.writeString(table_id);
        dest.writeString(username);
        dest.writeString(sex);
        dest.writeString(img_url);
        dest.writeString(type);
        dest.writeString(title);
        dest.writeString(address);
        dest.writeString(begin_time);
        dest.writeString(end_time);
        dest.writeString(model);
        dest.writeString(content);
        dest.writeString(create_time);
        dest.writeInt(look_num);
        dest.writeString(contact);
        dest.writeString(phone);
    }

    public static final Parcelable.Creator<PlayInfo> CREATOR
            = new  Parcelable.Creator<PlayInfo>(){
        public PlayInfo createFromParcel(Parcel in) {
            return new PlayInfo(in);
        }

        public PlayInfo[] newArray(int size) {
            return new PlayInfo[size];
        }
    };
}

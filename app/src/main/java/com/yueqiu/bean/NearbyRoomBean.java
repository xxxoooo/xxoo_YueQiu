package com.yueqiu.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wangyun on 15/4/19.
 */
public class NearbyRoomBean implements Parcelable {
    private String id;
    private String name;
    private String address;
    private String telephone;
    private String detail_info;
    private String price;
    private String shop_hours;
    private String range;
    private String overall_rating;
    private String img_url;

    public NearbyRoomBean() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getDetail_info() {
        return detail_info;
    }

    public void setDetail_info(String detail_info) {
        this.detail_info = detail_info;
    }

    public String getShop_hours() {
        return shop_hours;
    }

    public void setShop_hours(String shop_hours) {
        this.shop_hours = shop_hours;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getOverall_rating() {
        return overall_rating;
    }

    public void setOverall_rating(String overall_rating) {
        this.overall_rating = overall_rating;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "id is : " + id + " name is : " + name + " img_url is : " + img_url + " address is : " + address;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o == null ||  o.getClass() != this.getClass()){
            result = false;
        } else{
            NearbyRoomBean bean = (NearbyRoomBean) o;
            if (bean.getId().equals(this.getId())){
                result = true;
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + id.hashCode();
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
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(telephone);
        dest.writeString(detail_info);
        dest.writeString(price);
        dest.writeString(shop_hours);
        dest.writeString(range);
        dest.writeString(overall_rating);
        dest.writeString(img_url);
    }
    public static final Creator<NearbyRoomBean> CREATOR = new Creator<NearbyRoomBean>() {
        @Override
        public NearbyRoomBean createFromParcel(Parcel source) {
            return new NearbyRoomBean(source);
        }

        @Override
        public NearbyRoomBean[] newArray(int size) {
            return new NearbyRoomBean[size];
        }
    };

    public NearbyRoomBean(Parcel in){
        id = in.readString();
        name = in.readString();
        address = in.readString();
        telephone = in.readString();
        detail_info = in.readString();
        price = in.readString();
        shop_hours = in.readString();
        range = in.readString();
        overall_rating = in.readString();
        img_url = in.readString();
    }
}

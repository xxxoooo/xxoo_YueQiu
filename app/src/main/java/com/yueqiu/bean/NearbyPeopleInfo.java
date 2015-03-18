package com.yueqiu.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by doushuqi on 15/1/5.
 * 搜素结果实体类
 */
public class NearbyPeopleInfo {
    private int user_id;
    private int count;//总条数
    public List<SearchPeopleItemInfo> mList = new ArrayList<SearchPeopleItemInfo>();

    public class SearchPeopleItemInfo{
        private int user_id;//用户id
        private String username;//昵称
        private String img_url;//头像
        private String content;//最后发布的消息
        private String datetime;//时间
        private int sex;//性别
        private String district;//地区
        private String distance;

        public void setUsername(String username) {
            this.username = username;
        }

        public String getUsername() {

            return username;
        }

        public int getSex() {
            return sex;
        }

        public String getDistrict() {
            return district;
        }

        public void setSex(int sex) {

            this.sex = sex;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public void setImg_url(String img_url) {
            this.img_url = img_url;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setDatetime(String datetime) {
            this.datetime = datetime;
        }

        public String getDatetime() {

            return datetime;
        }

        public String getContent() {
            return content;
        }

        public String getImg_url() {
            return img_url;
        }

        public int getUser_id() {
            return user_id;
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 37 * result + user_id;
            return result;
        }

        @Override
        public boolean equals(Object o) {
            SearchPeopleItemInfo info = (SearchPeopleItemInfo) o;
            if(this.user_id == info.getUser_id()){
                return true;
            }
            return false;
        }


    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getUser_id() {

        return user_id;
    }

    public int getCount() {
        return count;
    }

}

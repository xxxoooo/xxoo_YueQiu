package com.yueqiu.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by doushuqi on 15/1/5.
 * 搜素结果实体类
 */
public class SearchPeopleInfo {
    private int user_id;
    private int count;//总条数
    public List<SearchPeopleItemInfo> mList = new ArrayList<SearchPeopleItemInfo>();

    public class SearchPeopleItemInfo{
        private int user_id;//用户id
        private int group_id;//分组id
        private int account;//昵称，接口中这样声明的
        private String img_url;//头像
        private String content;//最后发布的消息
        private String datetime;//时间

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public void setGroup_id(int group_id) {
            this.group_id = group_id;
        }

        public void setAccount(int account) {
            this.account = account;
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

        public int getAccount() {
            return account;
        }

        public int getGroup_id() {
            return group_id;
        }

        public int getUser_id() {
            return user_id;
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

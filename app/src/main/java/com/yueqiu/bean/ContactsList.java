package com.yueqiu.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by doushuqi on 15/1/6.
 */
public class ContactsList {

    private int count;

    public List<Contacts> mList = new ArrayList<Contacts>();

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {

        return count;
    }

    public class Contacts {
        private int user_id;
        private int group_id;
        private String username;
        private String img_url;
        private String content;
        private String create_time;


        public void setUsername(String username) {
            this.username = username;
        }

        public void setImg_url(String img_url) {
            this.img_url = img_url;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setCreate_time(String create_time) {
            this.create_time = create_time;
        }

        public void setUser_id(int user_id) {
            this.user_id = user_id;
        }

        public void setGroup_id(int group_id) {
            this.group_id = group_id;
        }

        public int getUser_id() {

            return user_id;
        }

        public int getGroup_id() {
            return group_id;
        }

        public String getUsername() {
            return username;
        }

        public String getImg_url() {
            return img_url;
        }

        public String getContent() {
            return content;
        }

        public String getCreate_time() {
            return create_time;
        }
    }



}

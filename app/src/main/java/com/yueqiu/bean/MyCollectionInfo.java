package com.yueqiu.bean;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 15/1/13.
 */
public class MyCollectionInfo {

    private int user_id;
    private int start_no;
    private int end_no;
    private int count;
    private int type;
    public List<CollectionItemInfo> mList = new ArrayList<CollectionItemInfo>();

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getStart_no() {
        return start_no;
    }

    public void setStart_no(int start_no) {
        this.start_no = start_no;
    }

    public int getEnd_no() {
        return end_no;
    }

    public void setEnd_no(int end_no) {
        this.end_no = end_no;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public class CollectionItemInfo {

        private String table_id;
        private int type;
        private String img_url;
        private String title;
        private String content;
        private String createTime;
        private String userName;
        private boolean checked;

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

        public String getImage_url() {
            return img_url;
        }

        public void setImage_url(String img_url) {
            this.img_url = img_url;
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
            return createTime;
        }

        public void setDateTime(String dateTime) {
            this.createTime = dateTime;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }
}

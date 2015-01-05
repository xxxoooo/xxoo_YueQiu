package com.yueqiu.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 15/1/4.
 */
public class PublishedInfo {

    private int user_id;
    private int type;/*发布类型*/
    private int start_no;/*开始条数*/
    private int end_no;/*结束条数*/
    private int sumCount;/*总条数*/
    public List<PublishedItemInfo> mList = new ArrayList<PublishedItemInfo>();


    public class PublishedItemInfo{
        private String table_id;/*表Id*/
        private int type;
        private String image_url;/*头像*/
        private String title;/*标题*/
        private String content;/*内容*/
        private String dateTime;/*时间*/

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
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
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
    }


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

    public int getSumCount() {
        return sumCount;
    }

    public void setSumCount(int sumCount) {
        this.sumCount = sumCount;
    }


}

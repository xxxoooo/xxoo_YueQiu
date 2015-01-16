package com.yueqiu.bean;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 15/1/4.
 */
public class PublishedInfo {

    private static final String USER_ID     = "user_id";
    private static final String STAR_NO     = "start_no";
    private static final String END_NO      = "end_no";
    private static final String COUNT       = "count";
    private static final String LIST_DATA   = "list_data";
    private static final String ID          = "id";
    private static final String IMG_URL     = "img_url";
    private static final String TITLE       = "title";
    private static final String CONTENT     = "content";
    private static final String CREATE_TIME = "create_time";

    private int user_id;
    private int type;/*发布类型*/
    private int start_no;/*开始条数*/
    private int end_no;/*结束条数*/
    private int sumCount;/*总条数*/
    public List<PublishedItemInfo> mList = new ArrayList<PublishedItemInfo>();


    public class PublishedItemInfo {
        private String table_id;/*表Id*/
        private int type;
        private String title;/*标题*/
        private String content;/*内容*/
        private String dateTime;/*时间*/
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

        @Override
        public boolean equals(Object o) {
            PublishedItemInfo item = (PublishedItemInfo) o;
            if(this.getTable_id().equals(item.getTable_id())){
                return true;
            }
            return false;
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

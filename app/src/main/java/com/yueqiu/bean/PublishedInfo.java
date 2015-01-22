package com.yueqiu.bean;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 15/1/4.
 */
public class PublishedInfo {
    private int user_id;
    private int type;/*发布类型*/
    private String table_id;/*表Id*/
    private String title;/*标题*/
    private String content;/*内容*/
    private String dateTime;/*时间*/
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





}

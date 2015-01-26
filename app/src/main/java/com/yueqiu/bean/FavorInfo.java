package com.yueqiu.bean;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangyun on 15/1/13.
 */
public class FavorInfo {

    private int user_id;
    private String table_id;
    private int type;
    private String title;
    private String content;
    private String createTime;
    private String userName;
    private int subType;
    private boolean checked;

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    @Override
    public boolean equals(Object o) {
        FavorInfo item = (FavorInfo) o;
        if(this.table_id.equals(item.getTable_id()) && this.type == item.getType()
                && this.user_id == item.getUser_id()){
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
}

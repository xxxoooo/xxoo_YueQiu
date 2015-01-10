package com.yueqiu.bean;

import java.util.List;

/**
 * Created by yinfeng on 15/1/9.
 */
public class ActivitiesList {
    private String id;
    private String img_url;
    private String content;
    private String create_time;
    private List<Activities> list;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
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

    public List<Activities> getList() {
        return list;
    }

    public void setList(List<Activities> list) {
        this.list = list;
    }
}

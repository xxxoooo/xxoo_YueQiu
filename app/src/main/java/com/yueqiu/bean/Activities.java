package com.yueqiu.bean;

/**
 * Created by yinfeng on 15/1/3.
 */
public class Activities {
    private int id;
    private int img_url;
    private String title;
    private String activitis_time;
    private String time_day;
    private String time_hour;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getImg_url() {
        return img_url;
    }

    public void setImg_url(int img_url) {
        this.img_url = img_url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getActivitis_time() {
        return activitis_time;
    }

    public void setActivitis_time(String activitis_time) {
        this.activitis_time = activitis_time;
    }

    public String getTime_day() {
        return time_day;
    }

    public void setTime_day(String time_day) {
        this.time_day = time_day;
    }

    public String getTime_hour() {
        return time_hour;
    }

    public void setTime_hour(String time_hour) {
        this.time_hour = time_hour;
    }
}
